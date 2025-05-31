package me.jacksonhoggard.holoframes.item;

import me.jacksonhoggard.holoframes.Holoframes;
import me.jacksonhoggard.holoframes.HoloframesComponents;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.function.Function;

public class HoloframesItems {
    private HoloframesItems() {
    }

    public static final Item HOLOGRAM_MODEL_ITEM = register(
            "hologram_model_item",
            HologramModelItem::new,
            new Item.Settings()
                    .component(HoloframesComponents.SELECTED_HOLOGRAM_FILE, Text.of(""))
    );

    public static Item register(String path, Function<Item.Settings, Item> factory, Item.Settings settings) {
        final RegistryKey<Item> registryKey = RegistryKey.of(RegistryKeys.ITEM, Identifier.of(Holoframes.MOD_ID, path));
        return Items.register(registryKey, factory, settings);
    }

    public static void initialize() {
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL).register(content -> {
            content.addAfter(Items.ITEM_FRAME, HOLOGRAM_MODEL_ITEM);
        });
    }
}
