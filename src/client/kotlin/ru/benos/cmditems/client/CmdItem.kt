package ru.benos.cmditems.client

import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.math.Axis
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.RenderType
import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemDisplayContext
import net.minecraft.world.item.ItemStack
import ru.benos.cmditems.client.CmditemsClient.Companion.MODID
import ru.benos.cmditems.client.CmditemsClient.Companion.rl
import software.bernie.geckolib.animatable.GeoItem
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache
import software.bernie.geckolib.animation.AnimatableManager
import software.bernie.geckolib.animation.AnimationController
import software.bernie.geckolib.animation.RawAnimation
import software.bernie.geckolib.model.GeoModel
import software.bernie.geckolib.renderer.GeoItemRenderer
import software.bernie.geckolib.renderer.GeoRenderer
import software.bernie.geckolib.util.GeckoLibUtil
import software.bernie.geckolib.util.RenderUtil

class CmdItem(props: Properties): Item(props), GeoItem {
    private var cache: AnimatableInstanceCache? = null

    fun setAnimatableInstanceCache(newAnimatableInstanceCache: AnimatableInstanceCache) {
        cache = newAnimatableInstanceCache
    }

    fun getGeoModel(customModelData: Int): GeoModel<CmdItem> {
        val model = Resources.models["" + customModelData] ?: "error"

        return CmdItemModel(model)
    }

    override fun registerControllers(p0: AnimatableManager.ControllerRegistrar) {
        p0.add(AnimationController(this) {
            it.setAndContinue(RawAnimation.begin().thenLoop("basic"))
        })
    }

    override fun getAnimatableInstanceCache(): AnimatableInstanceCache? = cache

    override fun getTick(itemStack: Any?): Double = RenderUtil.getCurrentTick()
}

class CmdItemModel(private val id: String): GeoModel<CmdItem>() {
    override fun getModelResource(p0: CmdItem?, p1: GeoRenderer<CmdItem>?): ResourceLocation = "$MODID:geo/$id.geo.json".rl

    override fun getTextureResource(p0: CmdItem?, p1: GeoRenderer<CmdItem>?): ResourceLocation = "$MODID:textures/item/$id.png".rl

    override fun getAnimationResource(p0: CmdItem?): ResourceLocation = "$MODID:animations/$id.animation.json".rl

    override fun getRenderType(animatable: CmdItem?, texture: ResourceLocation): RenderType? {
        val displaySettings = Resources.displays[id]

        if(displaySettings != null) {
            return when(displaySettings.renderType) {
                CmdItemDisplay.RenderType.DEFAULT -> super.getRenderType(animatable, texture)
                CmdItemDisplay.RenderType.GLOW -> RenderType.entityTranslucentEmissive(texture)
                CmdItemDisplay.RenderType.LIGHT -> super.getRenderType(animatable, texture) // TODO
                CmdItemDisplay.RenderType.GLOW_LIGHT -> RenderType.entityTranslucentEmissive(texture)
            }
        } else return super.getRenderType(animatable, texture)
    }
}

class CmdItemRenderer(
    geoModel: GeoModel<CmdItem>,
    private val displaySettings: CmdItemDisplay.DisplaySettings
): GeoItemRenderer<CmdItem>(geoModel) {
    override fun renderByItem(
        stack: ItemStack,
        transformType: ItemDisplayContext,
        poseStack: PoseStack,
        bufferSource: MultiBufferSource,
        packedLight: Int,
        packedOverlay: Int
    ) {
        poseStack.pushPose()
        val transform: (List<Double>, List<Double>, List<Double>) -> Unit = { t, r, s ->
            poseStack.apply {
                mulPose(Axis.XP.rotationDegrees(r[0].toFloat()))
                mulPose(Axis.YP.rotationDegrees(r[2].toFloat()))
                mulPose(Axis.ZP.rotationDegrees(r[1].toFloat()))

                scale(s[0].toFloat(), s[2].toFloat(), s[1].toFloat())

                translate(t[0], t[2], t[1])
            }
        }

        when(transformType) {
            ItemDisplayContext.FIRST_PERSON_LEFT_HAND -> transform(
                displaySettings.firstPersonLeftHand.transition,
                displaySettings.firstPersonLeftHand.rotation,
                displaySettings.firstPersonLeftHand.scale
            )
            ItemDisplayContext.FIRST_PERSON_RIGHT_HAND -> transform(
                displaySettings.firstPersonRightHand.transition,
                displaySettings.firstPersonRightHand.rotation,
                displaySettings.firstPersonRightHand.scale
            )

            ItemDisplayContext.THIRD_PERSON_LEFT_HAND -> transform(
                displaySettings.thirdPersonLeftHand.transition,
                displaySettings.thirdPersonLeftHand.rotation,
                displaySettings.thirdPersonLeftHand.scale
            )
            ItemDisplayContext.THIRD_PERSON_RIGHT_HAND -> transform(
                displaySettings.thirdPersonRightHand.transition,
                displaySettings.thirdPersonRightHand.rotation,
                displaySettings.thirdPersonRightHand.scale
            )

            ItemDisplayContext.HEAD -> transform(
                displaySettings.head.transition,
                displaySettings.head.rotation,
                displaySettings.head.scale
            )

            ItemDisplayContext.GROUND -> transform(
                displaySettings.ground.transition,
                displaySettings.ground.rotation,
                displaySettings.ground.scale
            )

            ItemDisplayContext.FIXED ->  transform(
                displaySettings.fixed.transition,
                displaySettings.fixed.rotation,
                displaySettings.fixed.scale
            )

            ItemDisplayContext.GUI -> transform(
                displaySettings.gui.transition,
                displaySettings.gui.rotation,
                displaySettings.gui.scale
            )

            else -> transform(
                listOf(0.0, 0.0, 0.0),
                listOf(0.0, 0.0, 0.0),
                listOf(1.0, 1.0, 1.0)
            )
        }

        super.renderByItem(stack, transformType, poseStack, bufferSource, packedLight, packedOverlay)
        poseStack.popPose()
    }
}

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

object CmdItemCache {
    private var cache: MutableMap<Int, CmdItemCache> = mutableMapOf()

    data class CmdItemCache(
        val model: String,
        val itemStack: ItemStack,
        val animatableInstanceCache: AnimatableInstanceCache,
        val itemRendererCache: CmdItemRenderer
    )

    fun clearCache() = cache.clear()

    fun getCache(customModelData: Int, model: String): CmdItemCache {
        val item = CmdRegister.CMD_ITEM
        val displaySettings = Resources.displays[model] ?: CmdItemDisplay.DisplaySettings()

        return cache[customModelData] ?: CmdItemCache(
            model,
            ItemStack(item),
            GeckoLibUtil.createInstanceCache(item),
            CmdItemRenderer(item.getGeoModel(customModelData), displaySettings)
        )
    }

    fun putCache(
        model: String,
        customModelData: Int,
        newItemStack: ItemStack,
        newAnimatableInstanceCache: AnimatableInstanceCache,
        newItemRendererCache: CmdItemRenderer
    ) { cache[customModelData] = CmdItemCache(model, newItemStack, newAnimatableInstanceCache, newItemRendererCache) }
}