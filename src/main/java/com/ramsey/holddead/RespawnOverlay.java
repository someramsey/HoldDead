package com.ramsey.holddead;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Main.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class RespawnOverlay implements IGuiOverlay {
    public static final String OVERLAY_ID = "respawn_overlay";
    public static int remainingTicks = 0;

    @SubscribeEvent
    public static void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END && remainingTicks + 10 > 0) {
            remainingTicks--;
        }
    }

    @Override
    public void render(ForgeGui forgeGui, GuiGraphics guiGraphics, float pPartialTicks, int width, int height) {
        if (remainingTicks > 0) {
            guiGraphics.drawCenteredString(Minecraft.getInstance().font, formatTime(remainingTicks), width / 2, 10, 0xFFFFFFFF);
        }

        final int transitionTicks = remainingTicks + 10;

        if (Config.doRespawnTransitions && transitionTicks > 0) {
            final int fadeout = 10;
            final int fadein = 40;

            if (transitionTicks < fadeout) {
                fill(transitionTicks / (float) fadeout, guiGraphics, width, height);
            } else if (transitionTicks < fadein + fadeout) {
                fill((fadein + fadeout - transitionTicks) / (float) fadein, guiGraphics, width, height);
            }
        }
    }

    private void fill(float alpha, GuiGraphics guiGraphics, int width, int height) {
        final int WHITE = 0x00FFFFFF;

        int alphaInt = (int) (alpha * 255) & 0xFF;
        int color = (alphaInt << 24) | WHITE;

        guiGraphics.fill(0, 0, width, height, color);
    }

    private String formatTime(int ticks) {
        int totalSeconds = ticks / 20;
        int hours = totalSeconds / 3600;
        int minutes = (totalSeconds % 3600) / 60;
        int seconds = totalSeconds % 60;

        StringBuilder stringBuilder = new StringBuilder();

        if (hours > 0) {
            stringBuilder.append(hours).append(":");
        }

        if (minutes < 10) {
            stringBuilder.append("0");
        }

        stringBuilder.append(minutes).append(":");

        if (seconds < 10) {
            stringBuilder.append("0");
        }

        stringBuilder.append(seconds);

        return stringBuilder.toString();
    }
}
