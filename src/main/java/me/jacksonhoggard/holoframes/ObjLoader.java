package me.jacksonhoggard.holoframes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;

public class ObjLoader {

    public static class ObjModel {
        public List<float[]> vertices = new LinkedList<>();
        public List<int[]> faces = new LinkedList<>();
        public List<float[]> texCoords = new LinkedList<>();
        public List<int[]> texFaces = new LinkedList<>();
        public String textureFileName = null;

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

    public static ObjModel loadFromString(String objData) {
        ObjModel model = new ObjModel();
        String[] lines = objData.split("\n");

        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }
            String[] tokens = line.split("\\s+");
            switch (tokens[0]) {
                case "v" -> {
                    float x = Float.parseFloat(tokens[1]);
                    float y = Float.parseFloat(tokens[2]);
                    float z = Float.parseFloat(tokens[3]);
                    model.vertices.add(new float[]{x, y, z});
                }
                case "vt" -> {
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

    public static ObjModel load(String filePath) throws IOException {
        ObjModel model = new ObjModel();
        Path path = Paths.get(filePath);
        List<String> lines = Files.readAllLines(path);
        String mtlFileName = null;

        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }
            String[] tokens = line.split("\\s+");
            switch (tokens[0]) {
                case "mtllib" -> {
                    if (tokens.length > 1) {
                        mtlFileName = tokens[1];
                    }
                }
                case "v" -> {
                    float x = Float.parseFloat(tokens[1]);
                    float y = Float.parseFloat(tokens[2]);
                    float z = Float.parseFloat(tokens[3]);
                    model.vertices.add(new float[]{x, y, z});
                }
                case "vt" -> {
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

        if (mtlFileName != null) {
            String objDir = path.getParent() != null ? path.getParent().toString() : "";
            String mtlPath = objDir.isEmpty() ? mtlFileName : objDir + java.io.File.separator + mtlFileName;
            Path mtlFilePath = Paths.get(mtlPath);
            if (Files.exists(mtlFilePath)) {
                List<String> mtlLines = Files.readAllLines(mtlFilePath);
                for (String mtlLine : mtlLines) {
                    mtlLine = mtlLine.trim();
                    if (mtlLine.startsWith("map_Kd")) {
                        String[] mtlTokens = mtlLine.split("\\s+");
                        if (mtlTokens.length > 1) {
                            model.textureFileName = mtlTokens[1];
                            break;
                        }
                    }
                }
            }
        }

        return model;
    }
}