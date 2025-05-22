package me.jacksonhoggard.holoframes.client;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.render.*;
import net.minecraft.client.render.entity.state.ItemFrameEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.RotationAxis;
import org.joml.Matrix4f;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.logging.Logger;

@Environment(value = EnvType.CLIENT)
public class HoloFrameRenderer {
    public static final RenderPipeline TRIANGLES = RenderPipelines.register(
            RenderPipeline.builder(RenderPipelines.POSITION_COLOR_SNIPPET)
                    .withLocation("pipeline/triangles")
                    .withVertexFormat(VertexFormats.POSITION_COLOR, VertexFormat.DrawMode.TRIANGLES)
                    .withCull(true)
                    .withBlend(BlendFunction.TRANSLUCENT)
                    .build()
    );

    private static final RenderLayer renderLayer = RenderLayer.of(
            "triangles",
            1536,
            TRIANGLES,
            RenderLayer.MultiPhaseParameters.builder().lineWidth(new RenderPhase.LineWidth(OptionalDouble.of(1))).build(false)
    );

    private static float totalTickDelta = 0;

    // Cache of loaded hologram models: filename -> GPU buffer model
    private static final Map<String, HologramModel> LOADED_MODELS = new HashMap<>();

    private static class HologramModel {
        float[] points;

        HologramModel(float[] points) {
            this.points = points;
        }
    }

    /**
     * Main render function called by the ItemFrameRenderer mixin.
     */
    public static void renderHologram(ItemFrameEntityRenderState frameEntityRenderState, String holoFile, MatrixStack matrices) {
        HologramModel model = LOADED_MODELS.get(holoFile);
        if (model == null) {
            model = loadHologramModel(holoFile);
            if (model == null) {
                return;  // loading failed
            }
            LOADED_MODELS.put(holoFile, model);
        }

        Tessellator tessellator = Tessellator.getInstance();

        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.TRIANGLES, VertexFormats.POSITION_COLOR);
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

        for (int i = 0; i < model.points.length; i += 3) {
            float x = (model.points[i] - minCoord) / (range != 0 ? range : 1);
            float y = (model.points[i + 1] - minCoord) / (range != 0 ? range : 1);
            float z = (model.points[i + 2] - minCoord) / (range != 0 ? range : 1);
            float xNormalized = (x - 0.5f);
            float yNormalized = (y - 0.5f);
            float zNormalized = (z - 0.5f);
            buffer.vertex(modifiedMatrix, xNormalized, yNormalized, zNormalized).color(1.0f, 1.0f, 1.0f, 0.5f);
        }

        matrices.pop();

        BuiltBuffer builtBuffer = buffer.end();
        renderLayer.draw(builtBuffer);
    }

    /** Load the file data from disk and upload to a GPU buffer. */
    private static HologramModel loadHologramModel(String fileName) {
        Path path = Path.of("config/holoframes/" + fileName);
        if (!Files.exists(path)) {
            // Try loading from mod resources (e.g., `resources/models/` for a built-in sample)
            try {
                path = Path.of(HoloFrameRenderer.class.getResource("/models/" + fileName).toURI());
            } catch (Exception e) {
                return null; // Failed to load model
            }
        }
        try {
            ObjLoader.ObjModel model = ObjLoader.load(path.toString());
            float[] points = ObjLoader.getVerticesInFaceOrder(model);

            return new HologramModel(points);
        } catch (Exception e) {
            return null;
        }
    }

    /** Apply transformations to orient the hologram model within the item frame. */
    private static void orientToFrame(ItemFrameEntityRenderState itemFrameEntityRenderState, MatrixStack matrixStack) {
        Direction direction = itemFrameEntityRenderState.facing;
        //matrixStack.translate((double)direction.getOffsetX() * 0.46875, (double)direction.getOffsetY() * 0.46875, (double)direction.getOffsetZ() * 0.46875);
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
        Logger.getLogger("HoloFrames").info("Hologram rotation: " + rotation);
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
