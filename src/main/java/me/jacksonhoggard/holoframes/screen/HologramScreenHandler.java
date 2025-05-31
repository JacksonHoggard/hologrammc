package me.jacksonhoggard.holoframes.screen;

import me.jacksonhoggard.holoframes.HoloframesTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;

public class HologramScreenHandler extends ScreenHandler {
    private final String[] hologramFiles;

    public HologramScreenHandler(int syncId, PlayerInventory playerInventory, String hologramFiles) {
        super(HoloframesTypes.HOLOGRAM_SCREEN_HANDLER_TYPE, syncId);
        this.hologramFiles = hologramFiles.split(",");
    }

    public HologramScreenHandler(int syncId, PlayerInventory playerInventory) {
        super(HoloframesTypes.HOLOGRAM_SCREEN_HANDLER_TYPE, syncId);
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
