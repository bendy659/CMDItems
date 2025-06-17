package ru.benos.cmditems.client

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

object CmdItemDisplay {
    @Serializable
    data class DisplaySettings(
        @SerialName("first_person_left_hand") val firstPersonLeftHand: DisplayInfo = DisplayInfo(),
        @SerialName("first_person_right_hand") val firstPersonRightHand: DisplayInfo = DisplayInfo(),
        @SerialName("third_person_left_hand") val thirdPersonLeftHand: DisplayInfo = DisplayInfo(),
        @SerialName("third_person_right_hand") val thirdPersonRightHand: DisplayInfo = DisplayInfo(),
        @SerialName("head") val head: DisplayInfo = DisplayInfo(),
        @SerialName("ground") val ground: DisplayInfo = DisplayInfo(),
        @SerialName("fixed") val fixed: DisplayInfo = DisplayInfo(),
        @SerialName("gui") val gui: DisplayInfo = DisplayInfo(),
        @SerialName("render_type") val renderType: RenderType = RenderType.DEFAULT
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
    data class DisplayInfo(
        @SerialName("transition") val transition: List<Double> = listOf(0.0, 0.0, 0.0),
        @SerialName("rotation") val rotation: List<Double> = listOf(0.0, 0.0, 0.0),
        @SerialName("scale") val scale: List<Double> = listOf(1.0, 1.0, 1.0)
    )
}