package me.jacksonhoggard.holoframes.client;

import me.jacksonhoggard.holoframes.client.network.ClientHoloFrameModelDataSyncReceiver;
import net.fabricmc.api.ClientModInitializer;

public class HoloframesClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ClientHoloFrameModelDataSyncReceiver.register();
    }
}
