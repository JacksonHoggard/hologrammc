package me.jacksonhoggard.splatframes.client;

import java.nio.file.*;
import java.io.*;
import java.util.*;

public class ObjLoader {

    public static class ObjModel {
        public List<float[]> vertices = new ArrayList<>();
        public List<int[]> faces = new ArrayList<>();
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
            if (tokens[0].equals("v")) {
                // Parse vertex position
                float x = Float.parseFloat(tokens[1]);
                float y = Float.parseFloat(tokens[2]);
                float z = Float.parseFloat(tokens[3]);
                model.vertices.add(new float[] { x, y, z });
            } else if (tokens[0].equals("f")) {
                // Parse face indices
                int[] indices = new int[tokens.length - 1];
                for (int i = 0; i < indices.length; i++) {
                    String[] parts = tokens[i + 1].split("/");
                    // OBJ files index starting at 1
                    indices[i] = Integer.parseInt(parts[0]) - 1;
                }
                model.faces.add(indices);
            }
        }
        return model;
    }

    public static float[] getVerticesInFaceOrder(ObjModel model) {
        int count = 0;
        for (int[] face : model.faces) {
            count += face.length;
        }
        float[] orderedVertices = new float[count * 3];
        int pos = 0;
        for (int[] face : model.faces) {
            for (int index : face) {
                float[] vertex = model.vertices.get(index);
                orderedVertices[pos++] = vertex[0];
                orderedVertices[pos++] = vertex[1];
                orderedVertices[pos++] = vertex[2];
            }
        }
        return orderedVertices;
    }
}