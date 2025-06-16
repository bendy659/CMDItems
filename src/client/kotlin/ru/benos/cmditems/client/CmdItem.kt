package ru.benos.cmditems.client

import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer
import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.Item
import ru.benos.cmditems.client.CmditemsClient.Companion.MODID
import ru.benos.cmditems.client.CmditemsClient.Companion.rl
import software.bernie.geckolib.animatable.GeoItem
import software.bernie.geckolib.animatable.client.GeoRenderProvider
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache
import software.bernie.geckolib.animation.AnimatableManager
import software.bernie.geckolib.animation.AnimationController
import software.bernie.geckolib.animation.RawAnimation
import software.bernie.geckolib.model.GeoModel
import software.bernie.geckolib.renderer.GeoItemRenderer
import software.bernie.geckolib.renderer.GeoRenderer
import software.bernie.geckolib.util.GeckoLibUtil
import software.bernie.geckolib.util.RenderUtil
import java.util.function.Consumer

class CmdItem(props: Properties): Item(props), GeoItem {
    private val cache = GeckoLibUtil.createInstanceCache(this)

    fun getGeoModel(customModelData: Int): GeoModel<CmdItem> {
        val model = Resources.models["" + customModelData] ?: "error"

        return CmdItemModel(model)
    }

    override fun registerControllers(p0: AnimatableManager.ControllerRegistrar) {
        p0.add(AnimationController(this) {
            it.setAndContinue(RawAnimation.begin().thenLoop("basic"))
        })
    }

    override fun getAnimatableInstanceCache(): AnimatableInstanceCache = cache

    override fun getTick(itemStack: Any?): Double = RenderUtil.getCurrentTick()
}

class CmdItemModel(private val id: String): GeoModel<CmdItem>() {
    override fun getModelResource(p0: CmdItem?, p1: GeoRenderer<CmdItem>?): ResourceLocation = "$MODID:geo/$id.geo.json".rl

    override fun getTextureResource(p0: CmdItem?, p1: GeoRenderer<CmdItem>?): ResourceLocation = "$MODID:textures/item/$id.png".rl

    override fun getAnimationResource(p0: CmdItem?): ResourceLocation = "$MODID:animations/$id.animation.json".rl
}

class CmdItemRenderer(geoModel: GeoModel<CmdItem>): GeoItemRenderer<CmdItem>(geoModel)

object CmdRegister {
    val CMD_ITEM: CmdItem = Registry.register(
        BuiltInRegistries.ITEM,
        "cmd_item",
        CmdItem(
            Item.Properties()
                .useItemDescriptionPrefix()
                .setId(ResourceKey.create(Registries.ITEM, "cmd_item".rl))
        )
    )
}