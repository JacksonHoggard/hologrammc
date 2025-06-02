package me.jacksonhoggard.holoframes.client;

import me.jacksonhoggard.holoframes.HoloframesComponents;
import me.jacksonhoggard.holoframes.HoloframesTypes;
import me.jacksonhoggard.holoframes.client.network.ClientHoloFrameModelDataSyncReceiver;
import me.jacksonhoggard.holoframes.item.HoloframesItems;
import me.jacksonhoggard.holoframes.screen.HologramScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Objects;

public class HoloframesClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ClientHoloFrameModelDataSyncReceiver.register();

        ItemTooltipCallback.EVENT.register(((itemStack, tooltipContext, tooltipType, list) -> {
            if(!itemStack.isOf(HoloframesItems.HOLOGRAM_MODEL_ITEM))
                return;
            list.add(Text.translatable("item.holoframes.hologram_model_item.tooltip").formatted(Formatting.AQUA));
            Text selectedFile = itemStack.getOrDefault(HoloframesComponents.SELECTED_HOLOGRAM_FILE, Text.of(""));
            if(!selectedFile.getString().isEmpty())
                list.add(selectedFile.copy().formatted(Formatting.GOLD));
        }));

        HandledScreens.register(HoloframesTypes.HOLOGRAM_SCREEN_HANDLER_TYPE, HologramScreen::new);
    }
}
