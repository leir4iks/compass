package com.leir4iks.compass;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import java.util.UUID;

public final class Compass extends JavaPlugin {

    public static NamespacedKey TRACKING_KEY;

    @Override
    public void onEnable() {
        TRACKING_KEY = new NamespacedKey(this, "tracking_target_uuid");
        this.getCommand("compass").setExecutor(new CompassCommand(this));
        startCompassUpdater();
    }

    private void startCompassUpdater() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    ItemStack itemInHand = player.getInventory().getItemInMainHand();

                    if (itemInHand.getType() != Material.COMPASS || !itemInHand.hasItemMeta()) {
                        continue;
                    }

                    ItemMeta meta = itemInHand.getItemMeta();
                    PersistentDataContainer container = meta.getPersistentDataContainer();

                    if (!container.has(TRACKING_KEY, PersistentDataType.STRING)) {
                        continue;
                    }

                    String targetUUIDString = container.get(TRACKING_KEY, PersistentDataType.STRING);
                    UUID targetUUID = UUID.fromString(targetUUIDString);
                    Player target = Bukkit.getPlayer(targetUUID);

                    String message;
                    if (target == null || !target.isOnline()) {
                        String offlineName = Bukkit.getOfflinePlayer(targetUUID).getName();
                        message = ChatColor.YELLOW + (offlineName != null ? offlineName : "Игрок") + ChatColor.WHITE + " | " + ChatColor.GRAY + "оффлайн";
                    } else {
                        player.setCompassTarget(target.getLocation());
                        if (player.getWorld().equals(target.getWorld())) {
                            int distance = (int) player.getLocation().distance(target.getLocation());
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
                    player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(message));
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
