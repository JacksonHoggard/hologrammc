package me.jacksonhoggard.holoframes.client.mixin;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.VertexFormat;
import me.jacksonhoggard.holoframes.Holoframes;
import me.jacksonhoggard.holoframes.client.ItemFrameEntityRenderStateAccessor;
import me.jacksonhoggard.holoframes.client.HoloFrameRenderer;
import me.jacksonhoggard.holoframes.ItemFrameEntityMixinAccess;
import net.minecraft.block.BlockState;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.render.*;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.entity.ItemFrameEntityRenderer;
import net.minecraft.client.render.entity.state.ItemFrameEntityRenderState;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.render.model.BlockModelPart;
import net.minecraft.client.render.model.BlockStateManagers;
import net.minecraft.client.render.model.BlockStateModel;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.TriState;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

import static net.minecraft.client.gl.RenderPipelines.ENTITY_SNIPPET;

@Mixin(ItemFrameEntityRenderer.class)
public class ItemFrameEntityRendererMixin<T extends ItemFrameEntity> {

    @Shadow
    @Final
    private BlockRenderManager blockRenderManager;

    @Unique
    private static final RenderPipeline QUADS = RenderPipelines.register(
            RenderPipeline.builder(ENTITY_SNIPPET)
                    .withLocation(Identifier.of(Holoframes.MOD_ID, "pipeline/quads"))
                    .withSampler("Sampler1")
                    .build()
    );

    @Unique
    private static final RenderLayer renderLayer = RenderLayer.of(
            "quads",
            1536,
            true,
            false,
            QUADS,
            RenderLayer.MultiPhaseParameters.builder().texture(new RenderPhase.Texture(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, TriState.FALSE, false))
                    .lightmap(RenderPhase.ENABLE_LIGHTMAP)
                    .overlay(RenderPhase.ENABLE_OVERLAY_COLOR)
                    .build(true)
    );


    @Inject(method = "render(Lnet/minecraft/client/render/entity/state/ItemFrameEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
            at = @At("HEAD"),
            cancellable = true)
    private void onRender(ItemFrameEntityRenderState itemFrameEntityRenderState, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo ci) {
        String holoFile = ((ItemFrameEntityRenderStateAccessor) itemFrameEntityRenderState).holoFrames$getModelFile();
        if (holoFile != null && !holoFile.isEmpty()) {
            Tessellator tessellator = Tessellator.getInstance();

            BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL);

            matrixStack.push();
            Direction direction = itemFrameEntityRenderState.facing;
            Vec3d vec3d = ((ItemFrameEntityRenderer<?>) (Object) this).getPositionOffset(itemFrameEntityRenderState);
            matrixStack.translate(-vec3d.getX(), -vec3d.getY(), -vec3d.getZ());
            matrixStack.translate((double)direction.getOffsetX() * (double)0.46875F, (double)direction.getOffsetY() * (double)0.46875F, (double)direction.getOffsetZ() * (double)0.46875F);
            float f;
            float g;
            if (direction.getAxis().isHorizontal()) {
                f = 0.0F;
                g = 180.0F - direction.getPositiveHorizontalDegrees();
            } else {
                f = (float)(-90 * direction.getDirection().offset());
                g = 180.0F;
            }

            matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(f));
            matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(g));
            if (!itemFrameEntityRenderState.invisible) {
                BlockState blockState = BlockStateManagers.getStateForItemFrame(itemFrameEntityRenderState.glow, itemFrameEntityRenderState.mapId != null);
                BlockStateModel blockStateModel = this.blockRenderManager.getModel(blockState);
                matrixStack.push();
                matrixStack.translate(-0.5F, -0.5F, -0.5F);
                for(BlockModelPart part : blockStateModel.getParts(Random.create(42L))) {
                    for(Direction d : Direction.values()) {
                        renderQuads(matrixStack.peek(), buffer, part.getQuads(d), i);
                    }
                    renderQuads(matrixStack.peek(), buffer, part.getQuads((Direction) null), i);
                }
                matrixStack.pop();
            }
            matrixStack.pop();
            BuiltBuffer builtBuffer = buffer.end();
            renderLayer.draw(builtBuffer);
            HoloFrameRenderer.renderHologram(itemFrameEntityRenderState, holoFile, matrixStack);
            ci.cancel();
        }
    }

    @Unique
    private static void renderQuads(MatrixStack.Entry entry, BufferBuilder buffer, List<BakedQuad> quads, int light) {
        for(BakedQuad bakedQuad : quads) {
            buffer.quad(entry, bakedQuad, 1.0F, 1.0F, 1.0F, 1.0F, light, OverlayTexture.DEFAULT_UV);
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
