package ru.benos.cmditems.client

import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.blaze3d.vertex.PoseStack.Pose
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
import org.joml.Quaternionf
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
        val display = Resources.displays[id]

        if(display != null) {
            return when(display.renderType) {
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
    private val display: CmdItemDisplay.DisplayInfo
) : GeoItemRenderer<CmdItem>(geoModel) {
    override fun renderByItem(
        stack: ItemStack,
        transformType: ItemDisplayContext,
        poseStack: PoseStack,
        bufferSource: MultiBufferSource,
        packedLight: Int,
        packedOverlay: Int
    ) {
        poseStack.pushPose()
        val transform: (Boolean, List<Double>, List<Double>, List<Double>) -> Unit = { leftHand, t, r, s ->
            poseStack.apply {
                translate(
                    t[0] / 16.0,
                    t[1] / 16.0,
                    t[2] / 16.0
                )

                mulPose(Quaternionf().rotateXYZ(
                    Math.toRadians(r[0]).toFloat(),
                    Math.toRadians(r[1]).toFloat(),
                    Math.toRadians(r[2]).toFloat()
                ))

                scale(
                    s[0].toFloat(),
                    s[1].toFloat(),
                    s[2].toFloat()
                )
            }
        }

        when (transformType) {
            ItemDisplayContext.FIRST_PERSON_LEFT_HAND -> transform(true,
                display.display.firstPersonLeftHand.transition,
                display.display.firstPersonLeftHand.rotation,
                display.display.firstPersonLeftHand.scale
            )
            ItemDisplayContext.FIRST_PERSON_RIGHT_HAND -> transform(false,
                display.display.firstPersonRightHand.transition,
                display.display.firstPersonRightHand.rotation,
                display.display.firstPersonRightHand.scale
            )
            ItemDisplayContext.THIRD_PERSON_LEFT_HAND -> transform(true,
                display.display.thirdPersonLeftHand.transition,
                display.display.thirdPersonLeftHand.rotation,
                display.display.thirdPersonLeftHand.scale
            )
            ItemDisplayContext.THIRD_PERSON_RIGHT_HAND -> transform(false,
                display.display.thirdPersonRightHand.transition,
                display.display.thirdPersonRightHand.rotation,
                display.display.thirdPersonRightHand.scale
            )
            ItemDisplayContext.HEAD -> transform(false,
                display.display.head.transition,
                display.display.head.rotation,
                display.display.head.scale
            )
            ItemDisplayContext.GROUND -> transform(false,
                display.display.ground.transition,
                display.display.ground.rotation,
                display.display.ground.scale
            )
            ItemDisplayContext.FIXED -> transform(false,
                display.display.fixed.transition,
                display.display.fixed.rotation,
                display.display.fixed.scale
            )
            ItemDisplayContext.GUI -> transform(false,
                display.display.gui.transition,
                display.display.gui.rotation,
                display.display.gui.scale
            )
            else -> transform(false,
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
                val display = Resources.displays[model] ?: CmdItemDisplay.DisplayInfo()

                return cache[customModelData] ?: CmdItemCache(
                    model,
                    ItemStack(item),
                    GeckoLibUtil.createInstanceCache(item),
                    CmdItemRenderer(item.getGeoModel(customModelData), display)
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