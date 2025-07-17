package com.leir4iks.compass;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class Compass extends JavaPlugin {

    private final Map<UUID, UUID> trackingData = new HashMap<>();

    @Override
    public void onEnable() {
        this.getCommand("compass").setExecutor(new CompassCommand(this));
        startCompassUpdater();
    }

    @Override
    public void onDisable() {
        trackingData.clear();
    }

    public Map<UUID, UUID> getTrackingData() {
        return trackingData;
    }

    private void startCompassUpdater() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Map.Entry<UUID, UUID> entry : trackingData.entrySet()) {
                    Player tracker = Bukkit.getPlayer(entry.getKey());
                    Player target = Bukkit.getPlayer(entry.getValue());

                    if (tracker == null || !tracker.isOnline()) {
                        continue;
                    }

                    boolean holdingCompass = tracker.getInventory().getItemInMainHand().getType() == Material.COMPASS ||
                                             tracker.getInventory().getItemInOffHand().getType() == Material.COMPASS;

                    if (!holdingCompass) {
                        continue;
                    }

                    String message;
                    if (target == null || !target.isOnline()) {
                        String offlineName = Bukkit.getOfflinePlayer(entry.getValue()).getName();
                        message = ChatColor.YELLOW + (offlineName != null ? offlineName : "Игрок") + ChatColor.WHITE + " | " + ChatColor.GRAY + "оффлайн";
                    } else {
                        tracker.setCompassTarget(target.getLocation());
                        if (tracker.getWorld().equals(target.getWorld())) {
                            int distance = (int) tracker.getLocation().distance(target.getLocation());
                            message = ChatColor.YELLOW + target.getName() +
                                      ChatColor.WHITE + " | " +
                                      ChatColor.GREEN + "расстояние: " + distance;
                        } else {
                            String worldName = getWorldDisplayName(target.getWorld().getName());
                            message = ChatColor.YELLOW + target.getName() +
                                      ChatColor.WHITE + " | " +
                                      ChatColor.RED + worldName;
                        }
                    }
                    tracker.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(message));
                }
            }
        }.runTaskTimer(this, 0L, 20L);
    }

    private String getWorldDisplayName(String technicalName) {
        switch (technicalName.toLowerCase()) {
            case "world": return "в обычном мире";
            case "world_nether": return "в незер мире";
            case "world_the_end": return "в эндер мире";
            default: return "в мире " + technicalName;
        }
    }
}
