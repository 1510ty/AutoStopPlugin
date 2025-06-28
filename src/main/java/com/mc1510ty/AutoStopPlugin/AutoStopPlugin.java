package com.mc1510ty.AutoStopPlugin;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public class AutoStopPlugin extends JavaPlugin implements Listener {

    private BukkitTask shutdownTask = null;
    private long shutdownDelayTicks = 20L * 60; // デフォルトは60秒（20 * 秒数）

    @Override
    public void onEnable() {
        saveDefaultConfig();
        reloadConfigFromFile();
        Bukkit.getPluginManager().registerEvents(this, this);

        // 起動時にプレイヤーがいなければシャットダウンスケジュール開始
        if (Bukkit.getOnlinePlayers().isEmpty()) {
            getLogger().info("No players online on startup. Scheduling shutdown...");
            scheduleShutdown();
        }

        getLogger().info("AutoShutdown enabled. Shutdown delay: " + (shutdownDelayTicks / 20L) + " seconds");
    }

    @Override
    public void onDisable() {
        if (shutdownTask != null) {
            shutdownTask.cancel();
        }
        getLogger().info("AutoShutdown disabled.");
    }

    private void reloadConfigFromFile() {
        FileConfiguration config = getConfig();
        int seconds = config.getInt("stop-seconds", 60);
        shutdownDelayTicks = seconds * 20L;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Bukkit.getScheduler().runTaskLater(this, () -> {
            if (Bukkit.getOnlinePlayers().isEmpty()) {
                scheduleShutdown();
            }
        }, 20L); // ログアウト処理後にチェック（1秒遅延）
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        cancelShutdown();
    }

    private void scheduleShutdown() {
        if (shutdownTask != null && !shutdownTask.isCancelled()) return;

        getLogger().info("No players online. Server will shut down in " + (shutdownDelayTicks / 20L) + " seconds...");
        shutdownTask = Bukkit.getScheduler().runTaskLater(this, () -> {
            if (Bukkit.getOnlinePlayers().isEmpty()) {
                getLogger().info("Still no players. Shutting down now.");
                Bukkit.shutdown();
            } else {
                getLogger().info("Players joined. Shutdown cancelled.");
            }
        }, shutdownDelayTicks);
    }

    private void cancelShutdown() {
        if (shutdownTask != null) {
            shutdownTask.cancel();
            shutdownTask = null;
            getLogger().info("Player joined. Shutdown task cancelled.");
        }
    }
}