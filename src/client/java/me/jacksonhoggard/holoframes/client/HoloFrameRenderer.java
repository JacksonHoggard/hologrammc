package me.jacksonhoggard.holoframes.client;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.AddressMode;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.TextureFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import me.jacksonhoggard.holoframes.Holoframes;
import me.jacksonhoggard.holoframes.network.packet.HoloFrameModelDataRequestPacket;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.render.*;
import net.minecraft.client.render.entity.state.ItemFrameEntityRenderState;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.RotationAxis;
import org.joml.Matrix4f;
import java.util.*;

@Environment(value = EnvType.CLIENT)
public class HoloFrameRenderer {

    private static final RenderPipeline HOLOGRAM_PIPELINE = RenderPipelines.register(
            RenderPipeline.builder(RenderPipelines.POSITION_TEX_COLOR_SNIPPET)
                    .withLocation(Identifier.of(Holoframes.MOD_ID, "pipeline/hologram"))
                    .withVertexFormat(VertexFormats.POSITION_TEXTURE_COLOR, VertexFormat.DrawMode.TRIANGLES)
                    .withSampler("Sampler0")
                    .withCull(true)
                    .withBlend(BlendFunction.TRANSLUCENT)
                    .withDepthWrite(true)
                    .withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST)
                    .build()
    );

    private static final RenderLayer HOLOGRAM_LAYER =
                RenderLayer.of(
                        "hologram",
                        1536,
                        HOLOGRAM_PIPELINE,
                        RenderLayer.MultiPhaseParameters.builder()
                                .build(false)
                );

    private static float totalTickDelta = 0;

    private static final Map<String, HologramModel> LOADED_MODELS = new HashMap<>();

    private static class HologramModel {
        float[] points;
        float[] texCoords;
        HologramTexture texture;

        HologramModel(float[] points) {
            this.points = points;
        }
    }

    private static class HologramTexture {
        final GpuTexture texture;
        final NativeImage image;

        public HologramTexture(String label, byte[] textureData) {
            try {
                image = NativeImage.read(textureData);
            } catch (Exception e) {
                throw new RuntimeException("Failed to load hologram texture", e);
            }
            this.texture = RenderSystem.getDevice().createTexture(
                    label,
                    TextureFormat.RGBA8,
                    image.getWidth(),
                    image.getHeight(),
                    1
            );
            this.texture.setAddressMode(
                    AddressMode.CLAMP_TO_EDGE,
                    AddressMode.CLAMP_TO_EDGE
            );
            this.texture.setTextureFilter(
                    FilterMode.LINEAR,
                    FilterMode.NEAREST,
                    false
            );

            RenderSystem.getDevice().createCommandEncoder().writeToTexture(
                    this.texture,
                    image,
                    0,
                    0,
                    0,
                    image.getWidth(),
                    image.getHeight(),
                    0,
                    0
            );
        }
    }


    public static void addHologramModel(float[] points, float[] texCoords, String holoFile, byte[] texture) {
        if(LOADED_MODELS.containsKey(holoFile))
            return;
        HologramModel model = new HologramModel(points);
        model.texture = texture != null && texture.length != 0 ? new HologramTexture(holoFile, texture) : null;
        model.texCoords = texCoords;
        LOADED_MODELS.put(holoFile, model);
    }

    public static void renderHologram(ItemFrameEntityRenderState frameEntityRenderState, String holoFile, MatrixStack matrices) {
        HologramModel model = LOADED_MODELS.get(holoFile);
        if (model == null) {
            ClientPlayNetworking.send(new HoloFrameModelDataRequestPacket(holoFile).toPayload());
            return;
        }

        Tessellator tessellator = Tessellator.getInstance();

        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.TRIANGLES, VertexFormats.POSITION_TEXTURE_COLOR);
        matrices.push();
        orientToFrame(frameEntityRenderState, matrices);
        rotateHologram(frameEntityRenderState, matrices);
        totalTickDelta += MinecraftClient.getInstance().getRenderTickCounter().getDynamicDeltaTicks();
        float rotation = (totalTickDelta / 50.0f % 360);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotation(rotation));
        Matrix4f modifiedMatrix = matrices.peek().getPositionMatrix();

        float minCoord = Float.MAX_VALUE;
        float maxCoord = -Float.MAX_VALUE;
        for (float point : model.points) {
            if (point < minCoord) minCoord = point;
            if (point > maxCoord) maxCoord = point;
        }
        float range = maxCoord - minCoord;

        for (int i = 0, j = 0; i < model.points.length; i += 3, j += 2) {
            float x = (model.points[i] - minCoord) / (range != 0 ? range : 1);
            float y = (model.points[i + 1] - minCoord) / (range != 0 ? range : 1);
            float z = (model.points[i + 2] - minCoord) / (range != 0 ? range : 1);
            float xNormalized = (x - 0.5f);
            float yNormalized = (y - 0.5f);
            float zNormalized = (z - 0.5f);
            buffer.vertex(modifiedMatrix, xNormalized, yNormalized, zNormalized)
                    .texture(model.texCoords[j], 1.0F - model.texCoords[j + 1])
                    .color(1.0F, 1.0F, 1.0F, 0.5F);
        }

        matrices.pop();

        BuiltBuffer builtBuffer = buffer.end();

        RenderSystem.setShaderTexture(0, model.texture != null ? model.texture.texture : null);
        HOLOGRAM_LAYER.draw(builtBuffer);
    }

    private static void orientToFrame(ItemFrameEntityRenderState itemFrameEntityRenderState, MatrixStack matrixStack) {
        Direction direction = itemFrameEntityRenderState.facing;
        matrixStack.scale(0.5F, 0.5F, 0.5F);
        matrixStack.translate(0.0F, 0.25F, 0.0F);
        float f = 0.0F;
        float g = 0.0F;
        if (direction.getAxis().isHorizontal()) {
            f = 0.0F;
            g = 180.0F - direction.getPositiveHorizontalDegrees();
        } else {
            if (direction == Direction.UP) {
                matrixStack.translate(0.0F, 0.5F, 0.0F);
            } else if (direction == Direction.DOWN) {
                matrixStack.translate(0.0F, -0.5F, 0.0F);
            } else {
                matrixStack.translate(0.0F, 0.25F, 0.0F);
            }
        }

        matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(f));
        matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(g));
    }

    private static void rotateHologram(ItemFrameEntityRenderState itemFrameEntityRenderState, MatrixStack matrixStack) {
        int rotation = ((ItemFrameEntityRenderStateAccessor) (itemFrameEntityRenderState)).holoFrames$getHologramRotation();
        switch (rotation) {
            case 0:
                break;
            case 1:
                matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90.0F));
                break;
            case 2:
                matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(180.0F));
                break;
            case 3:
                matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(270.0F));
                break;
            case 4:
                matrixStack.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(90.0F));
                break;
            case 5:
                matrixStack.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(270.0F));
                break;
        }
    }
}
