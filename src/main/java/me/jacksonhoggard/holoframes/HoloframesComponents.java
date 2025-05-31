package me.jacksonhoggard.holoframes;

import net.fabricmc.loader.impl.util.log.Log;
import net.fabricmc.loader.impl.util.log.LogCategory;
import net.minecraft.component.ComponentType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;
import net.minecraft.util.Identifier;

public class HoloframesComponents {

    public static final ComponentType<Text> SELECTED_HOLOGRAM_FILE = Registry.register(
            Registries.DATA_COMPONENT_TYPE,
            Identifier.of(Holoframes.MOD_ID, "hologram_model_item_selected_file"),
            ComponentType.<Text>builder().codec(TextCodecs.CODEC)
                    .packetCodec(TextCodecs.REGISTRY_PACKET_CODEC)
                    .cache()
                    .build()
    );

    public static void initialize() {
        Log.info(LogCategory.LOG, "Registering Holoframes Components", Holoframes.MOD_ID);
    }

}
