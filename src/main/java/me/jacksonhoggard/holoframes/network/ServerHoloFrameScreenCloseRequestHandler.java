package me.jacksonhoggard.holoframes.network;

import me.jacksonhoggard.holoframes.network.packet.HoloFrameScreenCloseRequestPacket;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public class ServerHoloFrameScreenCloseRequestHandler {
    public static void register() {
        ServerPlayNetworking.registerGlobalReceiver(HoloFrameScreenCloseRequestPacket.HoloFrameScreenCloseRequestPayload.ID, (payload, context) -> {
            context.server().executeAsync(future -> context.player().closeHandledScreen());
        });
    }
}
