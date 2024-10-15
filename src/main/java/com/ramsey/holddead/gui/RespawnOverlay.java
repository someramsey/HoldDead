package com.ramsey.holddead.gui;

import com.ramsey.holddead.Main;
import com.ramsey.holddead.network.PacketHandler;
import com.ramsey.holddead.network.packets.RespawnStatePacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Main.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class RespawnOverlay implements IGuiOverlay {
    public static final String OVERLAY_ID = "respawn_overlay";

    private static long respawnTime = 0;
    private static float transitionAlpha = 0.0f;
    private static long transitionStartTime = 0;
    private static TransitionState transitionState = TransitionState.Idle;

    private static final int TRANSITION_TIMEOUT_DURATION = 5000;

    public static void beginRespawnTransition() {
        transitionState = TransitionState.In;
        transitionStartTime = System.currentTimeMillis();
        transitionAlpha = 0.0f;
    }

    public static void endRespawnTransition() {
        transitionState = TransitionState.Out;
        transitionAlpha = 1.0f;
    }

    public static void setRespawnTime(long time) {
        respawnTime = time;
    }

    @Override
    public void render(ForgeGui forgeGui, GuiGraphics guiGraphics, float pPartialTicks, int width, int height) {
        long currentTimeMillis = System.currentTimeMillis();
        long remainingTime = respawnTime - currentTimeMillis;

        if (remainingTime > 0) {
            guiGraphics.drawCenteredString(forgeGui.getFont(), formatTime(remainingTime), width / 2, 10, 0xFFFFFFFF);
        }

        if (transitionState == TransitionState.Waiting) {
            long timePast = currentTimeMillis - transitionStartTime;

            if (timePast >= TRANSITION_TIMEOUT_DURATION) {
                Main.LOGGER.warn("Cancelling respawn transition, server did not respond in time");
                resetTransition();
            }

            fill(transitionAlpha, guiGraphics, width, height);
        } else if (transitionState == TransitionState.In) {
            if (transitionAlpha >= 1.0f) {
                stageUpTransition();
            } else {
                transitionAlpha += pPartialTicks / 40f;
            }

            fill(transitionAlpha, guiGraphics, width, height);
        } else if (transitionState == TransitionState.Out) {
            transitionAlpha -= pPartialTicks / 40f;

            if (transitionAlpha <= 0.0f) {
                resetTransition();
                return;
            }

            fill(transitionAlpha, guiGraphics, width, height);
        }
    }

    private static void resetTransition() {
        transitionState = TransitionState.Idle;
        transitionAlpha = 0.0f;
    }

    private static void stageUpTransition() {
        transitionState = TransitionState.Waiting;
        transitionAlpha = 1.0f;
        PacketHandler.sendToServer(new RespawnStatePacket(RespawnStatePacket.State.ACKNOWLEDGE_RESPAWN));
    }

    private void fill(float alpha, GuiGraphics guiGraphics, int width, int height) {
        final int WHITE = 0x00FFFFFF;

        int alphaInt = (int) (alpha * 255) & 0xFF;
        int color = (alphaInt << 24) | WHITE;

        guiGraphics.fill(0, 0, width, height, color);
    }

    private String formatTime(long ticks) {
        long totalSeconds = ticks / 1000;
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;

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

    public enum TransitionState {
        In,
        Out,
        Idle,
        Waiting
    }
}
