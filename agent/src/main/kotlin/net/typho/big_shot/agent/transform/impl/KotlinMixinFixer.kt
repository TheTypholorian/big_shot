package net.typho.big_shot.agent.transform.impl

import net.typho.asm_util.ASMUtil.splice
import net.typho.asm_util.ClassTransformInfo
import net.typho.asm_util.KotlinUtil.kotlinMetadata
import net.typho.asm_util.insn.InsnPointer
import net.typho.asm_util.method.MethodPointer
import net.typho.asm_util.remap.CompatClassRemapper
import net.typho.big_shot.agent.Log
import net.typho.big_shot.agent.PlatformMod
import net.typho.big_shot.agent.platform.fabric.BigShotFabric
import net.typho.big_shot.agent.transform.TransformEvent
import net.typho.big_shot.common.event.EventGraph
import net.typho.big_shot.common.mixin.kotlin.InvalidKotlinMixinException
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.commons.Remapper
import org.objectweb.asm.tree.ClassNode
import org.spongepowered.asm.mixin.Mixins
import org.spongepowered.asm.service.MixinService
import kotlin.metadata.ClassKind
import kotlin.metadata.jvm.KotlinClassMetadata
import kotlin.metadata.kind

object KotlinMixinFixer : EventGraph.SelfAware<String>, TransformEvent {
    override val id: String
        get() = "big_shot:kotlin_mixin_fixer"

    override fun transform(mod: PlatformMod?, info: ClassTransformInfo) {
    }

    override fun transformMixin(
        info: ClassTransformInfo
    ) {
        if (fix(info.node)) {
            info.markChanged()
        }
    }

    /**
     * If the mixin is written in kotlin, this method fixes it so it works fine (specifically, static methods).
     *
     * If the mixin isn't written in kotlin, nothing happens and the method returns false.
     *
     * @return If the class was changed
     */
    @Suppress("UNCHECKED_CAST")
    @JvmStatic
    fun fix(node: ClassNode): Boolean {
        val metadata = KotlinClassMetadata.readLenient(node.kotlinMetadata ?: return false)
        var changed = false

        if (metadata !is KotlinClassMetadata.Class) {
            Log.warn("Kotlin mixin ${node.name} must be a normal class for Big Shot to tweak it, got a ${metadata.javaClass.name}. No action will be taken, if there are problems with the mixin, this is likely a cause.")
            return false
        }

        Log.debug("Fixing kotlin mixin ${node.name}")

        when (metadata.kmClass.kind) {
            ClassKind.OBJECT -> {
                changed = true
                node.fields.removeIf { it.name == "INSTANCE" }
                MethodPointer.method()
                    .name("<clinit>")
                    .find(node).forEach { method ->
                        method.instructions.splice(
                            InsnPointer.type(node.name),
                            InsnPointer.fieldSetStatic()
                                .owner(node.name)
                                .name("INSTANCE")
                                .desc("L${node.name};")
                        )
                    }
            }
            ClassKind.ENUM_CLASS -> {
                changed = true
                node.fields.removeIf { it.desc == "Lkotlin/enums/EnumEntries;" && it.access and Opcodes.ACC_SYNTHETIC != 0 }
                node.methods.removeIf { it.name == "getEntries" && it.desc == "()Lkotlin/enums/EnumEntries;" }
                MethodPointer.method()
                    .name("<clinit>")
                    .findOrThrow(node) { method ->
                        method.instructions.splice(
                            InsnPointer.fieldGetStatic()
                                .owner(node.name)
                                .desc("[L${node.name};"),
                            InsnPointer.fieldSetStatic()
                                .owner(node.name)
                                .desc("Lkotlin/enums/EnumEntries;")
                        )
                    }
            }
            else -> {}
        }

        metadata.kmClass.companionObject?.let { companion ->
            changed = true
            val fullCompanion = "${node.name}$$companion"
            node.fields.removeIf { it.name == "Companion" }

            when (metadata.kmClass.kind) {
                ClassKind.INTERFACE -> {
                    MethodPointer.method()
                        .name("<clinit>")
                        .findOrThrow(node) { method ->
                            method.instructions.splice(
                                InsnPointer.fieldGetStatic()
                                    .owner(fullCompanion)
                                    .desc("L$fullCompanion;"),
                                InsnPointer.fieldSetStatic()
                                    .owner(node.name)
                                    .name("Companion")
                                    .desc("L$fullCompanion;")
                            )
                        }
                }
                else -> {
                    MethodPointer.method()
                        .name("<clinit>")
                        .findOrThrow(node) { method ->
                            method.instructions.splice(
                                InsnPointer.type(fullCompanion),
                                InsnPointer.fieldSetStatic()
                                    .owner(node.name)
                                    .name("Companion")
                                    .desc("L$fullCompanion;")
                            )
                        }
                }
            }

            val companionVisitor = CompanionObjectVisitor(Opcodes.ASM9, node, node.name, fullCompanion)
            val companionNode = MixinService.getService().bytecodeProvider.getClassNode(fullCompanion)

            for (field in companionNode.fields) {
                if (field.desc != "L$fullCompanion;") {
                    if ((field.access and Opcodes.ACC_SYNTHETIC) == 0 && field.visibleAnnotations.none { it.desc == "Lkotlin/jvm/JvmStatic;" }) {
                        throw InvalidKotlinMixinException("All mixin companion object fields and methods must be compiled with @JvmStatic, field ${node.name} ${field.name} ${field.desc} was not")
                    }

                    field.accept(companionVisitor)
                }
            }

            for (method in companionNode.methods) {
                if (method.name != "<init>" && method.name != "<clinit>" && !((method.access and Opcodes.ACC_SYNTHETIC) != 0 && method.name.startsWith("access$"))) {
                    if ((method.access and Opcodes.ACC_SYNTHETIC) == 0 && method.visibleAnnotations.none { it.desc == "Lkotlin/jvm/JvmStatic;" }) {
                        throw InvalidKotlinMixinException("All mixin companion object fields and methods must be compiled with @JvmStatic, method ${node.name} ${method.name} ${method.desc} was not")
                    }

                    node.methods.removeIf { it.name == method.name && it.desc == method.desc }
                    method.accept(companionVisitor)
                }
            }
        }

        node.visibleAnnotations?.removeIf { it.desc == "Lkotlin/Metadata;" }

        return changed
    }

    open class CompanionObjectVisitor(
        api: Int,
        classVisitor: ClassVisitor?,
        @JvmField
        val parentName: String,
        @JvmField
        val fullCompanionName: String
    ) : CompatClassRemapper(api, classVisitor, object : Remapper(api) {
        override fun map(internalName: String?): String? {
            if (internalName == fullCompanionName) {
                return parentName
            }

            return super.map(internalName)
        }
    }) {
        override fun visitMethod(
            access: Int,
            name: String,
            descriptor: String,
            signature: String?,
            exceptions: Array<String>?
        ): MethodVisitor {
            return object : MethodVisitor(api, super.visitMethod(access or Opcodes.ACC_STATIC, name, descriptor, signature, exceptions)) {
                override fun visitVarInsn(opcode: Int, varIndex: Int) {
                    if (varIndex != 0) {
                        super.visitVarInsn(opcode, varIndex - 1) // remove 'this'
                    }
                }

                override fun visitFieldInsn(opcode: Int, owner: String, name: String, descriptor: String) {
                    super.visitFieldInsn(if (owner == fullCompanionName) when (opcode) {
                        Opcodes.GETFIELD -> Opcodes.GETSTATIC
                        Opcodes.PUTFIELD -> Opcodes.PUTSTATIC
                        else -> opcode
                    } else opcode, owner, name, descriptor)
                }

                override fun visitMethodInsn(
                    opcode: Int,
                    owner: String,
                    name: String,
                    descriptor: String,
                    isInterface: Boolean
                ) {
                    super.visitMethodInsn(if (owner == fullCompanionName) Opcodes.INVOKESTATIC else opcode, owner, name, descriptor, isInterface)
                }
            }
        }

        // TODO method calls on fellow companion objects (or self)
    }
}