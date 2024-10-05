package com.ramsey.holddead.events;

import com.ramsey.holddead.DeathScreenWrapper;
import com.ramsey.holddead.Main;
import com.ramsey.holddead.RespawnOverlay;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@OnlyIn(Dist.CLIENT)
public class ClientEvents {
    @Mod.EventBusSubscriber(modid = Main.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ModBusEvents {
        @SubscribeEvent
        public static void registerGuiOverlays(RegisterGuiOverlaysEvent event) {
            event.registerAboveAll(RespawnOverlay.OVERLAY_ID, new RespawnOverlay());
        }
    }

    @Mod.EventBusSubscriber(modid = Main.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
    public static class ForgeBusEvents {
        @SubscribeEvent(priority = EventPriority.LOWEST)
        public static void onScreenOpen(ScreenEvent.Opening event) {
            Screen screen = event.getScreen();

            if (screen instanceof DeathScreen deathScreen) {
                Minecraft minecraft = Minecraft.getInstance();

                boolean isOpen = minecraft.screen instanceof DeathScreenWrapper;
                LocalPlayer player = minecraft.player;

                if (player != null && !isOpen) {
                    event.setNewScreen(new DeathScreenWrapper(deathScreen));
                }
            }
        }
    }
}
