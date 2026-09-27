package net.typho.test_mod

import net.typho.big_shot.agent.PlatformMod
import net.typho.big_shot.agent.entrypoint.EntrypointInput
import java.lang.instrument.Instrumentation

class TestModEarly {
    @EntrypointInput
    lateinit var mod: PlatformMod
    @EntrypointInput
    lateinit var instrumentation: Instrumentation

    init {
        println("Loading test mod ${mod.version}")
        println("Instrumentation test ${instrumentation.getObjectSize(this)}")
    }
}