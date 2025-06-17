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
        // Очистка кеша //
        displays.clear()

        val json = Json {
            ignoreUnknownKeys = true
            isLenient = true
            allowStructuredMapKeys = true
        }

        models.forEach { (_, modelName) ->
            val resource = ResourceLocation.tryBuild(MODID, "displays/$modelName.display.json") ?: return@forEach
            val resourceOptional = resourceManager.getResource(resource)

            val settings = try {
                val stream = resourceOptional.orElse(null)?.open()

                if (stream == null) {
                    LOGGER.info("No display config found for model '$modelName', using default.")
                    CmdItemDisplay.DisplaySettings()
                } else {
                    stream.use { input ->
                        val reader = InputStreamReader(input)
                        val parsed = json.decodeFromString(
                            CmdItemDisplay.DisplaySettings.serializer(),
                            reader.readText()
                        )
                        parsed.withDefaultsFilled()
                    }
                }
            } catch (e: Exception) {
                LOGGER.warning("Failed to load display setting for model [$modelName], using default. Error: ${e.message}")
                CmdItemDisplay.DisplaySettings()
            }

            displays[modelName] = settings
        }

        if (displays.isNotEmpty()) {
            val logOutput = buildString {
                appendLine()
                appendLine("=== [ CMDITEMS | DISPLAYS SETTINGS ] ===")
                displays.entries.forEach { (key, value) ->
                    val serialized = json.encodeToString(CmdItemDisplay.DisplaySettings.serializer(), value)
                    appendLine("$key = $serialized")
                }
                appendLine("========================================")
            }
            LOGGER.info(logOutput)
        }
    }

    private fun CmdItemDisplay.DisplaySettings.withDefaultsFilled(): CmdItemDisplay.DisplaySettings {
        fun CmdItemDisplay.DisplayInfo?.orDefault(): CmdItemDisplay.DisplayInfo = this ?: CmdItemDisplay.DisplayInfo()

        return CmdItemDisplay.DisplaySettings(
            firstPersonLeftHand = firstPersonLeftHand.orDefault(),
            firstPersonRightHand = firstPersonRightHand.orDefault(),
            thirdPersonLeftHand = thirdPersonLeftHand.orDefault(),
            thirdPersonRightHand = thirdPersonRightHand.orDefault(),
            head = head.orDefault(),
            ground = ground.orDefault(),
            fixed = fixed.orDefault(),
            gui = gui.orDefault(),
            renderType = renderType
        )
    }
}