package me.jacksonhoggard.holoframes.network.packet;

import net.minecraft.network.packet.CustomPayload;
import org.jetbrains.annotations.NotNull;

public interface IPacket<D extends CustomPayload> {

    static void register() {}

    @NotNull D toPayload();

}
