package com.ramsey.holddead.network.packets;

import com.ramsey.holddead.gui.RespawnOverlay;
import io.netty.buffer.ByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SyncTimeoutPacket {
    private final long respawnTime;

    public SyncTimeoutPacket(long pEndTime) {
        this.respawnTime = pEndTime;
    }

    public SyncTimeoutPacket(ByteBuf buf) {
        this.respawnTime = buf.readLong();
    }

    public void encode(ByteBuf buf) {
        buf.writeLong(this.respawnTime);
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.setPacketHandled(true);

        RespawnOverlay.setRespawnTime(this.respawnTime);
    }
}
