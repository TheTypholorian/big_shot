package net.typho.big_shot.agent

import net.typho.asm_util.ClassTransformInfo
import net.typho.asm_util.error.ClassVisitException
import net.typho.asm_util.insn.InsnPointer
import net.typho.asm_util.method.MethodPointer
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.FieldInsnNode
import org.objectweb.asm.tree.InsnList
import org.objectweb.asm.tree.InsnNode
import org.objectweb.asm.tree.LdcInsnNode
import org.objectweb.asm.tree.MethodInsnNode
import org.objectweb.asm.tree.VarInsnNode
import java.lang.instrument.Instrumentation
import java.nio.file.Files
import java.nio.file.Paths
import java.util.jar.JarFile
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.createParentDirectories
import kotlin.io.path.deleteRecursively
import kotlin.io.path.outputStream
import kotlin.io.path.writeBytes

object BigShotAgent {
    @JvmField
    val DEBUG_PATH = Paths.get(".big_shot_debug")
    @JvmField
    val API_PATH = Files.createTempDirectory("big_shot_agent").resolve("api.jar")

    @JvmStatic
    fun debugSaveClass(
        className: String,
        bytes: ByteArray
    ) {
        val path = DEBUG_PATH.resolve("$className.class")
        path.createParentDirectories()
        path.writeBytes(bytes)
    }

    @OptIn(ExperimentalPathApi::class)
    @JvmStatic
    fun premain(args: String?, inst: Instrumentation) {
        DEBUG_PATH.deleteRecursively()

        API_PATH.outputStream().use { output ->
            javaClass.classLoader.getResourceAsStream("api.jar").use { input ->
                input!!.copyTo(output)
            }
        }
        inst.appendToSystemClassLoaderSearch(JarFile(API_PATH.toFile()))

        inst.addTransformer({ loader, className, classBeingRedefined, domain, bytes ->
            try {
                val info = ClassTransformInfo.ByteTransform(bytes)

                when (className) {
                    "net/fabricmc/loader/impl/launch/knot/Knot" -> {
                        info.markChanged()
                        info.computeMaxStacks()

                        val bigShotApi = loader.loadClass("net.typho.big_shot.api.BigShot")
                        bigShotApi.getField("API_PATH").set(null, API_PATH)
                        bigShotApi.getField("INSTRUMENTATION").set(null, inst)
                        bigShotApi.getMethod("onInstrumentationInit").invoke(null)

                        MethodPointer.method()
                            .name("<clinit>")
                            .findOrThrow(info.node) { method ->
                                method.instructions.insertBefore(
                                    InsnPointer.simple()
                                        .opcode(Opcodes.RETURN)
                                        .findOrThrow(method.instructions),
                                    MethodInsnNode(
                                        Opcodes.INVOKESTATIC,
                                        "net/typho/big_shot/api/platform/fabric/BigShotFabric",
                                        "clinit",
                                        "()V"
                                    )
                                )
                            }
                        MethodPointer.method()
                            .name("init")
                            .desc("([Ljava/lang/String;)Ljava/lang/ClassLoader;")
                            .findOrThrow(info.node) { method ->
                                method.instructions.insert(
                                    InsnPointer.fieldSet()
                                        .owner(className)
                                        .name("provider")
                                        .findOrThrow(method.instructions),
                                    InsnList().apply {
                                        add(VarInsnNode(Opcodes.ALOAD, 0))
                                        add(FieldInsnNode(
                                            Opcodes.GETFIELD,
                                            className,
                                            "provider",
                                            "Lnet/fabricmc/api/impl/game/GameProvider;"
                                        ))
                                        add(MethodInsnNode(
                                            Opcodes.INVOKESTATIC,
                                            "net/typho/big_shot/api/platform/fabric/BigShotFabric",
                                            "loadGameProvider",
                                            "(Lnet/fabricmc/api/impl/game/GameProvider;)V"
                                        ))
                                    }
                                )
                                method.instructions.insert(
                                    InsnPointer.methodCallStatic()
                                        .owner("net/fabricmc/loader/impl/launch/FabricLauncherBase")
                                        .name("finishMixinBootstrapping")
                                        .findOrThrow(method.instructions),
                                    InsnList().apply {
                                        add(MethodInsnNode(
                                            Opcodes.INVOKESTATIC,
                                            "net/typho/big_shot/api/platform/fabric/BigShotFabric",
                                            "registerMixins",
                                            "()V"
                                        ))
                                    }
                                )
                            }
                    }
                    "net/fabricmc/loader/impl/FabricLoaderImpl" -> {
                        info.markChanged()
                        info.computeMaxStacks()

                        MethodPointer.method()
                            .name("finishModLoading")
                            .findOrThrow(info.node) { method ->
                                method.instructions.insert(
                                    InsnList().apply {
                                        add(MethodInsnNode(
                                            Opcodes.INVOKESTATIC,
                                            "net/typho/big_shot/api/platform/fabric/BigShotFabric",
                                            "finishModLoading",
                                            "()V"
                                        ))
                                    }
                                )
                            }
                    }
                    "org/spongepowered/asm/mixin/transformer/MixinInfo" -> {
                        info.markChanged()
                        info.computeMaxStacks()

                        MethodPointer.method()
                            .name("loadMixinClass")
                            .desc("(Ljava/lang/String;)Lorg/objectweb/asm/tree/ClassNode;")
                            .findOrThrow(info.node) { method ->
                                method.instructions.insertBefore(
                                    InsnPointer.simple()
                                        .opcode(Opcodes.ARETURN)
                                        .lastOrdinal()
                                        .findOrThrow(method.instructions),
                                    InsnList().apply {
                                        add(InsnNode(Opcodes.DUP))
                                        add(MethodInsnNode(
                                            Opcodes.INVOKESTATIC,
                                            "net/typho/big_shot/api/BigShot",
                                            "transformMixinInfo",
                                            "(Lorg/objectweb/asm/tree/ClassNode;)V"
                                        ))
                                    }
                                )
                            }
                    }
                    "org/spongepowered/asm/mixin/transformer/ClassInfo" -> {
                        info.markChanged()
                        info.computeMaxStacks()

                        MethodPointer.method()
                            .name("<init>")
                            .desc("(Lorg/objectweb/asm/tree/ClassNode;)V")
                            .findOrThrow(info.node) { method ->
                                method.instructions.insert(
                                    InsnPointer.methodCall()
                                        .owner("java/lang/Object")
                                        .name("<init>")
                                        .desc("()V")
                                        .ordinal(0)
                                        .findOrThrow(method.instructions),
                                    InsnList().apply {
                                        add(VarInsnNode(Opcodes.ALOAD, 1))
                                        add(MethodInsnNode(
                                            Opcodes.INVOKESTATIC,
                                            "net/typho/big_shot/api/BigShot",
                                            "transformClassInfo",
                                            "(Lorg/objectweb/asm/tree/ClassNode;)V"
                                        ))
                                    }
                                )
                            }
                    }
                }

                return@addTransformer info.compile(::debugSaveClass)
            } catch (t: Throwable) {
                ClassVisitException("Error while Big Shot Agent was transforming class $className", t).printStackTrace()

                return@addTransformer null
            }
        }, true)
    }
}