package net.typho.big_shot.plugin.ext

import org.gradle.api.provider.Property
import javax.inject.Inject

abstract class BigShotConfigExtension @Inject constructor() {
    abstract val configClass: Property<String>

    init {
        configClass.convention("Config")
    }
}