package me.jacksonhoggard.holoframes.network;

import me.jacksonhoggard.holoframes.HologramModelRegistry;
import me.jacksonhoggard.holoframes.network.packet.HoloFrameModelDataRequestPacket;
import me.jacksonhoggard.holoframes.network.packet.HoloFrameModelDataSyncPacket;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;

public class ServerHoloFrameModelDataRequestHandler {
    public static void register() {
        ServerPlayNetworking.registerGlobalReceiver(HoloFrameModelDataRequestPacket.HoloFrameModelDataRequestPayload.ID, (payload, context) -> {
            context.server().executeAsync(future -> {
                String hologramFile = payload.hologramFile();
                sendModelDataToClient(context.player(), hologramFile);
            });
        });
    }

    private static void sendModelDataToClient(ServerPlayerEntity player, String hologramFile) {
        HologramModelRegistry.HologramModel model = HologramModelRegistry.models.get(hologramFile);
        if (model != null) {
            HoloFrameModelDataSyncPacket.HoloFrameModelDataPayload payload = new HoloFrameModelDataSyncPacket(model.vertices(), model.texCoords(), model.texture(), hologramFile).toPayload();
            ServerPlayNetworking.send(player, payload);
        }
    }
}
