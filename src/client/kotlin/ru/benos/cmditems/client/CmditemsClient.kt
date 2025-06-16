package ru.benos.cmditems.client

import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.resource.ResourceManagerHelper
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.packs.PackType
import net.minecraft.server.packs.resources.ResourceManager
import org.apache.commons.logging.impl.Log4JLogger
import java.util.logging.Logger

class CmditemsClient : ClientModInitializer {
    companion object {
        const val MODID = "cmditems"
        val LOGGER = Logger.getLogger(MODID)

        // Utils //
        val String.rl: ResourceLocation get() = ResourceLocation.parse(this)
    }

    override fun onInitializeClient() {
        // Logging //
        LOGGER.info("Loading [CMDItems | CustomModelDataItems] mod...")

        // Init Item //
        CmdRegister.CMD_ITEM

        // Reload resource manager helper //
        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(
            object: SimpleSynchronousResourceReloadListener {
                override fun getFabricId(): ResourceLocation =
                    "$MODID:resources".rl

                override fun onResourceManagerReload(resourceManager: ResourceManager) =
                    Resources.loadModels(resourceManager)
            }
        )
    }
}
