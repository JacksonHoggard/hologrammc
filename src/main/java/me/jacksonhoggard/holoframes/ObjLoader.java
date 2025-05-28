package me.jacksonhoggard.holoframes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;

public class ObjLoader {

    public static class ObjModel {
        public List<float[]> vertices = new LinkedList<>();
        public List<int[]> faces = new LinkedList<>();
        public List<float[]> texCoords = new LinkedList<>();
        public List<int[]> texFaces = new LinkedList<>();

        public float[] getVerticesInFaceOrder() {
            int count = 0;
            for (int[] face : this.faces) {
                count += face.length;
            }
            float[] orderedVertices = new float[count * 3];
            int pos = 0;
            for (int[] face : this.faces) {
                for (int index : face) {
                    float[] vertex = this.vertices.get(index);
                    orderedVertices[pos++] = vertex[0];
                    orderedVertices[pos++] = vertex[1];
                    orderedVertices[pos++] = vertex[2];
                }
            }
            return orderedVertices;
        }

        public float[] getTexCoordsInFaceOrder() {
            int count = 0;
            for (int[] face : this.texFaces) {
                count += face.length;
            }
            float[] orderedTexCoords = new float[count * 2];
            int pos = 0;
            for (int[] face : this.texFaces) {
                for (int index : face) {
                    float[] tex = (index != -1) ? this.texCoords.get(index) : new float[]{0f, 0f};
                    orderedTexCoords[pos++] = tex[0];
                    orderedTexCoords[pos++] = tex[1];
                }
            }
            return orderedTexCoords;
        }
    }

    public static ObjModel load(String filePath) throws IOException {
        ObjModel model = new ObjModel();
        List<String> lines = Files.readAllLines(Paths.get(filePath));

        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }
            String[] tokens = line.split("\\s+");
            switch (tokens[0]) {
                case "v" -> {
                    // Parse vertex position
                    float x = Float.parseFloat(tokens[1]);
                    float y = Float.parseFloat(tokens[2]);
                    float z = Float.parseFloat(tokens[3]);
                    model.vertices.add(new float[]{x, y, z});
                }
                case "vt" -> {
                    // Parse texture coordinate
                    float u = Float.parseFloat(tokens[1]);
                    float v = Float.parseFloat(tokens[2]);
                    model.texCoords.add(new float[]{u, v});
                }
                case "f" -> {
                    int n = tokens.length - 1;
                    int[] vertexIndices = new int[n];
                    int[] textureIndices = new int[n];
                    for (int i = 0; i < n; i++) {
                        String[] parts = tokens[i + 1].split("/");
                        vertexIndices[i] = Integer.parseInt(parts[0]) - 1;
                        if (parts.length > 1 && !parts[1].isEmpty()) {
                            textureIndices[i] = Integer.parseInt(parts[1]) - 1;
                        } else {
                            textureIndices[i] = -1;
                        }
                    }
                    if (n > 3) {
                        for (int i = 1; i < n - 1; i++) {
                            model.faces.add(new int[]{vertexIndices[0], vertexIndices[i], vertexIndices[i + 1]});
                            model.texFaces.add(new int[]{textureIndices[0], textureIndices[i], textureIndices[i + 1]});
                        }
                    } else {
                        model.faces.add(vertexIndices);
                        model.texFaces.add(textureIndices);
                    }
                }
            }
        }
        return model;
    }
}