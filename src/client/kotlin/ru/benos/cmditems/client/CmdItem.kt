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

    override fun getRenderType(animatable: CmdItem?, texture: ResourceLocation?): RenderType? {
        return super.getRenderType(animatable, texture)
    }

    fun getDisplaySettings(): CmdItemDisplay.DisplaySettings = Resources.displays[id] ?: CmdItemDisplay.defaultDisplaySettings()
}

class CmdItemRenderer(
    geoModel: GeoModel<CmdItem>,
    val displaySettings: CmdItemDisplay.DisplaySettings
): GeoItemRenderer<CmdItem>(geoModel) {
    override fun renderByItem(
        stack: ItemStack,
        transformType: ItemDisplayContext,
        poseStack: PoseStack,
        bufferSource: MultiBufferSource,
        packedLight: Int,
        packedOverlay: Int
    ) {
        super.renderByItem(stack, transformType, poseStack, bufferSource, packedLight, packedOverlay)

        val transform: (List<Double>, List<Double>, List<Double>) -> Unit = { t, r, s ->
            poseStack.apply {
                translate(t[0], t[1], t[2])
                mulPose(Axis.XP.rotationDegrees(r[0].toFloat())); mulPose(Axis.YP.rotationDegrees(r[1].toFloat())); mulPose(Axis.ZP.rotationDegrees(r[2].toFloat()))
                scale(s[0].toFloat(), s[1].toFloat(), s[2].toFloat())
            }
        }

        when(transformType) {
            ItemDisplayContext.FIRST_PERSON_LEFT_HAND -> transform(
                displaySettings.hands.firstPerson.leftHand.transform,
                displaySettings.hands.firstPerson.leftHand.rotation,
                displaySettings.hands.firstPerson.leftHand.scale
            )
            ItemDisplayContext.FIRST_PERSON_RIGHT_HAND -> transform(
                displaySettings.hands.firstPerson.rightHand.transform,
                displaySettings.hands.firstPerson.rightHand.rotation,
                displaySettings.hands.firstPerson.rightHand.scale
            )

            ItemDisplayContext.THIRD_PERSON_LEFT_HAND -> transform(
                displaySettings.hands.firstPerson.leftHand.transform,
                displaySettings.hands.firstPerson.leftHand.rotation,
                displaySettings.hands.firstPerson.leftHand.scale
            )
            ItemDisplayContext.THIRD_PERSON_RIGHT_HAND -> transform(
                displaySettings.hands.thirdPerson.rightHand.transform,
                displaySettings.hands.thirdPerson.rightHand.rotation,
                displaySettings.hands.thirdPerson.rightHand.scale
            )

            ItemDisplayContext.HEAD -> transform(
                displaySettings.head.transform,
                displaySettings.head.rotation,
                displaySettings.head.scale
            )

            ItemDisplayContext.GUI -> transform(
                displaySettings.gui.transform,
                displaySettings.gui.rotation,
                displaySettings.gui.scale
            )

            ItemDisplayContext.GROUND -> transform(
                displaySettings.world.ground.transform,
                displaySettings.world.ground.rotation,
                displaySettings.world.ground.scale
            )

            else -> transform(
                listOf(0.0, 0.0, 0.0),
                listOf(0.0, 0.0, 0.0),
                listOf(1.0, 1.0, 1.0)
            )
        }
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
        val itemRendererCache: GeoItemRenderer<CmdItem>
    )

    fun getCache(customModelData: Int, model: String): CmdItemCache {
        val item = CmdRegister.CMD_ITEM
        val displaySettings = Resources.displays[model] ?: CmdItemDisplay.defaultDisplaySettings()

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
        newItemRendererCache: GeoItemRenderer<CmdItem>
    ) { cache[customModelData] = CmdItemCache(model, newItemStack, newAnimatableInstanceCache, newItemRendererCache) }
}