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
import me.jacksonhoggard.holoframes.ObjLoader;
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
import org.apache.commons.io.IOUtils;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Environment(value = EnvType.CLIENT)
public class HoloFrameRenderer {

    private static final HologramModel DEFAULT_MODEL = loadDefaultModel();

    private static HologramModel loadDefaultModel() {
        String defaultModelObj;
        try {
            InputStream inputStream = HoloFrameRenderer.class
                    .getClassLoader()
                    .getResourceAsStream("assets/" + Holoframes.MOD_ID + "/models/hologram_error.obj");
            if (inputStream == null) {
                throw new IllegalStateException("Default hologram model not found");
            }
            defaultModelObj = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load default hologram model", e);
        }
        ObjLoader.ObjModel model = ObjLoader.loadFromString(defaultModelObj);
        float[] points = model.getVerticesInFaceOrder();
        float[] texCoords = model.getTexCoordsInFaceOrder();
        HologramModel defaultModel = new HologramModel(points);
        defaultModel.texCoords = texCoords;
        defaultModel.texture = new HologramTexture(
                "default_hologram_texture",
                HoloFrameRenderer.class
                        .getClassLoader()
                        .getResourceAsStream("assets/" + Holoframes.MOD_ID + "/textures/hologram_error.png")
        );
        defaultModel.normalize();
        return defaultModel;
    }

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

    private static final Map<String, HologramModel> LOADED_MODELS = new HashMap<>();

    private static class HologramModel {
        float[] points;
        float[] texCoords;
        HologramTexture texture;

        HologramModel(float[] points) {
            this.points = points;
        }

        void normalize() {
            // Calculate centroid
            float sumX = 0, sumY = 0, sumZ = 0;
            int numPoints = this.points.length / 3;
            for (int i = 0; i < this.points.length; i += 3) {
                sumX += this.points[i];
                sumY += this.points[i + 1];
                sumZ += this.points[i + 2];
            }
            float centroidX = sumX / numPoints;
            float centroidY = sumY / numPoints;
            float centroidZ = sumZ / numPoints;

            // Find min and max for normalization
            float minCoord = Float.MAX_VALUE;
            float maxCoord = -Float.MAX_VALUE;
            for (float point : this.points) {
                if (point < minCoord) minCoord = point;
                if (point > maxCoord) maxCoord = point;
            }
            float range = maxCoord - minCoord;
            for (int i = 0, j = 0; i < this.points.length; i += 3, j += 2) {
                this.points[i] = (this.points[i] - centroidX) / (range != 0 ? range : 1);
                this.points[i + 1] = (this.points[i + 1] - centroidY) / (range != 0 ? range : 1);
                this.points[i + 2] = (this.points[i + 2] - centroidZ) / (range != 0 ? range : 1);
            }
        }
    }

    private static class HologramTexture implements AutoCloseable {
        final GpuTexture texture;
        final NativeImage image;

        public HologramTexture(String label, InputStream imageStream) {
            try {
                this.image = NativeImage.read(imageStream);
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

        @Override
        public void close() {
            if (this.texture != null) {
                this.texture.close();
            }
            if (this.image != null) {
                this.image.close();
            }
        }
    }

    public static void addHologramModel(float[] points, float[] texCoords, String holoFile, byte[] texture) {
        HologramModel model = new HologramModel(points);
        model.normalize();
        model.texture = texture != null && texture.length != 0 ? new HologramTexture(holoFile, texture) : null;
        model.texCoords = texture != null && texture.length != 0 ? texCoords : new float[points.length * 2];
        LOADED_MODELS.put(holoFile, model);
    }

    public static void renderHologram(ItemFrameEntityRenderState frameEntityRenderState, String holoFile, MatrixStack matrices, VertexConsumerProvider vertexConsumerProvider) {
        HologramModel model = LOADED_MODELS.get(holoFile);
        if (model == null) {
            LOADED_MODELS.put(holoFile, DEFAULT_MODEL);
            ClientPlayNetworking.send(new HoloFrameModelDataRequestPacket(holoFile).toPayload());
            return;
        }

        matrices.push();
        orientToFrame(frameEntityRenderState, matrices);
        rotateHologram(frameEntityRenderState, matrices);
        assert MinecraftClient.getInstance().world != null;
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((MinecraftClient.getInstance().world.getTime() + MinecraftClient.getInstance().getRenderTickCounter().getDynamicDeltaTicks()) * 4));
        Matrix4f modifiedMatrix = matrices.peek().getPositionMatrix();

        Vector3f color = model.texture != null ?
                new Vector3f(1.0F, 1.0F, 1.0F) :
                new Vector3f(0.3333333333333333F, 1.0F, 1.0F);

        VertexConsumer vertexConsumer = vertexConsumerProvider.getBuffer(HOLOGRAM_LAYER);

        for (int i = 0, j = 0; i < model.points.length; i += 3, j += 2) {
            vertexConsumer.vertex(modifiedMatrix, model.points[i], model.points[i + 1], model.points[i + 2])
                    .texture(model.texCoords[j], 1.0F - model.texCoords[j + 1])
                    .color(color.x, color.y, color.z, 0.5F);
        }

        matrices.pop();

        RenderSystem.setShaderTexture(0, model.texture != null ? model.texture.texture : null);
    }

    private static void orientToFrame(ItemFrameEntityRenderState itemFrameEntityRenderState, MatrixStack matrixStack) {
        Direction direction = itemFrameEntityRenderState.facing;
        matrixStack.scale(0.5F, 0.5F, 0.5F);
        matrixStack.translate(0.0F, 0.5F, 0.0F);
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
