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
import org.joml.Matrix4f;

import java.io.InputStream;
import java.util.*;

@Environment(value = EnvType.CLIENT)
public class HoloFrameRenderer {

    private static final HologramModel DEFAULT_MODEL = loadDefaultModel();

    private static HologramModel loadDefaultModel() {
        ObjLoader.ObjModel model = ObjLoader.loadFromString(DEFAULT_MODEL_OBJ);
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
        public void close() throws Exception {
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
        model.texture = texture != null && texture.length != 0 ? new HologramTexture(holoFile, texture) : null;
        model.texCoords = texCoords;
        LOADED_MODELS.put(holoFile, model);
    }

    public static void renderHologram(ItemFrameEntityRenderState frameEntityRenderState, String holoFile, MatrixStack matrices) {
        HologramModel model = LOADED_MODELS.get(holoFile);
        if (model == null) {
            LOADED_MODELS.put(holoFile, DEFAULT_MODEL);
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

    private static final String DEFAULT_MODEL_OBJ =
            "mtllib error_x.mtl\n" +
            "o Cube\n" +
            "v -1.000000 0.000000 1.414214\n" +
            "v -1.000000 1.414214 0.000000\n" +
            "v -1.000000 -1.414214 0.000000\n" +
            "v -1.000000 0.000000 -1.414214\n" +
            "v 1.000000 0.000000 1.414214\n" +
            "v 1.000000 1.414214 0.000000\n" +
            "v 1.000000 -1.414214 0.000000\n" +
            "v 1.000000 0.000000 -1.414214\n" +
            "v -1.000000 1.414214 0.000000\n" +
            "v -1.000000 0.000000 -1.414214\n" +
            "v 1.000000 0.000000 -1.414214\n" +
            "v 1.000000 1.414214 0.000000\n" +
            "v -1.000000 -1.414214 0.000000\n" +
            "v -1.000000 0.000000 1.414214\n" +
            "v -1.000000 1.414214 0.000000\n" +
            "v -1.000000 0.000000 -1.414214\n" +
            "v 1.000000 -1.414214 0.000000\n" +
            "v 1.000000 0.000000 -1.414214\n" +
            "v 1.000000 0.000000 1.414214\n" +
            "v 1.000000 1.414214 0.000000\n" +
            "v -1.000000 1.414214 0.000000\n" +
            "v -1.000000 0.000000 -1.414214\n" +
            "v 1.000000 0.000000 -1.414214\n" +
            "v 1.000000 1.414214 0.000000\n" +
            "v -1.000000 -7.541393 -6.127180\n" +
            "v -1.000000 -6.127180 -7.541393\n" +
            "v 1.000000 -6.127180 -7.541393\n" +
            "v 1.000000 -7.541393 -6.127180\n" +
            "v 1.000000 6.127180 7.541393\n" +
            "v 1.000000 7.541393 6.127180\n" +
            "v -1.000000 7.541393 6.127180\n" +
            "v -1.000000 6.127180 7.541393\n" +
            "v -1.000000 -7.541393 6.127180\n" +
            "v 1.000000 -7.541393 6.127180\n" +
            "v 1.000000 -6.127180 7.541393\n" +
            "v -1.000000 -6.127180 7.541393\n" +
            "v 1.000000 6.127180 -7.541393\n" +
            "v -1.000000 6.127180 -7.541393\n" +
            "v -1.000000 7.541393 -6.127180\n" +
            "v 1.000000 7.541393 -6.127180\n" +
            "v 1.000000 0.000000 -1.414214\n" +
            "v -1.000000 0.000000 -1.414214\n" +
            "v -1.000000 0.000000 -1.414214\n" +
            "v 1.000000 0.000000 -1.414214\n" +
            "vn -1.0000 -0.0000 -0.0000\n" +
            "vn -0.0000 0.7071 -0.7071\n" +
            "vn 1.0000 -0.0000 -0.0000\n" +
            "vn -0.0000 -0.7071 -0.7071\n" +
            "vn -0.0000 -0.7071 0.7071\n" +
            "vn -0.0000 0.7071 0.7071\n" +
            "vt 0.375000 0.000000\n" +
            "vt 0.625000 0.000000\n" +
            "vt 0.625000 0.250000\n" +
            "vt 0.375000 0.250000\n" +
            "vt 0.625000 0.750000\n" +
            "vt 0.625000 1.000000\n" +
            "vt 0.375000 0.500000\n" +
            "vt 0.625000 0.500000\n" +
            "vt 0.375000 0.750000\n" +
            "vt 0.375000 1.000000\n" +
            "vt 0.125000 0.500000\n" +
            "vt 0.125000 0.750000\n" +
            "vt 0.875000 0.500000\n" +
            "vt 0.875000 0.750000\n" +
            "s 0\n" +
            "usemtl Material.001\n" +
            "f 1/1/1 2/2/1 4/3/1 3/4/1\n" +
            "f 6/5/2 2/6/2 15/6/2 20/5/2\n" +
            "f 7/7/3 8/8/3 6/5/3 5/9/3\n" +
            "f 12/5/2 11/8/2 23/8/2 24/5/2\n" +
            "f 7/7/2 5/9/2 19/9/2 17/7/2\n" +
            "f 4/3/2 2/2/2 9/2/2 10/3/2\n" +
            "f 1/1/2 3/4/2 13/4/2 14/1/2\n" +
            "f 2/6/2 6/5/2 12/5/2 9/6/2\n" +
            "f 4/3/2 10/3/2 22/3/2 16/3/2\n" +
            "f 6/5/2 8/8/2 11/8/2 12/5/2\n" +
            "f 2/2/2 1/1/2 14/1/2 15/2/2\n" +
            "f 9/6/2 12/5/2 24/5/2 21/6/2\n" +
            "f 10/3/2 9/2/2 21/2/2 22/3/2\n" +
            "f 11/8/2 8/8/2 18/8/2 23/8/2\n" +
            "f 5/9/2 6/5/2 20/5/2 19/9/2\n" +
            "f 8/8/2 7/7/2 17/7/2 18/8/2\n" +
            "f 3/4/2 4/3/2 16/3/2 13/4/2\n" +
            "f 25/4/4 26/3/4 27/8/4 28/7/4\n" +
            "f 16/3/1 26/3/1 25/4/1 13/4/1\n" +
            "f 18/8/2 27/8/2 26/3/2 16/3/2\n" +
            "f 17/7/3 28/7/3 27/8/3 18/8/3\n" +
            "f 13/4/5 25/4/5 28/7/5 17/7/5\n" +
            "f 29/9/6 30/5/6 31/6/6 32/10/6\n" +
            "f 20/5/3 30/5/3 29/9/3 19/9/3\n" +
            "f 15/6/2 31/6/2 30/5/2 20/5/2\n" +
            "f 14/10/1 32/10/1 31/6/1 15/6/1\n" +
            "f 19/9/5 29/9/5 32/10/5 14/10/5\n" +
            "f 33/11/5 34/7/5 35/9/5 36/12/5\n" +
            "f 17/7/4 34/7/4 33/11/4 13/11/4\n" +
            "f 19/9/3 35/9/3 34/7/3 17/7/3\n" +
            "f 14/12/6 36/12/6 35/9/6 19/9/6\n" +
            "f 13/11/1 33/11/1 36/12/1 14/12/1\n" +
            "f 37/8/2 38/13/2 39/14/2 40/5/2\n" +
            "f 22/13/4 38/13/4 37/8/4 23/8/4\n" +
            "f 21/14/1 39/14/1 38/13/1 22/13/1\n" +
            "f 24/5/6 40/5/6 39/14/6 21/14/6\n" +
            "f 23/8/3 37/8/3 40/5/3 24/5/3\n" +
            "f 41/8/2 42/3/2 43/3/2 44/8/2\n" +
            "f 16/3/2 42/3/2 41/8/2 18/8/2\n" +
            "f 22/3/2 43/3/2 42/3/2 16/3/2\n" +
            "f 23/8/2 44/8/2 43/3/2 22/3/2\n" +
            "f 18/8/2 41/8/2 44/8/2 23/8/2\n";
}
