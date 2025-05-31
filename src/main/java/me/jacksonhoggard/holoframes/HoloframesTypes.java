package me.jacksonhoggard.holoframes;

import me.jacksonhoggard.holoframes.network.packet.HologramScreenDataSyncPacket;
import me.jacksonhoggard.holoframes.screen.HologramScreenHandler;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.fabricmc.loader.impl.util.log.Log;
import net.fabricmc.loader.impl.util.log.LogCategory;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class HoloframesTypes {

    public static final ExtendedScreenHandlerType<HologramScreenHandler, HologramScreenDataSyncPacket.HologramScreenDataPayload> HOLOGRAM_SCREEN_HANDLER_TYPE =
            Registry.register(
                    Registries.SCREEN_HANDLER,
                    Identifier.of(Holoframes.MOD_ID, "hologram_screen_handler"),
                    new ExtendedScreenHandlerType<>(
                            (syncId, playerInventory, buf) ->
                                    new HologramScreenHandler(syncId, playerInventory, buf.hologramFiles()),
                            HologramScreenDataSyncPacket.HologramScreenDataPayload.CODEC
                    )
            );

    public static void initialize() {
        Log.info(LogCategory.LOG, "Registering Holoframes Types");
    }
}
