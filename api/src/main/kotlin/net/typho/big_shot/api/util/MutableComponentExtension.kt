package net.typho.big_shot.api.util

import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.network.chat.Style
import net.minecraft.network.chat.TextColor
import java.awt.Color
import java.util.function.UnaryOperator

interface MutableComponentExtension {
    private fun self() = this as MutableComponent

    operator fun plus(text: String) = self().append(text)

    operator fun plus(component: Component) = self().append(component)

    operator fun times(style: UnaryOperator<Style>) = self().withStyle(style)

    operator fun times(style: Style) = self().withStyle(style)

    operator fun times(format: ChatFormatting) = self().withStyle(format)

    operator fun times(color: Color) = self().withColor(color.rgb)

    operator fun times(color: TextColor) = self().withColor(color)
}