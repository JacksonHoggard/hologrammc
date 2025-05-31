package me.jacksonhoggard.holoframes.network.packet;

import me.jacksonhoggard.holoframes.Holoframes;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

public class HoloFrameScreenCloseRequestPacket implements IPacket<HoloFrameScreenCloseRequestPacket.HoloFrameScreenCloseRequestPayload> {
    public static final Identifier ID = Identifier.of(Holoframes.MOD_ID, "screen_close_request");

    public final String hologramFile;

    public HoloFrameScreenCloseRequestPacket(String hologramFile) {
        this.hologramFile = hologramFile;
    }

    public static void register() {
        PayloadTypeRegistry.playC2S().register(HoloFrameScreenCloseRequestPayload.ID, HoloFrameScreenCloseRequestPayload.CODEC);
    }

    public @NotNull HoloFrameScreenCloseRequestPayload toPayload() {
        return new HoloFrameScreenCloseRequestPayload(this.hologramFile);
    }

    public record HoloFrameScreenCloseRequestPayload(String hologramFile) implements CustomPayload {
        public static final CustomPayload.Id<HoloFrameScreenCloseRequestPayload> ID = new CustomPayload.Id<>(HoloFrameScreenCloseRequestPacket.ID);
        public static final PacketCodec<RegistryByteBuf, HoloFrameScreenCloseRequestPayload> CODEC = PacketCodec.tuple(
                PacketCodecs.STRING, HoloFrameScreenCloseRequestPayload::hologramFile,
                HoloFrameScreenCloseRequestPayload::new
        );

        @Override
        public Id<? extends CustomPayload> getId() {
            return ID;
        }
    }
}
