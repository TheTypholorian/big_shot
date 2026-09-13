package net.typho.big_shot.loader.mixin_util.switches

import net.typho.asm_util.ASMUtil.iterateSlice
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.AbstractInsnNode
import org.objectweb.asm.tree.AnnotationNode
import org.objectweb.asm.tree.InsnList
import org.objectweb.asm.tree.JumpInsnNode
import org.objectweb.asm.tree.LabelNode
import org.objectweb.asm.tree.LookupSwitchInsnNode
import org.objectweb.asm.tree.MethodNode
import org.objectweb.asm.tree.TableSwitchInsnNode
import org.spongepowered.asm.mixin.injection.code.Injector
import org.spongepowered.asm.mixin.injection.struct.InjectionInfo
import org.spongepowered.asm.mixin.injection.struct.InjectionNodes
import org.spongepowered.asm.mixin.injection.struct.Target
import org.spongepowered.asm.mixin.transformer.MixinTargetContext
import org.spongepowered.asm.util.Annotations

@InjectionInfo.AnnotationType(AddSwitchBranch::class)
@InjectionInfo.HandlerPrefix("switch_branch") // idk if I actually need this
class AddSwitchBranchInjectionInfo : InjectionInfo {
    constructor(mixin: MixinTargetContext?, method: MethodNode?, annotation: AnnotationNode?) : super(
        mixin,
        method,
        annotation
    )

    constructor(mixin: MixinTargetContext?, method: MethodNode?, annotation: AnnotationNode?, atKey: String?) : super(
        mixin,
        method,
        annotation,
        atKey
    )

    override fun parseInjector(anno: AnnotationNode): Injector {
        var value = Annotations.getValue<Int>(anno, "intValue")
        val enumValue = Annotations.getValue<String>(anno, "enumValue")

        if (!enumValue.isNullOrEmpty()) {
            if (value != 0) {
                throw IllegalArgumentException("@AddSwitchBranch cannot specify both an int value and an enum value")
            }

            TODO("enum values")
        }

        return InjectorImpl(this, value)
    }

    class InjectorImpl(
        info: InjectionInfo,
        @JvmField
        val key: Int
    ) : Injector(info, "@AddSwitchBranch") {
        fun createLabel(
            instructions: InsnList,
            switch: AbstractInsnNode,
            default: LabelNode
        ): LabelNode {
            val thenJumpTo = (default.previous as? JumpInsnNode)?.label ?: default
            /*
            var thenJumpTo = default

            if (instructions.iterateSlice(setOf(switch), setOf(default)).none { (it as? JumpInsnNode)?.label === default }) {
                println(default.previous?.previous?.previous?.previous?.previous?.previous?.previous?.previous?.previous?.previous?.previous)
                println(default.previous?.previous?.previous?.previous?.previous?.previous?.previous?.previous?.previous?.previous)
                println(default.previous?.previous?.previous?.previous?.previous?.previous?.previous?.previous?.previous)
                println(default.previous?.previous?.previous?.previous?.previous?.previous?.previous?.previous)
                println(default.previous?.previous?.previous?.previous?.previous?.previous?.previous)
                println(default.previous?.previous?.previous?.previous?.previous?.previous)
                println(default.previous?.previous?.previous?.previous?.previous)
                println(default.previous?.previous?.previous?.previous)
                println(default.previous?.previous?.previous)
                println(default.previous?.previous)
                println(default.previous)
                thenJumpTo = (default.previous as? JumpInsnNode ?: throw IllegalStateException("Instruction before default branch of switch was ${default.previous}, not a jump")).label
            }
             */

            val label = LabelNode()
            val insns = InsnList()
            insns.add(label)
            invokeHandler(insns)
            insns.add(JumpInsnNode(Opcodes.GOTO, thenJumpTo))
            instructions.insert(switch, insns)
            return label
        }

        override fun inject(
            target: Target,
            node: InjectionNodes.InjectionNode
        ) {
            when (val switch = node.currentTarget) {
                is LookupSwitchInsnNode -> {
                    if (switch.keys.contains(key)) {
                        throw IllegalStateException("@AddSwitchBranch tried to add an existing entry to a lookup switch ($key)")
                    }

                    val index = if (key < switch.keys.first()) {
                        0
                    } else if (key > switch.keys.last()) {
                        switch.keys.size
                    } else {
                        val windows = switch.keys.windowed(2)
                        windows.mapIndexedNotNull { index, (a, b) ->
                            if (key > a && key < b) {
                                index + 1
                            } else {
                                null
                            }
                        }.first()
                    }

                    switch.keys.add(index, key)
                    switch.labels.add(index, createLabel(target.insns, switch, switch.dflt))
                }
                is TableSwitchInsnNode -> {
                    when (key) {
                        switch.min - 1 -> {
                            switch.min = key
                            switch.labels.addFirst(createLabel(target.insns, switch, switch.dflt))
                        }
                        switch.max + 1 -> {
                            switch.max = key
                            switch.labels.addLast(createLabel(target.insns, switch, switch.dflt))
                        }
                        else -> throw IllegalStateException("@AddSwitchBranch tried to add an invalid entry to a table switch (got $key, must be either ${switch.min - 1} or ${switch.max + 1})")
                    }
                }
                else -> throw IllegalArgumentException("@AddSwitchBranch must target a switch operation")
            }
        }
    }
}