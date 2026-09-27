package net.typho.big_shot.agent.platform

import net.fabricmc.classtweaker.api.ClassTweaker
import net.fabricmc.classtweaker.api.ClassTweakerReader
import net.typho.asm_util.ClassTransformInfo
import net.typho.asm_util.method.MethodPointer
import net.typho.big_shot.agent.BigShotAgent
import net.typho.big_shot.agent.LOG
import net.typho.big_shot.agent.PlatformMod
import net.typho.big_shot.agent.entrypoint.EntrypointInput
import net.typho.big_shot.agent.entrypoint.EntrypointInputSupplier
import net.typho.big_shot.agent.transform.TransformEvent
import net.typho.big_shot.agent.transform.impl.ClassTweakerTransform
import net.typho.big_shot.common.KtServiceLoader
import net.typho.big_shot.common.KtServiceLoader.loadAll
import net.typho.big_shot.common.event.EventGraph
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import org.objectweb.asm.tree.FieldInsnNode
import org.objectweb.asm.tree.InsnList
import org.objectweb.asm.tree.InsnNode
import org.objectweb.asm.tree.MethodNode
import org.objectweb.asm.tree.VarInsnNode
import java.io.FileNotFoundException
import java.util.ServiceLoader
import kotlin.properties.Delegates
import kotlin.text.isNotBlank

abstract class BigShotPlatform : EventGraph.SelfAware<String>, TransformEvent {
    companion object {
        @JvmField
        val ENTRYPOINT_INPUT_TYPE = Type.getType(EntrypointInput::class.java)
        @JvmStatic
        @get:JvmName("getInstance")
        @set:JvmName("setInstance")
        var INSTANCE: BigShotPlatform? by Delegates.observable(null) { property, old, new ->
            if (old != null && old != new) {
                throw IllegalStateException("Already loaded big shot on $old but trying to load on $new")
            }
        }
            internal set

        @JvmStatic
        fun <S : Any> Map<PlatformMod, List<ServiceLoader.Provider<S>>>.loadAll() = mapValues { it.value.loadAll() }
    }

    private val entrypoints = mutableMapOf<PlatformMod, Map<String, List<String>>>()
    abstract val allMods: List<PlatformMod>
    abstract val loaded: Boolean
    abstract val classLoader: ClassLoader

    init {
        BigShotAgent.TRANSFORM_EVENTS.register(this)
    }

    override fun transform(mod: PlatformMod?, info: ClassTransformInfo) {
        mod ?: return
        val type = entrypoints[mod]?.entries?.firstOrNull { (type, values) -> values.contains(info.className.replace('/', '.')) }?.key

        if (type != null) {
            val clinit = InsnList()
            val init = InsnList()
            val suppliers by lazy { KtServiceLoader.load(EntrypointInputSupplier::class.java).loadAll() }

            fieldIt@
            for (field in info.node.fields) {
                val anno = field.visibleAnnotations?.firstOrNull { it.desc == ENTRYPOINT_INPUT_TYPE.descriptor }

                if (anno != null) {
                    info.markChanged()
                    info.computeFrames()
                    field.visibleAnnotations.remove(anno)
                    val instance = field.access and Opcodes.ACC_STATIC == 0
                    val insns = if (instance) init else clinit

                    for (supplier in suppliers) {
                        supplier.getInput(type, mod, info, field)?.let {
                            if (instance) {
                                insns.add(VarInsnNode(Opcodes.ALOAD, 0))
                                insns.add(it)
                                insns.add(FieldInsnNode(
                                    Opcodes.PUTFIELD,
                                    info.className,
                                    field.name,
                                    field.desc
                                ))
                            } else {
                                insns.add(it)
                                insns.add(FieldInsnNode(
                                    Opcodes.PUTSTATIC,
                                    info.className,
                                    field.name,
                                    field.desc
                                ))
                            }

                            continue@fieldIt
                        }
                    }

                    LOG.warn("No entrypoint input value found for field ${info.className}.${field.name} ${field.desc}, skipping")
                }
            }

            if (clinit.size() > 0) {
                val method = MethodPointer.method()
                    .name("<clinit>")
                    .find(info.node)
                    .firstOrNull()
                    ?: MethodNode(
                        Opcodes.ACC_STATIC,
                        "<clinit>",
                        "()V",
                        null,
                        null
                    ).also {
                        it.instructions.add(InsnNode(Opcodes.RETURN))
                        info.node.methods.add(it)
                    }
                method.instructions.insert(clinit)
            }

            if (init.size() > 0) {
                val method = MethodPointer.method()
                    .name("<init>")
                    .findOrThrow(info.node)
                method.instructions.insert(init)
            }
        }
    }

    @JvmOverloads
    fun <S : Any> loadModService(service: Class<S>, loader: ClassLoader = classLoader): Map<PlatformMod, List<ServiceLoader.Provider<S>>> {
        return allMods.associateWith { mod ->
            mod.findResource(KtServiceLoader.PREFIX + service.name)?.use { file ->
                KtServiceLoader.load(service, file.reader().readAllLines().filter { it.isNotBlank() }, loader)
            } ?: listOf()
        }.filterValues { !it.isEmpty() }
    }

    @JvmOverloads
    fun <S : Any> loadModEntrypoint(key: String, entrypoint: Class<S>, loader: ClassLoader = classLoader): Map<PlatformMod, List<ServiceLoader.Provider<S>>> {
        return entrypoints.mapValues { (mod, entrypoints) ->
            entrypoints[key]?.let { classes -> KtServiceLoader.load(entrypoint, classes, loader) } ?: listOf()
        }.filterValues { !it.isEmpty() }
    }

    fun getModsThatHaveClass(className: String) = getModsThatHaveResource(className.replace('.', '/') + ".class")

    fun getModThatHasClass(className: String): PlatformMod? {
        val className = className.replace('/', '.')
        val mods = getModsThatHaveClass(className)
        return when (mods.size) {
            0 -> null
            1 -> mods.single()
            else -> {
                LOG.warn("Class $className can be found in multiple mods $mods, this should never happen. Assuming ${mods.first()}.")
                mods.first()
            }
        }
    }

    abstract fun getModsThatHaveResource(path: String): List<PlatformMod>

    fun loadBigShotMetadata() {
        LOG.info("Loading big shot mod metadata")

        for (mod in allMods) {
            try {
                mod.bigShotData?.let { data ->
                    entrypoints[mod] = data.entrypoints ?: mapOf()
                    data.classTweaker?.let {
                        (mod.findResource(it) ?: throw FileNotFoundException("Mod '$mod' is missing class tweaker ${data.classTweaker}")).use { file ->
                            val tweaker = ClassTweaker.newInstance()
                            ClassTweakerReader.create(tweaker).read(file.bufferedReader())
                            ClassTweakerTransform.CLASS_TWEAKERS.add(tweaker)
                        }
                    }
                }
            } catch (t: Throwable) {
                LOG.error("Error while loading big shot mod metadata for $mod", t)
            }
        }
    }
}