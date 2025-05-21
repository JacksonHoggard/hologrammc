package me.jacksonhoggard.holoframes;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
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
    }
}
