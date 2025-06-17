package ru.benos.cmditems.client

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

object CmdItemDisplay {
    @Serializable
    data class DisplayInfo(
        @SerialName("display") val display: Display = Display(),
        @SerialName("render_type") val renderType: RenderType = RenderType.DEFAULT
    )

    @Serializable
    data class Display(
        @SerialName("firstperson_lefthand") val firstPersonLeftHand: Transform = Transform(),
        @SerialName("firstperson_righthand") val firstPersonRightHand: Transform = Transform(),
        @SerialName("thirdperson_lefthand") val thirdPersonLeftHand: Transform = Transform(),
        @SerialName("thirdperson_righthand") val thirdPersonRightHand: Transform = Transform(),
        @SerialName("head") val head: Transform = Transform(),
        @SerialName("ground") val ground: Transform = Transform(),
        @SerialName("fixed") val fixed: Transform = Transform(),
        @SerialName("gui") val gui: Transform = Transform()
    )

    @Serializable
    enum class RenderType {
        DEFAULT,
        GLOW,
        LIGHT,
        GLOW_LIGHT
    }

    // ==== //

    @Serializable
    data class Transform(
        @SerialName("transition") val transition: List<Double> = listOf(0.0, 0.0, 0.0),
        @SerialName("rotation") val rotation: List<Double> = listOf(0.0, 0.0, 0.0),
        @SerialName("scale") val scale: List<Double> = listOf(1.0, 1.0, 1.0)
    )
}