package ru.benos.cmditems.client

import com.google.gson.JsonParser
import kotlinx.serialization.json.Json
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.packs.resources.ResourceManager
import ru.benos.cmditems.client.CmditemsClient.Companion.LOGGER
import ru.benos.cmditems.client.CmditemsClient.Companion.MODID
import java.io.InputStreamReader

object Resources {
    var models: MutableMap<String, String> = mutableMapOf()
        private set

    var displays: MutableMap<String, CmdItemDisplay.DisplaySettings> = mutableMapOf()
        private set

    fun loadModels(resourceManager: ResourceManager) {
        // Get resource //
        val resource = ResourceLocation.tryBuild(MODID, "models.json") ?: return
        val stream = resourceManager.getResource(resource).orElse(null)?.open() ?: return
        val json = JsonParser.parseReader(InputStreamReader(stream)).asJsonObject

        // Clearing //
        models.clear()

        // Add models from Json //
        json.entrySet().forEach { models[it.key] = it.value.asString }

        // Print results //
        LOGGER.info(
            buildString {
                appendLine()
                appendLine("=== [ CMDITEMS | MODELS ] ===")
                models.entries.forEach { (key, value) ->
                    appendLine(String.format("%8s = %s", key, value))
                }
                appendLine("=============================")
            }
        )
    }

    fun loadDisplays(resourceManager: ResourceManager) {
        val json = Json { ignoreUnknownKeys = true }

        models.forEach { (_, value) ->
            val resource = ResourceLocation.tryBuild(MODID, "displays/$value.display.json") ?: return@forEach
            val stream = resourceManager.getResource(resource).orElse(null)?.open()

            LOGGER.info("Trying load resource: $resource | ${!resourceManager.getResource(resource).isEmpty}")

            val settings = try {
                if (stream == null) {
                    LOGGER.info("No display config found for model '$value', using default.")
                    CmdItemDisplay.defaultDisplaySettings()
                } else {
                    stream.use {
                        val reader = InputStreamReader(it)
                        val parsed = json.decodeFromString(
                            CmdItemDisplay.DisplaySettings.serializer(),
                            reader.readText()
                        )
                        parsed.withDefaultsFilled()
                    }
                }
            } catch (e: Exception) {
                LOGGER.warning("Failed to load display setting for model [$value], using default.")
                CmdItemDisplay.defaultDisplaySettings()
            }

            displays[value] = settings

            LOGGER.info(
                buildString {
                    appendLine()
                    appendLine("=== [ CMDITEMS | DISPLAYS SETTINGS ] ===")
                    displays.entries.forEach { (key, value) ->
                        val serialized = json.encodeToString(CmdItemDisplay.DisplaySettings.serializer(), value)
                        appendLine("$key = $serialized")
                    }
                    appendLine("========================================")
                }
            )
        }
    }

    private fun CmdItemDisplay.DisplaySettings.withDefaultsFilled(): CmdItemDisplay.DisplaySettings {
        fun CmdItemDisplay.DisplayInfo?.orDefault(): CmdItemDisplay.DisplayInfo = this ?: CmdItemDisplay.putDisplayInfo()

        fun CmdItemDisplay.HandsDisplayType?.fill(): CmdItemDisplay.HandsDisplayType = CmdItemDisplay.HandsDisplayType(
            leftHand = this?.leftHand.orDefault(),
            rightHand = this?.rightHand.orDefault()
        )

        val filledHands = CmdItemDisplay.HandsDisplay(
            firstPerson = hands.firstPerson.fill(),
            thirdPerson = hands.thirdPerson.fill()
        )

        val filledHead = this.head.orDefault()

        val filledGui = this.gui.orDefault()

        val filledFacings = world.itemFrame.facings.mapValues { (_, rotations) ->
            CmdItemDisplay.ItemFrameDisplayRotation(
                rotation = (0..7).associateWith { i ->
                    rotations.rotation[i].orDefault()
                }
            )
        }

        val filledWorld = CmdItemDisplay.WorldDisplay(
            ground = world.ground.orDefault(),
            itemFrame = CmdItemDisplay.ItemFrameDisplay(facings = filledFacings)
        )

        return CmdItemDisplay.DisplaySettings(
            hands = filledHands,
            head = filledHead,
            gui = filledGui,
            world = filledWorld
        )
    }
}