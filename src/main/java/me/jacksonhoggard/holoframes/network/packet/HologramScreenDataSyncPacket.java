package me.jacksonhoggard.holoframes.network.packet;

import me.jacksonhoggard.holoframes.Holoframes;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

public class HologramScreenDataSyncPacket implements IPacket<HologramScreenDataSyncPacket.HologramScreenDataPayload> {
    public static final Identifier ID = Identifier.of(Holoframes.MOD_ID, "screen_data_sync");

    public final String hologramFiles;

    public HologramScreenDataSyncPacket(String[] hologramFiles) {
        this.hologramFiles = serializeHologramFiles(hologramFiles);
    }

    public static void register() {
        PayloadTypeRegistry.playS2C().register(HologramScreenDataPayload.ID, HologramScreenDataPayload.CODEC);
    }

    private static String serializeHologramFiles(String[] hologramFiles) {
        return String.join(",", hologramFiles);
    }

    public record HologramScreenDataPayload(String hologramFiles) implements CustomPayload {
        public static final CustomPayload.Id<HologramScreenDataPayload> ID = new CustomPayload.Id<>(HologramScreenDataSyncPacket.ID);
        public static final PacketCodec<RegistryByteBuf, HologramScreenDataPayload> CODEC = PacketCodec.tuple(
                PacketCodecs.STRING, HologramScreenDataPayload::hologramFiles,
                HologramScreenDataPayload::new
        );

        @Override
        public Id<? extends CustomPayload> getId() {
            return ID;
        }
    }

    public @NotNull HologramScreenDataPayload toPayload() {
        return new HologramScreenDataPayload(this.hologramFiles);
    }
}
