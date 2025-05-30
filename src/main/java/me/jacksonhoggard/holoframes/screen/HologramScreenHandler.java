package me.jacksonhoggard.holoframes.screen;

import me.jacksonhoggard.holoframes.network.packet.HologramScreenDataSyncPacket;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.screen.ScreenHandler;

public class HologramScreenHandler extends ScreenHandler {

    public static final String ID = "hologram";
    public static final ExtendedScreenHandlerType<HologramScreenHandler, HologramScreenDataSyncPacket.HologramScreenDataPayload> TYPE =
            Registry.register(
                    Registries.SCREEN_HANDLER,
                    ID,
                    new ExtendedScreenHandlerType<>(
                            (syncId, playerInventory, buf) ->
                                    new HologramScreenHandler(syncId, playerInventory, buf.hologramFiles()),
                            HologramScreenDataSyncPacket.HologramScreenDataPayload.CODEC
                    )
            );

    private final String[] hologramFiles;

    public HologramScreenHandler(int syncId, PlayerInventory playerInventory, String hologramFiles) {
        super(TYPE, syncId);
        this.hologramFiles = hologramFiles.split(",");
    }

    public HologramScreenHandler(int syncId, PlayerInventory playerInventory) {
        super(TYPE, syncId);
        this.hologramFiles = new String[0];
    }

    public String[] getHologramFiles() {
        return hologramFiles;
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slot) {
        return null;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return true;
    }
}
