package me.jacksonhoggard.holoframes;

import me.jacksonhoggard.holoframes.network.HoloFrameModelDataRequestPacket;
import me.jacksonhoggard.holoframes.network.HoloFrameModelDataSyncPacket;
import me.jacksonhoggard.holoframes.network.ServerHoloFrameModelDataRequestHandler;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.server.command.CommandManager;

public class Holoframes implements ModInitializer {
    public static final String MOD_ID = "holoframes";

    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register(((commandDispatcher, commandRegistryAccess, registrationEnvironment) -> {
            commandDispatcher.register(CommandManager.literal("holoframes")
                    .then(CommandManager.argument("file", HologramCommand.FILE_ARGUMENT)
                            .executes(commandContext -> HologramCommand.executeSetHologram(commandContext))
                    )
            );
        }));
        HoloFrameModelDataSyncPacket.register();
        HoloFrameModelDataRequestPacket.register();
        HologramModelRegistry.register();
        ServerHoloFrameModelDataRequestHandler.register();
    }
}
