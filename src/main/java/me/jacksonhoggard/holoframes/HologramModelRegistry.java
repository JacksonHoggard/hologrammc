package me.jacksonhoggard.holoframes;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.impl.util.log.Log;
import net.fabricmc.loader.impl.util.log.LogCategory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
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
                                ObjLoader.ObjModel model = ObjLoader.load(path.toString());
                                float[] vertices = model.getVerticesInFaceOrder();
                                float[] texCoords = model.getTexCoordsInFaceOrder();
                                byte[] texture = null;
                                if(model.textureFileName != null)
                                    texture = convertImageToByteArray(
                                        FabricLoader.getInstance().getConfigDir()
                                                .resolve("holoframes")
                                                .resolve("models")
                                                .resolve(model.textureFileName)
                                                .toString());
                                if(texture == null) {
                                    if(model.textureFileName != null)
                                        Log.error(LogCategory.LOG, "Texture for model " + path.getFileName() + " not found.");
                                    texture = new byte[0];
                                }
                                models.put(path.getFileName().toString(), new HologramModel(vertices, texCoords, texture));
                                Log.info(LogCategory.LOG, "Loaded hologram model: " + path.getFileName(), Holoframes.MOD_ID);
                            } catch (Exception e) {
                                Log.error(LogCategory.LOG, "Failed to load hologram model from " + path, e);
                            }
                        });
            }
        } catch (Exception e) {
            Log.error(LogCategory.LOG, "Failed to load hologram models", e);
        }
    }

    public record HologramModel(float[] vertices, float[] texCoords, byte[] texture) {
    }

    public static byte[] convertImageToByteArray(String imagePath) {
        try {
            File imageFile = new File(imagePath);
            BufferedImage bufferedImage = ImageIO.read(imageFile);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            String fileExtension = imagePath.substring(imagePath.lastIndexOf(".") + 1);
            ImageIO.write(bufferedImage, fileExtension, outputStream);
            return outputStream.toByteArray();
        } catch (IOException e) {
            Log.error(LogCategory.LOG, "Failed to convert image to byte array", e);
            return null;
        }
    }
}
