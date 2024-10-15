package com.ramsey.holddead;

import com.ramsey.holddead.network.PacketHandler;
import com.ramsey.holddead.network.packets.RespawnStatePacket;
import com.ramsey.holddead.network.packets.SyncTimeoutPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

//TODO: Add a mixin to player death and put the local player immeditely in spectator mode on the client side to avoid latency
public class RespawnHandler {
    private static ConcurrentHashMap<UUID, PlayerTimeout> timeouts;
    private static ScheduledExecutorService scheduler;
    private static MinecraftServer server;
    private static boolean initialized = false;

    public static void init(MinecraftServer pServer) {
        if (initialized) {
            return;
        }

        initialized = true;

        server = pServer;
        timeouts = new ConcurrentHashMap<>(pServer.getMaxPlayers());
        scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(RespawnHandler::tick, 0, 1, TimeUnit.SECONDS);
    }

    public static void dispose() {
        if (!initialized) {
            return;
        }

        initialized = false;

        if (timeouts != null) {
            timeouts.clear();
        }

        if (scheduler != null) {
            scheduler.shutdown();
        }

        if (server != null) {
            server = null;
        }
    }

    private static void tick() {
        long currentTimeMillis = System.currentTimeMillis();

        for (PlayerTimeout timeout : timeouts.values()) {
            if (timeout.canEnd(currentTimeMillis)) {
                PacketHandler.sendToPlayer(timeout.player, new RespawnStatePacket(RespawnStatePacket.State.BEGIN_RESPAWN));
                timeout.ended = true;
            }
        }
    }

    public static void timeoutPlayer(ServerPlayer pPlayer) {
        GameType oldGameMode = pPlayer.gameMode.getGameModeForPlayer();
        Level level = pPlayer.level();
        UUID playerUUID = pPlayer.getUUID();

        int respawnDelay = GameRuleRegistry.getRespawnDelayMillis(level.getGameRules());
        long currentTimeMillis = System.currentTimeMillis();

        long respawnTime = currentTimeMillis + respawnDelay;

        PlayerTimeout timeout = new PlayerTimeout(respawnTime, oldGameMode, pPlayer);
        timeouts.put(playerUUID, timeout);

        pPlayer.setGameMode(GameType.SPECTATOR);
        PacketHandler.sendToPlayer(pPlayer, new SyncTimeoutPacket(respawnTime));
    }

    public static void cancelPlayerTimeout(ServerPlayer pPlayer) {
        UUID playerUUID = pPlayer.getUUID();

        if (timeouts.containsKey(playerUUID)) {
            timeouts.remove(playerUUID);

            PacketHandler.sendToPlayer(pPlayer, new RespawnStatePacket(RespawnStatePacket.State.CANCEL_RESPAWN));
        }
    }

    public static void onPlayerJoin(ServerPlayer pPlayer) {
        UUID playerUUID = pPlayer.getUUID();

        if (timeouts.containsKey(playerUUID)) {
            PlayerTimeout timeout = timeouts.get(playerUUID);

            if (timeout.ended) {
                timeouts.remove(playerUUID);
                respawnPlayer(pPlayer, timeout.gameMode);

            } else {
                timeout.player = pPlayer;
                PacketHandler.sendToPlayer(pPlayer, new SyncTimeoutPacket(timeout.respawnTime));
            }
        }
    }

    public static void onAcknowledgePlayerRespawn(ServerPlayer pPlayer) {
        UUID playerUUID = pPlayer.getUUID();

        if (timeouts.containsKey(playerUUID)) {
            PlayerTimeout timeout = timeouts.get(playerUUID);

            if(!timeout.ended) {
                return;
            }

            respawnPlayer(pPlayer, timeout.gameMode);
            PacketHandler.sendToPlayer(pPlayer, new RespawnStatePacket(RespawnStatePacket.State.END_RESPAWN));

            timeouts.remove(playerUUID);
        }
    }

    private static void respawnPlayer(ServerPlayer pPlayer, GameType gameMode) {
        pPlayer.setGameMode(gameMode);

        BlockPos blockpos = pPlayer.getRespawnPosition();
        float angle = pPlayer.getRespawnAngle();
        ServerLevel serverlevel = server.getLevel(pPlayer.getRespawnDimension());

        if (serverlevel == null) {
            serverlevel = server.overworld();
        }

        if(blockpos == null) {
            blockpos = serverlevel.getSharedSpawnPos();
        }

        pPlayer.teleportTo(serverlevel, blockpos.getX() + 0.5D, blockpos.getY(), blockpos.getZ() + 0.5D, angle, 0.0F);
    }

    private static class PlayerTimeout {
        public final long respawnTime;
        public final GameType gameMode;
        public ServerPlayer player;
        public boolean ended;

        public PlayerTimeout(long respawnTime, GameType gameMode, ServerPlayer player) {
            this.respawnTime = respawnTime;
            this.gameMode = gameMode;
            this.player = player;
        }

        public boolean canEnd(long currentTimeMillis) {
            return !ended && respawnTime < currentTimeMillis;
        }
    }
}
