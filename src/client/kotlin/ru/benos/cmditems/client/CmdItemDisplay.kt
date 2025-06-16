package ru.benos.cmditems.client

import kotlinx.serialization.Serializable

object CmdItemDisplay {
    @Serializable
    data class DisplaySettings(
        val hands: HandsDisplay,
        val head: DisplayInfo,
        val gui: DisplayInfo,
        val world: WorldDisplay
    )

    @Serializable
    data class HandsDisplay(
        val firstPerson: HandsDisplayType,
        val thirdPerson: HandsDisplayType
    )

    @Serializable
    data class HandsDisplayType(
        val leftHand: DisplayInfo,
        val rightHand: DisplayInfo
    )

    @Serializable
    data class WorldDisplay(
        val ground: DisplayInfo,
        val itemFrame: ItemFrameDisplay
    )

    @Serializable
    data class ItemFrameDisplay(
        val facings: Map<String, ItemFrameDisplayRotation>
    )

    @Serializable
    data class ItemFrameDisplayRotation(
        val rotation: Map<Int, DisplayInfo>
    )

    // ==== //

    @Serializable
    data class DisplayInfo(
        val model: String?, // use other model
        val transform: List<Double>, // transformation [x, y, z]
        val rotation: List<Double>, // rotation [x, y, z]
        val scale: List<Double>, // scale [x, y, z]
        val renderType: String // renderType [ Default, Glow, PhysicLight ]
    )

    // ==== //

    fun defaultDisplaySettings(): DisplaySettings {
        return DisplaySettings(
            HandsDisplay(
                HandsDisplayType(
                    putDisplayInfo(),
                    putDisplayInfo()
                ),
                HandsDisplayType(
                    putDisplayInfo(),
                    putDisplayInfo()
                )
            ),
            putDisplayInfo(),
            putDisplayInfo(),
            WorldDisplay(
                putDisplayInfo(),
                ItemFrameDisplay(
                    emptyMap()
                )
            )
        )
    }
    
    fun putDisplayInfo(
        model: String? = null,
        transform: List<Double > = listOf(0.0, 0.0, 0.0),
        rotation: List<Double > = listOf(0.0, 0.0, 0.0),
        scale: List<Double> = listOf(1.0, 1.0, 1.0),
        renderType: String = "Default"
        
    ) = DisplayInfo(model, transform, rotation, scale, renderType)
}