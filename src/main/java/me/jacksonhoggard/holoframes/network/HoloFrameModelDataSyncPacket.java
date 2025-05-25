package me.jacksonhoggard.holoframes.network;

import me.jacksonhoggard.holoframes.Holoframes;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import java.nio.ByteBuffer;

public class HoloFrameModelDataSyncPacket {
    public static final Identifier ID = Identifier.of(Holoframes.MOD_ID, "model_data_sync");

    public final byte[] vertices;
    public final String hologramFile;

    public HoloFrameModelDataSyncPacket(float[] vertices, String hologramFile) {
        this.vertices = floatToByteArray(vertices);
        this.hologramFile = hologramFile;
    }

    public static void register() {
        PayloadTypeRegistry.playS2C().register(HoloFrameModelDataPayload.ID, HoloFrameModelDataPayload.CODEC);
    }

    public record HoloFrameModelDataPayload(byte[] vertices, String hologramFile) implements CustomPayload {
        public static final CustomPayload.Id<HoloFrameModelDataPayload> ID = new CustomPayload.Id<>(HoloFrameModelDataSyncPacket.ID);
        public static final PacketCodec<RegistryByteBuf, HoloFrameModelDataPayload> CODEC = PacketCodec.tuple(
                PacketCodecs.BYTE_ARRAY, HoloFrameModelDataPayload::vertices,
                PacketCodecs.STRING, HoloFrameModelDataPayload::hologramFile,
                HoloFrameModelDataPayload::new
        );

        @Override
        public Id<? extends CustomPayload> getId() {
            return ID;
        }
    }

    public HoloFrameModelDataPayload toPayload() {
        return new HoloFrameModelDataPayload(this.vertices, this.hologramFile);
    }

    private static byte[] floatToByteArray(float[] input) {
        byte[] bytes = new byte[input.length*4];
        for (int x = 0; x < input.length; x++) {
            ByteBuffer.wrap(bytes, x*4, 4).putFloat(input[x]);
        }
        return bytes;
    }
}
