package com.ramsey.holddead;

import net.minecraft.world.level.GameRules;

public class GameRuleRegistry {
    public static GameRules.Key<GameRules.IntegerValue> RESPAWN_DELAY;

    public static void register() {
        RESPAWN_DELAY = GameRules.register("respawnDelay", GameRules.Category.SPAWNING, GameRules.IntegerValue.create(5 * 1200));
    }
}
