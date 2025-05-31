package me.jacksonhoggard.holoframes.client.mixin;

import me.jacksonhoggard.holoframes.client.ItemFrameEntityRenderStateAccessor;
import me.jacksonhoggard.holoframes.client.HoloFrameRenderer;
import me.jacksonhoggard.holoframes.ItemFrameEntityMixinAccess;
import net.minecraft.client.render.*;
import net.minecraft.client.render.entity.ItemFrameEntityRenderer;
import net.minecraft.client.render.entity.state.ItemFrameEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.decoration.ItemFrameEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemFrameEntityRenderer.class)
public class ItemFrameEntityRendererMixin<T extends ItemFrameEntity> {
    @Inject(method = "render(Lnet/minecraft/client/render/entity/state/ItemFrameEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
            at = @At("RETURN"))
    private void onRender(ItemFrameEntityRenderState itemFrameEntityRenderState, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo ci) {
        String holoFile = ((ItemFrameEntityRenderStateAccessor) itemFrameEntityRenderState).holoFrames$getModelFile();
        if (holoFile != null && !holoFile.isEmpty()) {
            HoloFrameRenderer.renderHologram(itemFrameEntityRenderState, holoFile, matrixStack, vertexConsumerProvider);
        }
    }

    @Inject(method = "updateRenderState(Lnet/minecraft/entity/decoration/ItemFrameEntity;Lnet/minecraft/client/render/entity/state/ItemFrameEntityRenderState;F)V",
            at = @At("TAIL"))
    private void onUpdateRenderState(T itemFrameEntity, ItemFrameEntityRenderState itemFrameEntityRenderState, float f, CallbackInfo ci) {
        String holoFile = ((ItemFrameEntityMixinAccess) itemFrameEntity).holoFrames$getModelFile();
        int hologramRotation = ((ItemFrameEntityMixinAccess) itemFrameEntity).holoFrames$getHologramRotation();
        if (holoFile != null && !holoFile.isEmpty()) {
            ((ItemFrameEntityRenderStateAccessor) itemFrameEntityRenderState).holoFrames$setModelFile(holoFile);
            ((ItemFrameEntityRenderStateAccessor) itemFrameEntityRenderState).holoFrames$setHologramRotation(hologramRotation);
        } else {
            ((ItemFrameEntityRenderStateAccessor) itemFrameEntityRenderState).holoFrames$setModelFile("");
            ((ItemFrameEntityRenderStateAccessor) itemFrameEntityRenderState).holoFrames$setHologramRotation(0);
        }
    }
}
