package com.ramsey.holddead.network;

import com.ramsey.holddead.Main;
import com.ramsey.holddead.network.packets.InvokeRespawnPacket;
import com.ramsey.holddead.network.packets.RespawnStatePacket;
import com.ramsey.holddead.network.packets.SyncTimeoutPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public class PacketHandler {
    public static final SimpleChannel INSTANCE = NetworkRegistry.ChannelBuilder
        .named(new ResourceLocation(Main.MODID, "main"))
        .serverAcceptedVersions((version) -> true)
        .clientAcceptedVersions((version) -> true)
        .networkProtocolVersion(() -> "1")
        .simpleChannel();

    public static void register() {
        INSTANCE.messageBuilder(SyncTimeoutPacket.class, 0)
            .encoder(SyncTimeoutPacket::encode)
            .decoder(SyncTimeoutPacket::new)
            .consumerMainThread(SyncTimeoutPacket::handle).add();

        INSTANCE.messageBuilder(InvokeRespawnPacket.class, 1)
            .encoder((packet, buf) -> {})
            .decoder((buf) -> new InvokeRespawnPacket())
            .consumerMainThread(InvokeRespawnPacket::handle).add();

        INSTANCE.messageBuilder(RespawnStatePacket.class, 2)
            .encoder(RespawnStatePacket::encode)
            .decoder(RespawnStatePacket::new)
            .consumerMainThread(RespawnStatePacket::handle).add();
    }

    public static void sendToPlayer(ServerPlayer player, Object packet) {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), packet);
    }

    public static void sendToServer(Object packet) {
        INSTANCE.sendToServer(packet);
    }
}
