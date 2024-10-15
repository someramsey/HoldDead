package com.ramsey.holddead.events;

import com.ramsey.holddead.Main;
import com.ramsey.holddead.RespawnHandler;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Main.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public abstract class PlayerEvents {
    @SubscribeEvent
    public static void onPlayerChangeGameMode(PlayerEvent.PlayerChangeGameModeEvent event) {
        if (event.getNewGameMode() != GameType.SPECTATOR) {
            RespawnHandler.cancelPlayerTimeout((ServerPlayer) event.getEntity());
        }
    }

    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        RespawnHandler.onPlayerJoin((ServerPlayer) event.getEntity());
    }
}
