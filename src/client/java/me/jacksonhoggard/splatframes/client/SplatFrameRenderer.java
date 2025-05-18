package me.jacksonhoggard.splatframes.client;

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
import org.lwjgl.opengl.*;

import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.logging.Logger;

@Environment(value = EnvType.CLIENT)
public class SplatFrameRenderer {
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
    private static final Map<String, GaussianSplatModel> LOADED_MODELS = new HashMap<>();
    // Shader program IDs
    private static int drawProgram = 0;
    private static int computeProgram = 0;
    // Flag to indicate shaders are initialized
    private static boolean shadersInitialized = false;

    // Inner class to hold GPU buffers and metadata for a hologram model
    private static class GaussianSplatModel {
        float[] points;

        // Constructor sets up VAO/VBO
        GaussianSplatModel(float[] points) {
            this.points = points;
        }
    }

    /**
     * Main render function called by the ItemFrameRenderer mixin.
     */
    public static void renderHologram(ItemFrameEntityRenderState frameEntityRenderState, String splatFile, MatrixStack matrices,
                                      VertexConsumerProvider vertexConsumers, int light) {
        // Ensure the model for this splat file is loaded into GPU memory
        GaussianSplatModel model = LOADED_MODELS.get(splatFile);
        if (model == null) {
            model = loadSplatModel(splatFile);
            if (model == null) {
                return;  // loading failed
            }
            LOADED_MODELS.put(splatFile, model);
        }

        Tessellator tessellator = Tessellator.getInstance();

        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.TRIANGLES, VertexFormats.POSITION_COLOR);
        matrices.push();
        orientToFrame(frameEntityRenderState, matrices);
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



//        // Initialize shaders on first run
//        if (!shadersInitialized) {
//            initShaders();
//            shadersInitialized = true;
//        }



//        IntBuffer programBuffer = MemoryUtil.memAllocInt(1);
//        GL11.glGetIntegerv(GL20.GL_CURRENT_PROGRAM, programBuffer);
//
//        // Prepare OpenGL state for rendering translucent points
//        GL11.glEnable(GL11.GL_BLEND);
//        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
//        GL11.glEnable(GL20.GL_VERTEX_PROGRAM_POINT_SIZE); // allow setting point size in shader
//
//        // Use a compute shader to update points (e.g., animate or filter) before drawing, if desired
//        GL43.glUseProgram(computeProgram);
//        // Update a time uniform for animation (not shown: retrieving time or tick)
//        float time = MinecraftClient.getInstance().world.getTime() + MinecraftClient.getInstance().getRenderTickCounter().getDynamicDeltaTicks();
//        int timeUniformLoc = GL20.glGetUniformLocation(computeProgram, "u_Time");
//        GL20.glUniform1f(timeUniformLoc, time);
//        // Bind the VBO as shader storage for compute
//        GL43.glBindBufferBase(GL43.GL_SHADER_STORAGE_BUFFER, 0, model.vboId);
//        // Dispatch compute shader to process all points (e.g., update positions or colors)
//        int workgroups = (model.pointCount / 256) + 1;
//        GL43.glDispatchCompute(workgroups, 1, 1);
//        // Wait for compute shader to finish writing to the buffer
//        GL43.glMemoryBarrier(GL43.GL_SHADER_STORAGE_BARRIER_BIT);
//
//        // Use the draw shader program (vertex/fragment) for rendering points as splats
//        GL20.glUseProgram(drawProgram);
//
//        // Set up transformation matrix uniform in shader to position the hologram in the frame
//        // Compute model matrix based on item frame orientation
//        matrices.push();
//        orientToFrame(frameEntityRenderState, matrices);
//        // Get the combined matrix (model-view-projection)
//        //float[] mvpMatrix = getModelViewProjection(matrices);
//        int mvpLoc = GL20.glGetUniformLocation(drawProgram, "u_MVPMatrix");
//        GL20.glUniformMatrix4fv(mvpLoc, false, mvpMatrix);
//        matrices.pop();
//
//        // Bind and draw the point cloud
//        GL30.glBindVertexArray(model.vaoId);
//        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, model.vboId);
//        GL11.glDrawArrays(GL11.GL_POINTS, 0, model.pointCount);
//
//        // Reset state
//        GL30.glBindVertexArray(0);
//        GL20.glUseProgram(programBuffer.get(0));
    }

    /** Load the .splat file data from disk and upload to a GPU buffer. */
    private static GaussianSplatModel loadSplatModel(String fileName) {
        Path path = Path.of("config/splatframes/" + fileName);
        if (!Files.exists(path)) {
            // Try loading from mod resources (e.g., `resources/splats/` for a built-in sample)
            try {
                path = Path.of(SplatFrameRenderer.class.getResource("/splats/" + fileName).toURI());
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
        }
        Logger.getGlobal().info("Loading splat file: " + path.toString());
        try {
            ObjLoader.ObjModel model = ObjLoader.load(path.toString());
            float[] points = ObjLoader.getVerticesInFaceOrder(model);

            return new GaussianSplatModel(points);

//            // Generate VAO and VBO for this point cloud
//            int vao = GL30.glGenVertexArrays();
//            int vbo = GL15.glGenBuffers();
//            GL30.glBindVertexArray(vao);
//            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
//            GL15.glBufferData(GL15.GL_ARRAY_BUFFER, data.add, GL15.GL_STATIC_DRAW);
//            MemoryUtil.memFree(data.add);
//
//            // Define vertex attributes (we'll use layout(location=0) for position, etc. in the shader)
//            int stride = 8 * Float.BYTES;
//            // Attribute 0: vec3 position (x,y,z)
//            GL20.glEnableVertexAttribArray(0);
//            GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, stride, 0);
//            // Attribute 1: vec4 color (r,g,b,a)
//            GL20.glEnableVertexAttribArray(1);
//            GL20.glVertexAttribPointer(1, 4, GL11.GL_FLOAT, false, stride, 3 * Float.BYTES);
//            // Attribute 2: float size
//            GL20.glEnableVertexAttribArray(2);
//            GL20.glVertexAttribPointer(2, 1, GL11.GL_FLOAT, false, stride, 7 * Float.BYTES);
//
//            GL30.glBindVertexArray(0);
            //return new GaussianSplatModel(count, vao, vbo);
        } catch (Exception e) {
            System.err.println("Failed to load splat file: " + fileName);
            e.printStackTrace();
            return null;
        }
    }

    /** Initialize the OpenGL shader programs (compute and draw shaders). */
    private static void initShaders() {
        // (For brevity, shader source strings are abbreviated. In practice, load from resources .glsl files)
        String computeShaderSrc = """
            #version 430
            layout(local_size_x = 256) in;
            struct Point { vec3 pos; vec4 color; float size; };
            layout(std430, binding = 0) buffer Points { Point points[]; };
            uniform float u_Time;
            void main() {
                uint id = gl_GlobalInvocationID.x;
                if (id >= points.length()) return;
                // Example: make points bob in a sine wave for interactivity
                points[id].pos.z += 0.1 * sin(u_Time + id * 0.1);
            }
        """;
        computeProgram = compileShaderProgram(computeShaderSrc, GL43.GL_COMPUTE_SHADER);

        String vertexShaderSrc = """
            #version 430
            layout(location=0) in vec3 a_Position;
            layout(location=1) in vec4 a_Color;
            layout(location=2) in float a_Size;
            uniform mat4 u_MVPMatrix;
            out vec4 v_Color;
            out float v_PointSize;
            void main() {
                v_Color = a_Color;
                v_PointSize = a_Size;
                gl_Position = u_MVPMatrix * vec4(a_Position, 1.0);
                // Set point size based on stored size and perspective (optional: scale with distance)
                gl_PointSize = a_Size * 1.0; // scale factor to make splat visible; adjust as needed
            }
        """;
        String fragmentShaderSrc = """
            #version 430
            in vec4 v_Color;
            out vec4 fragColor;
            void main() {
                // Each point rendered as a circle (disc) with Gaussian falloff
                float dist = length(gl_PointCoord - vec2(0.5));
                float alpha = exp(-4.0 * dist * dist); // Gaussian falloff
                fragColor = vec4(v_Color.rgb, v_Color.a * alpha);
            }
        """;
        drawProgram = compileShaderProgram(vertexShaderSrc, GL20.GL_VERTEX_SHADER, fragmentShaderSrc, GL20.GL_FRAGMENT_SHADER);
    }

    /** Helper to compile a shader (or shaders) and link into a program. */
    private static int compileShaderProgram(String src, int shaderType) {
        int shader = GL20.glCreateShader(shaderType);
        GL20.glShaderSource(shader, src);
        GL20.glCompileShader(shader);
        if (GL20.glGetShaderi(shader, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE) {
            System.err.println("Shader compile error: " + GL20.glGetShaderInfoLog(shader));
        }
        int program = GL20.glCreateProgram();
        GL20.glAttachShader(program, shader);
        GL20.glLinkProgram(program);
        if (GL20.glGetProgrami(program, GL20.GL_LINK_STATUS) == GL11.GL_FALSE) {
            System.err.println("Program link error: " + GL20.glGetProgramInfoLog(program));
        }
        return program;
    }
    private static int compileShaderProgram(String vertSrc, int vertType, String fragSrc, int fragType) {
        int vertShader = GL20.glCreateShader(vertType);
        GL20.glShaderSource(vertShader, vertSrc);
        GL20.glCompileShader(vertShader);
        if (GL20.glGetShaderi(vertShader, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE) {
            System.err.println("Vertex shader compile error: " + GL20.glGetShaderInfoLog(vertShader));
        }
        int fragShader = GL20.glCreateShader(fragType);
        GL20.glShaderSource(fragShader, fragSrc);
        GL20.glCompileShader(fragShader);
        if (GL20.glGetShaderi(fragShader, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE) {
            System.err.println("Fragment shader compile error: " + GL20.glGetShaderInfoLog(fragShader));
        }
        int program = GL20.glCreateProgram();
        GL20.glAttachShader(program, vertShader);
        GL20.glAttachShader(program, fragShader);
        GL20.glLinkProgram(program);
        if (GL20.glGetProgrami(program, GL20.GL_LINK_STATUS) == GL11.GL_FALSE) {
            System.err.println("Program link error: " + GL20.glGetProgramInfoLog(program));
        }
        // (Error checks omitted for brevity)
        return program;
    }

    /** Apply transformations to orient the hologram model within the item frame. */
    private static void orientToFrame(ItemFrameEntityRenderState itemFrameEntityRenderState, MatrixStack matrixStack) {
        Direction direction = itemFrameEntityRenderState.facing;
        //matrixStack.translate((double)direction.getOffsetX() * 0.46875, (double)direction.getOffsetY() * 0.46875, (double)direction.getOffsetZ() * 0.46875);
        matrixStack.scale(0.5F, 0.5F, 0.5F);
        matrixStack.translate(0.0F, 0.25F, 0.0F);
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
    }

    /** Extract the current model-view-projection matrix from the MatrixStack for the shader. */
    private static float[] getModelViewProjection(MatrixStack matrices) {
        // Combine the current stack matrix with Minecraft's projection matrix
        // (In Fabric API, can retrieve projection via MinecraftClient)
        float[] modelMatrix = matrices.peek().getPositionMatrix().get(new float[16]);
        float[] projMatrix = MinecraftClient.getInstance().gameRenderer.getBasicProjectionMatrix(MinecraftClient.getInstance().options.getFov().getValue()).get(new float[16]);
        // Multiply modelMatrix and projMatrix to get MVP (16-element float array)
        float[] mvp = new float[16];
        multiplyMatrix(modelMatrix, projMatrix, mvp);
        return mvp;
    }

    private static void multiplyMatrix(float[] a, float[] b, float[] dest) {
        for (int i = 0; i < 4; ++i) {
            for (int j = 0; j < 4; ++j) {
                dest[i * 4 + j] = a[i * 4 + 0] * b[0 * 4 + j] +
                        a[i * 4 + 1] * b[1 * 4 + j] +
                        a[i * 4 + 2] * b[2 * 4 + j] +
                        a[i * 4 + 3] * b[3 * 4 + j];
            }
        }
    }
}
