package me.jacksonhoggard.holoframes;

import me.jacksonhoggard.holoframes.item.HoloframesItems;
import me.jacksonhoggard.holoframes.network.*;
import me.jacksonhoggard.holoframes.network.packet.HoloFrameModelDataRequestPacket;
import me.jacksonhoggard.holoframes.network.packet.HoloFrameModelDataSyncPacket;
import me.jacksonhoggard.holoframes.network.packet.HoloFrameScreenCloseRequestPacket;
import me.jacksonhoggard.holoframes.network.packet.HologramScreenDataSyncPacket;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.loader.impl.util.log.Log;
import net.fabricmc.loader.impl.util.log.LogCategory;
import net.minecraft.server.command.CommandManager;

public class Holoframes implements ModInitializer {
    public static final String MOD_ID = "holoframes";

    @Override
    public void onInitialize() {
        Log.info(LogCategory.LOG, "Initializing Holoframes");
        CommandRegistrationCallback.EVENT.register(((commandDispatcher, commandRegistryAccess, registrationEnvironment) -> {
            commandDispatcher.register(CommandManager.literal("holoframes")
                    .requires((player) -> player.hasPermissionLevel(2))
                    .then(CommandManager.argument("file", HologramCommand.FILE_ARGUMENT)
                            .executes(HologramCommand::executeSetHologram)
                    )
            );
        }));
        // Register network packets
        HoloFrameModelDataSyncPacket.register();
        HoloFrameModelDataRequestPacket.register();
        HologramModelRegistry.register();
        ServerHoloFrameModelDataRequestHandler.register();
        HologramScreenDataSyncPacket.register();
        HoloFrameScreenCloseRequestPacket.register();
        ServerHoloFrameScreenCloseRequestHandler.register();

        // Register items
        HoloframesComponents.initialize();
        HoloframesItems.initialize();
    }
}
