package ru.benos.cmditems.client

import com.google.gson.JsonParser
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.packs.resources.ResourceManager
import ru.benos.cmditems.client.CmditemsClient.Companion.LOGGER
import ru.benos.cmditems.client.CmditemsClient.Companion.MODID
import java.io.InputStreamReader

object Resources {
    var models: MutableMap<String, String> = mutableMapOf()
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
}