package com.ramsey.holddead.network.packets;

import com.ramsey.holddead.RespawnHandler;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class InvokeRespawnPacket {
    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        ServerPlayer player = context.getSender();

        if (player != null) {
            RespawnHandler.timeoutPlayer(player);
        }

        context.setPacketHandled(true);
    }
}
