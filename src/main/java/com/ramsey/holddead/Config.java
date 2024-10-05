package com.ramsey.holddead;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

@Mod.EventBusSubscriber(modid = Main.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    private static final ForgeConfigSpec.BooleanValue respawnTimeProp = BUILDER
        .comment("Wether or not to apply screen effects when respawning")
        .define("doRespawnTransitions", true);

    public static final ForgeConfigSpec SPEC = BUILDER.build();

    public static boolean doRespawnTransitions;

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        doRespawnTransitions = respawnTimeProp.get();
    }
}
