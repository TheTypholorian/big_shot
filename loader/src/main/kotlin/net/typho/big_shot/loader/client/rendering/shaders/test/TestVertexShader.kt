package net.typho.big_shot.loader.client.rendering.shaders.test

import net.typho.big_shot.loader.client.rendering.shaders.reflect.JavaShader
import org.joml.Vector3d
import org.joml.Vector3dc
import org.joml.Vector3f
import org.joml.Vector3fc
import kotlin.io.path.Path

class TestVertexShader : JavaShader.Vertex() {
    @Input
    @Location(0)
    @JvmField
    var pos: Vector3fc = Vector3f()
    @Input
    @Location(1)
    @JvmField
    var pos2: Vector3fc = Vector3f()

    @Output
    @Location(0)
    @JvmField
    var outPos: Vector3fc = Vector3f()

    override fun main() {
        outPos = Vector3f(pos).mulAdd(10f, Vector3f(13f, 20f, 19f))
    }
}