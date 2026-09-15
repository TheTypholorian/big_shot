package net.typho.big_shot.loader.mixin_util

import org.jetbrains.annotations.ApiStatus

sealed interface JumpHandle {
    fun jump()

    fun hasJumped(): Boolean

    interface Complex : JumpHandle {
        val numLocals: Int
        val numStack: Int

        /**
         * @see Jump.localsToModify
         */
        fun setLocal(index: Int, value: Any?)

        /**
         * @see Jump.shiftBeforeStack
         */
        fun setStack(index: Int, value: Any?)
    }

    @ApiStatus.Internal
    sealed class Impl : JumpHandle {
        private var jumped = false

        override fun jump() {
            jumped = true
        }

        override fun hasJumped() = jumped
    }

    @ApiStatus.Internal
    class ComplexImpl(
        numLocals: Int,
        numStack: Int
    ) : Impl(), Complex {
        private val locals = arrayOfNulls<Any>(numLocals)
        private val stack = arrayOfNulls<Any>(numStack)
        override val numLocals: Int
            get() = locals.size
        override val numStack: Int
            get() = stack.size

        override fun setLocal(index: Int, value: Any?) {
            locals[index] = value
        }

        override fun setStack(index: Int, value: Any?) {
            stack[index] = value
        }

        fun localObject(index: Int) = locals[index]

        fun localInt(index: Int) = localObject(index) as Int

        fun localLong(index: Int) = localObject(index) as Long

        fun localFloat(index: Int) = localObject(index) as Float

        fun localDouble(index: Int) = localObject(index) as Double

        fun stackObject(index: Int) = stack[index]

        fun stackInt(index: Int) = stackObject(index) as Int

        fun stackLong(index: Int) = stackObject(index) as Long

        fun stackFloat(index: Int) = stackObject(index) as Float

        fun stackDouble(index: Int) = stackObject(index) as Double
    }
}