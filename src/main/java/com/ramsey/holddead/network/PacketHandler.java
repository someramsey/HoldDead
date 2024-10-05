package com.ramsey.holddead.network;

import com.ramsey.holddead.Main;
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
        INSTANCE.messageBuilder(UpdateTimeoutPacket.class, 0)
            .encoder(UpdateTimeoutPacket::encode)
            .decoder(UpdateTimeoutPacket::new)
            .consumerMainThread(UpdateTimeoutPacket::handle)
            .add();
    }

    public static void sendToPlayer(ServerPlayer player, Object packet) {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), packet);
    }
}
