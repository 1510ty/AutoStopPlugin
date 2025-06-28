package com.mc1510ty.AutoStopPlugin;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public class AutoStopPlugin extends JavaPlugin implements Listener {

    private BukkitTask shutdownTask = null;
    private final long SHUTDOWN_DELAY_TICKS = 20L * 60; // 60秒

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
        getLogger().info("AutoShutdown enabled.");
    }

    @Override
    public void onDisable() {
        if (shutdownTask != null) {
            shutdownTask.cancel();
        }
        getLogger().info("AutoShutdown disabled.");
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Bukkit.getScheduler().runTaskLater(this, () -> {
            if (Bukkit.getOnlinePlayers().isEmpty()) {
                scheduleShutdown();
            }
        }, 20L); // 1秒後にチェック（ログアウト処理後）
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        cancelShutdown();
    }

    private void scheduleShutdown() {
        if (shutdownTask != null && !shutdownTask.isCancelled()) return;

        getLogger().info("No players online. Server will shut down in 60 seconds...");
        shutdownTask = Bukkit.getScheduler().runTaskLater(this, () -> {
            if (Bukkit.getOnlinePlayers().isEmpty()) {
                getLogger().info("Still no players. Shutting down now.");
                Bukkit.shutdown();
            } else {
                getLogger().info("Players joined. Shutdown cancelled.");
            }
        }, SHUTDOWN_DELAY_TICKS);
    }

    private void cancelShutdown() {
        if (shutdownTask != null) {
            shutdownTask.cancel();
            shutdownTask = null;
            getLogger().info("Player joined. Shutdown task cancelled.");
        }
    }
}
