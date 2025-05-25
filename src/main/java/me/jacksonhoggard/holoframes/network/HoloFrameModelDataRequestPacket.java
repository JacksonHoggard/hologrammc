package me.jacksonhoggard.holoframes.network;

import me.jacksonhoggard.holoframes.Holoframes;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public class HoloFrameModelDataRequestPacket {
    public static final Identifier ID = Identifier.of(Holoframes.MOD_ID, "model_data_request");
    public final String hologramFile;

    public HoloFrameModelDataRequestPacket(String hologramFile) {
        this.hologramFile = hologramFile;
    }

    public static void register() {
        PayloadTypeRegistry.playC2S().register(HoloFrameModelDataRequestPayload.ID, HoloFrameModelDataRequestPayload.CODEC);
    }

    public record HoloFrameModelDataRequestPayload(String hologramFile) implements CustomPayload {
        public static final CustomPayload.Id<HoloFrameModelDataRequestPayload> ID = new CustomPayload.Id<>(HoloFrameModelDataRequestPacket.ID);
        public static final PacketCodec<RegistryByteBuf, HoloFrameModelDataRequestPayload> CODEC = PacketCodec.tuple(
                PacketCodecs.STRING, HoloFrameModelDataRequestPayload::hologramFile,
                HoloFrameModelDataRequestPayload::new
        );

        @Override
        public Id<? extends CustomPayload> getId() {
            return ID;
        }
    }
}
