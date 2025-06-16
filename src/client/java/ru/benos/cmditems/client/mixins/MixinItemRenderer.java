package ru.benos.cmditems.client.mixins;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomModelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.benos.cmditems.client.CmdItem;
import ru.benos.cmditems.client.CmdItemRenderer;
import ru.benos.cmditems.client.CmdRegister;
import ru.benos.cmditems.client.Resources;
import software.bernie.geckolib.renderer.GeoItemRenderer;

import java.util.Objects;

@Mixin(ItemRenderer.class)
public class MixinItemRenderer {
    @Unique
    private static final CmdItem item = CmdRegister.INSTANCE.getCMD_ITEM();

    @Inject(method = "renderItem", at = @At("HEAD"), cancellable = true)
    public void onRenderItem(
            ItemStack itemStack,
            ItemDisplayContext displayContext,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight,
            int packedOverlay,
            BakedModel model,
            boolean renderOpenBundle,
            CallbackInfo ci
    ) {
        CustomModelData customModelData = itemStack.getComponents().get(DataComponents.CUSTOM_MODEL_DATA);

        if(customModelData != null) {
            String modelId = Resources.INSTANCE.getModels().get(customModelData.value()+"");

            if(modelId != null) {
                ItemStack proxyStack = new ItemStack(item);
                GeoItemRenderer<CmdItem> renderer = new CmdItemRenderer(item.getGeoModel(customModelData.value()));

                renderer.renderByItem(proxyStack, displayContext, poseStack, bufferSource, packedLight, packedOverlay);

                ci.cancel();
            }
        }
    }
}