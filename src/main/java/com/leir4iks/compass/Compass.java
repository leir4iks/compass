package com.leir4iks.compass;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CompassMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import java.util.UUID;

public final class Compass extends JavaPlugin {

    public static NamespacedKey TRACKING_KEY;

    @Override
    public void onEnable() {
        TRACKING_KEY = new NamespacedKey(this, "tracking_target_uuid");
        this.getCommand("compass").setExecutor(new CompassCommand());
        startCompassUpdater();
    }

    private void startCompassUpdater() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    updatePlayerCompass(player);
                }
            }
        }.runTaskTimer(this, 0L, 20L);
    }

    private void updatePlayerCompass(Player owner) {
        ItemStack item = owner.getInventory().getItemInMainHand();

        if (item.getType() != Material.COMPASS || !(item.getItemMeta() instanceof CompassMeta meta)) {
            return;
        }

        if (!meta.getPersistentDataContainer().has(TRACKING_KEY, PersistentDataType.STRING)) {
            return;
        }

        String targetUUIDString = meta.getPersistentDataContainer().get(TRACKING_KEY, PersistentDataType.STRING);
        UUID targetUUID = UUID.fromString(targetUUIDString);
        Player target = Bukkit.getPlayer(targetUUID);

        String message;
        if (target != null && target.isOnline()) {
            meta.setLodestone(target.getLocation());
            meta.setLodestoneTracked(false);
            if (owner.getWorld().equals(target.getWorld())) {
                int distance = (int) owner.getLocation().distance(target.getLocation());
                message = ChatColor.YELLOW + target.getName() + ChatColor.WHITE + " | " + ChatColor.GREEN + "расстояние: " + distance;
            } else {
                String worldName = getWorldDisplayName(target.getWorld().getName());
                message = ChatColor.YELLOW + target.getName() + ChatColor.WHITE + " | " + ChatColor.RED + worldName;
            }
        } else {
            meta.setLodestone(null);
            String offlineName = Bukkit.getOfflinePlayer(targetUUID).getName();
            message = ChatColor.YELLOW + (offlineName != null ? offlineName : "Игрок") + ChatColor.WHITE + " | " + ChatColor.GRAY + "оффлайн";
        }

        item.setItemMeta(meta);
        owner.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(message));
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
