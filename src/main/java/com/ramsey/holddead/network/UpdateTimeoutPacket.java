package com.ramsey.holddead.network;

import com.ramsey.holddead.RespawnOverlay;
import io.netty.buffer.ByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class UpdateTimeoutPacket {
    private final int remainingTicks;

    public UpdateTimeoutPacket(int pRemainingTicks) {
        this.remainingTicks = pRemainingTicks;
    }

    public UpdateTimeoutPacket(ByteBuf buf) {
        this.remainingTicks = buf.readInt();
    }

    public void encode(ByteBuf buf) {
        buf.writeInt(this.remainingTicks);
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();

        context.enqueueWork(() -> RespawnOverlay.remainingTicks = this.remainingTicks);

        context.setPacketHandled(true);
    }
}
