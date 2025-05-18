package me.jacksonhoggard.splatframes.client.mixin;

import me.jacksonhoggard.splatframes.client.ItemFrameEntityRenderStateAccessor;
import me.jacksonhoggard.splatframes.client.SplatFrameRenderer;
import me.jacksonhoggard.splatframes.ItemFrameEntityMixinAccess;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.ItemFrameEntityRenderer;
import net.minecraft.client.render.entity.state.ItemFrameEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.decoration.ItemFrameEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemFrameEntityRenderer.class)
public class ItemFrameEntityRendererMixin<T extends ItemFrameEntity> extends EntityRenderer<T, ItemFrameEntityRenderState> {

    protected ItemFrameEntityRendererMixin(EntityRendererFactory.Context context) {
        super(context);
    }

    @Inject(method = "render(Lnet/minecraft/client/render/entity/state/ItemFrameEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
            at = @At("HEAD"))
    private void onRender(ItemFrameEntityRenderState itemFrameEntityRenderState, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo ci) {
        String splatFile = ((ItemFrameEntityRenderStateAccessor) itemFrameEntityRenderState).splatFrames$getSplatFile();
        if (splatFile != null && !splatFile.isEmpty()) {
            SplatFrameRenderer.renderHologram(itemFrameEntityRenderState, splatFile, matrixStack, vertexConsumerProvider, i);
        }
    }

    @Override
    public ItemFrameEntityRenderState createRenderState() {
        return new ItemFrameEntityRenderState();
    }

    @Inject(method = "updateRenderState(Lnet/minecraft/entity/decoration/ItemFrameEntity;Lnet/minecraft/client/render/entity/state/ItemFrameEntityRenderState;F)V",
            at = @At("TAIL"))
    private void onUpdateRenderState(T itemFrameEntity, ItemFrameEntityRenderState itemFrameEntityRenderState, float f, CallbackInfo ci) {
        String splatFile = ((ItemFrameEntityMixinAccess) itemFrameEntity).splatFrames$getSplatFile();
        if (splatFile != null && !splatFile.isEmpty()) {
            ((ItemFrameEntityRenderStateAccessor) itemFrameEntityRenderState).splatFrames$setSplatFile(splatFile);
        }
    }
}
