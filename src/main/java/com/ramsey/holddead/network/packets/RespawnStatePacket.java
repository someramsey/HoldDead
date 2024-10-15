package com.ramsey.holddead.network.packets;

import com.ramsey.holddead.RespawnHandler;
import com.ramsey.holddead.gui.RespawnOverlay;
import io.netty.buffer.ByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class RespawnStatePacket {
    private final State state;

    public RespawnStatePacket(State state) {
        this.state = state;
    }

    public RespawnStatePacket(ByteBuf buf) {
        this.state = State.VALUES[buf.readInt()];
    }

    public void encode(ByteBuf buf) {
        buf.writeInt(this.state.ordinal());
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();

        switch (this.state) {
            case BEGIN_RESPAWN:
                RespawnOverlay.beginRespawnTransition();
                break;
            case ACKNOWLEDGE_RESPAWN:
                acknowledgeRespawn(context);
                break;
            case END_RESPAWN:
                RespawnOverlay.endRespawnTransition();
                break;
            case CANCEL_RESPAWN:
                RespawnOverlay.setRespawnTime(0);
                break;
        }

        context.setPacketHandled(true);
    }

    private static void acknowledgeRespawn(NetworkEvent.Context context) {
        ServerPlayer player = context.getSender();

        if (player != null) {
            RespawnHandler.onAcknowledgePlayerRespawn(player);
        }
    }

    public enum State {
        BEGIN_RESPAWN,
        ACKNOWLEDGE_RESPAWN,
        END_RESPAWN,
        CANCEL_RESPAWN;

        static final State[] VALUES = values();
    }
}
