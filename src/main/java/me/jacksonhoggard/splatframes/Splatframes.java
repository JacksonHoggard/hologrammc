package me.jacksonhoggard.splatframes;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;

public class Splatframes implements ModInitializer {
    public static final String MOD_ID = "splatframes";

    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register(((commandDispatcher, commandRegistryAccess, registrationEnvironment) -> {
            commandDispatcher.register(CommandManager.literal("splatframe")
                    .then(CommandManager.argument("file", HologramCommand.FILE_ARGUMENT)
                            .executes(commandContext -> HologramCommand.executeSetSplat(commandContext))
                    )
            );
        }));
    }
}
