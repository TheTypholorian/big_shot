package net.typho.big_shot.loader.mixin_util.jump

class JumpHandle {
    private var jumped = false

    fun jump() {
        jumped = true
    }

    fun hasJumped() = jumped
}