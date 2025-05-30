package me.jacksonhoggard.holoframes.screen;

import me.jacksonhoggard.holoframes.HologramModelRegistry;
import me.jacksonhoggard.holoframes.network.packet.HologramScreenDataSyncPacket;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

public class HologramScreenHandlerFactory implements ExtendedScreenHandlerFactory<HologramScreenDataSyncPacket.HologramScreenDataPayload> {

    @Override
    public HologramScreenDataSyncPacket.HologramScreenDataPayload getScreenOpeningData(ServerPlayerEntity serverPlayerEntity) {
        return new HologramScreenDataSyncPacket(HologramModelRegistry.models.keySet().toArray(new String[0])).toPayload();
    }

    @Override
    public Text getDisplayName() {
        return Text.translatable("item.holoframes.hologram_model_item.title");
    }

    @Override
    public @Nullable ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        return new HologramScreenHandler(syncId, playerInventory);
    }
}
