package me.jacksonhoggard.holoframes.network;

import me.jacksonhoggard.holoframes.HoloframesComponents;
import me.jacksonhoggard.holoframes.network.packet.HoloFrameScreenCloseRequestPacket;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.text.Text;

public class ServerHoloFrameScreenCloseRequestHandler {
    public static void register() {
        ServerPlayNetworking.registerGlobalReceiver(HoloFrameScreenCloseRequestPacket.HoloFrameScreenCloseRequestPayload.ID, (payload, context) -> {
            context.server().executeAsync(
                    future -> {
                        context.player().getMainHandStack().set(HoloframesComponents.SELECTED_HOLOGRAM_FILE, Text.of(payload.hologramFile()));
                        context.player().closeHandledScreen();
                    }
            );
        });
    }
}
