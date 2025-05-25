package me.jacksonhoggard.holoframes;

import me.jacksonhoggard.holoframes.network.HoloFrameModelDataSyncPacket;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.impl.util.log.Log;
import net.fabricmc.loader.impl.util.log.LogCategory;
import net.minecraft.server.network.ServerPlayerEntity;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class HologramModelRegistry {
    public static final Map<String, HologramModel> models = new HashMap<String, HologramModel>();

    public static void register() {
        try {
            var resourceUrl = FabricLoader.getInstance().getConfigDir().resolve("holoframes").resolve("models");
            if (resourceUrl.toFile().exists()) {
                var modelsUri = resourceUrl.toUri();
                var modelsPath = java.nio.file.Paths.get(modelsUri);
                java.nio.file.Files.list(modelsPath)
                        .filter(path -> path.getFileName().toString().endsWith(".obj"))
                        .forEach(path -> {
                            try {
                                float[] vertices = loadOBJModel(path);
                                models.put(path.getFileName().toString(), new HologramModel(vertices));
                            } catch (Exception e) {
                                Log.error(LogCategory.KNOT, "Failed to load hologram model from " + path, e);
                            }
                        });
            }
        } catch (Exception e) {
            Log.error(LogCategory.KNOT, "Failed to load hologram models", e);
        }
    }

    private static float[] loadOBJModel(java.nio.file.Path path) throws Exception {
        ObjLoader.ObjModel model = ObjLoader.load(path.toString());
        return ObjLoader.getVerticesInFaceOrder(model);
    }

    public record HologramModel(float[] vertices) {
    }
}
