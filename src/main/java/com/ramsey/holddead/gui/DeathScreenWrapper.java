package com.ramsey.holddead.gui;

import com.ramsey.holddead.network.PacketHandler;
import com.ramsey.holddead.network.packets.InvokeRespawnPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.*;
import net.minecraft.client.multiplayer.chat.report.ReportingContext;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;

import static net.minecraft.network.chat.Component.translatable;

public class DeathScreenWrapper extends Screen {
    private final Component causeOfDeath;
    private float overlayAlpha;

    public DeathScreenWrapper(DeathScreen pDeathScreen) {
        super(translatable("deathScreen.title"));
        this.causeOfDeath = getCauseOfDeath(pDeathScreen);
        this.overlayAlpha = 1.0F;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    protected void init() {
        this.addRenderableWidget(Button.builder(translatable("deathScreen.spectate"), this::handleSpectateButton)
            .bounds(this.width / 2 - 100, this.height / 4 + 72, 200, 20)
            .build());

        this.addRenderableWidget(Button.builder(translatable("deathScreen.titleScreen"), this::handleExitTitleScreenButton)
            .bounds(this.width / 2 - 100, this.height / 4 + 96, 200, 20)
            .build());
    }

    @Override
    public void render(@NotNull GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        final int gradientStart = 0x60500000;
        final int gradientEnd = 0xA0500000;

        pGuiGraphics.fillGradient(0, 0, this.width, this.height, gradientStart, gradientEnd);

        final int WHITE = 16777215;

        pGuiGraphics.pose().pushPose();
        pGuiGraphics.pose().scale(2.0F, 2.0F, 2.0F);
        pGuiGraphics.drawCenteredString(this.font, this.title, this.width / 2 / 2, 30, WHITE);
        pGuiGraphics.pose().popPose();

        if (this.causeOfDeath != null) {
            pGuiGraphics.drawCenteredString(this.font, this.causeOfDeath, this.width / 2, 85, WHITE);
        }

        super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);

        if (this.overlayAlpha > 0) {
            int alpha = Math.round(this.overlayAlpha * 255) << 24;
            int whiteWithAlpha = (WHITE & 0x00FFFFFF) | alpha;

            pGuiGraphics.fill(0, 0, this.width, this.height, whiteWithAlpha);

            this.overlayAlpha -= pPartialTick / 50;
        }
    }

    private void handleSpectateButton(Button pButton) {
        respawnPlayer();
        pButton.active = false;
    }

    private void handleExitTitleScreenButton(Button pButton) {
        assert this.minecraft != null;

        ReportingContext context = this.minecraft.getReportingContext();
        context.draftReportHandled(this.minecraft, this, () -> {
            ConfirmScreen screen = new DeathScreen.TitleConfirmScreen((pConfirmed) -> {
                if (pConfirmed) {
                    exitToTitleScreen();
                } else {
                    assert this.minecraft != null;
                    this.minecraft.setScreen(this);
                }
            }, translatable("deathScreen.quit.confirm"), CommonComponents.EMPTY, translatable("deathScreen.titleScreen"), translatable("gui.back"));

            this.minecraft.setScreen(screen);
            screen.setDelay(20);
        }, true);
    }

    private void respawnPlayer() {
        assert this.minecraft != null;
        assert this.minecraft.player != null;
        assert this.minecraft.gameMode != null;

        this.minecraft.player.respawn();
        this.minecraft.setScreen(null);

        PacketHandler.sendToServer(new InvokeRespawnPacket());
    }

    private void exitToTitleScreen() {
        assert this.minecraft != null;

        if (this.minecraft.level != null) {
            this.minecraft.level.disconnect();
        }

        this.minecraft.clearLevel(new GenericDirtMessageScreen(translatable("menu.savingLevel")));
        this.minecraft.setScreen(new TitleScreen());
    }

    private static Component getCauseOfDeath(DeathScreen deathScreen) {
        try {
            Field causeOfDeathField = DeathScreen.class.getDeclaredField("causeOfDeath");
            causeOfDeathField.setAccessible(true);

            return (Component) causeOfDeathField.get(deathScreen);
        } catch (NoSuchFieldException | IllegalAccessException err) {
            return null;
        }
    }
}
