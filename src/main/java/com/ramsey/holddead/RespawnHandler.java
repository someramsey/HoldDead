package com.ramsey.holddead;

import com.ramsey.holddead.network.PacketHandler;
import com.ramsey.holddead.network.UpdateTimeoutPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.*;

public class RespawnHandler {
    private static ScheduledExecutorService scheduler;
    private static ConcurrentHashMap<UUID, PlayerTimeout> timeouts;

    public static void init(int pMaxPlayers) {
        scheduler = Executors.newScheduledThreadPool(pMaxPlayers);
        timeouts = new ConcurrentHashMap<>();
    }

    //TODO: Suscribe and block the player respawn event, cancel it and put the player into spectator mod immediately

    public static void dispose() {
        if (scheduler != null) {
            scheduler.shutdown();
        }

        if (timeouts != null) {
            timeouts.clear();
        }
    }

    public static void timeoutPlayer(ServerPlayer pPlayer) {
        GameType oldGameMode = pPlayer.gameMode.getGameModeForPlayer();
//        pPlayer.setGameMode(GameType.SPECTATOR); //TODO: add

        Level level = pPlayer.level();
        UUID playerUUID = pPlayer.getUUID();

        long ticks = level.getGameTime();
        PlayerTimeout timeout = new PlayerTimeout(ticks, oldGameMode);

        int respawnTime = level.getGameRules().getInt(GameRuleRegistry.RESPAWN_DELAY);
        timeout.setSchedule(pPlayer, respawnTime / 20);

        timeouts.put(playerUUID, timeout);

        PacketHandler.sendToPlayer(pPlayer, new UpdateTimeoutPacket(respawnTime));
    }

    public static void cancelPlayerTimeout(ServerPlayer pPlayer) {
        UUID playerUUID = pPlayer.getUUID();

        if (timeouts.containsKey(playerUUID)) {
            PlayerTimeout timeout = timeouts.get(playerUUID);

            timeout.cancelSchedule();
            timeouts.remove(playerUUID);

            PacketHandler.sendToPlayer(pPlayer, new UpdateTimeoutPacket(0));
        }
    }

    public static void onPlayerLeave(ServerPlayer pPlayer) {
        UUID playerUUID = pPlayer.getUUID();

        if (timeouts.containsKey(playerUUID)) {
            PlayerTimeout timeout = timeouts.get(playerUUID);
            timeout.cancelSchedule();
        }
    }

    public static void onPlayerJoin(ServerPlayer pPlayer) {
        UUID playerUUID = pPlayer.getUUID();
        Level playerLevel = pPlayer.level();

        if (timeouts.containsKey(playerUUID)) {
            PlayerTimeout timeout = timeouts.get(playerUUID);

            long currentTime = playerLevel.getGameTime();
            long timePast = currentTime - timeout.deathTime;
            int timeLeft = playerLevel.getGameRules().getInt(GameRuleRegistry.RESPAWN_DELAY) - (int) timePast;

            if (timeLeft < 0) {
                respawnPlayer(pPlayer, timeout.gameMode);
                timeouts.remove(playerUUID);
            } else {
                timeout.setSchedule(pPlayer, timeLeft);
                PacketHandler.sendToPlayer(pPlayer, new UpdateTimeoutPacket(timeLeft));
            }
        }
    }

    private static BlockPos getSpawnpoint(ServerPlayer player) {
        return Objects.requireNonNullElseGet(player.getRespawnPosition(), player.level()::getSharedSpawnPos);
    }

    private static ServerLevel getSpawnLevel(MinecraftServer server, ServerPlayer player) {
        return Objects.requireNonNullElseGet(server.getLevel(player.getRespawnDimension()), server::overworld);
    }

    private static void respawnPlayer(ServerPlayer pPlayer, GameType pGameMode) {
        pPlayer.setGameMode(pGameMode);

        MinecraftServer server = pPlayer.getServer();

        if (server == null) {
            return;
        }

        BlockPos spawnpos = getSpawnpoint(pPlayer);
        ServerLevel level = getSpawnLevel(server, pPlayer);

        pPlayer.teleportTo(level, spawnpos.getX(), spawnpos.getY(), spawnpos.getZ(), 0, 0);
        pPlayer.setDeltaMovement(0, 0, 0);
    }

    private static class PlayerTimeout {
        public ScheduledFuture<?> schedule;
        public final long deathTime;
        public final GameType gameMode;

        private PlayerTimeout(long pDeathTime, GameType pGameMode) {
            this.deathTime = pDeathTime;
            this.gameMode = pGameMode;
        }

        public void setSchedule(ServerPlayer pPlayer, int pTime) {
            this.schedule = scheduler.schedule(() -> {
                respawnPlayer(pPlayer, this.gameMode);
                timeouts.remove(pPlayer.getUUID());
            }, pTime, TimeUnit.SECONDS);
        }

        public void cancelSchedule() {
            this.schedule.cancel(false);
            this.schedule = null;
        }
    }
}
