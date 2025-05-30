package me.jacksonhoggard.holoframes.client.network;

import me.jacksonhoggard.holoframes.client.HoloFrameRenderer;
import me.jacksonhoggard.holoframes.network.packet.HoloFrameModelDataSyncPacket;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

import java.nio.ByteBuffer;

public class ClientHoloFrameModelDataSyncReceiver {
    public static void register() {
        ClientPlayNetworking.registerGlobalReceiver(HoloFrameModelDataSyncPacket.HoloFrameModelDataPayload.ID, (payload, context) -> {
            context.client().execute(() -> {
                byte[] vertices = payload.vertices();
                byte[] texCoords = payload.texCoords();
                byte[] texture = payload.texture();
                String hologramFile = payload.hologramFile();
                HoloFrameRenderer.addHologramModel(byteArrayToFloat(vertices), byteArrayToFloat(texCoords), hologramFile, texture);
            });
        });
    }

    private static float[] byteArrayToFloat(byte[] input) {
        float[] floats = new float[input.length/4];
        for (int x = 0; x < input.length/4; x++) {
            floats[x] = ByteBuffer.wrap(input, x*4, 4).getFloat();
        }
        return floats;
    }
}
