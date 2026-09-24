package net.typho.big_shot.api

import net.typho.big_shot.agent.TestInterface

object TestImpl : TestInterface {
    override fun abc() {
        println("yay service!")
    }
}