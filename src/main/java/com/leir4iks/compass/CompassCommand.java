package com.leir4iks.compass;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scheduler.Cancellable;

import java.util.UUID;

public class CompassCommand implements CommandExecutor {

    private final Compass plugin;

    public CompassCommand(Compass plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by a player.");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            stopTracking(player);
            return true;
        }

        if (args.length == 1) {
            if (player.getInventory().getItemInMainHand().getType() != Material.COMPASS) {
                player.sendMessage(ChatColor.RED + "Чтобы использовать эту команду, возьмите компас в основную руку.");
                return true;
            }

            Player target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                player.sendMessage(ChatColor.RED + "Игрок " + args[0] + " не найден на сервере.");
                return true;
            }

            if (target.equals(player)) {
                player.sendMessage(ChatColor.RED + "Вы не можете отслеживать самого себя.");
                return true;
            }

            stopTracking(player);
            startTracking(player, target);
            return true;
        }

        sender.sendMessage(ChatColor.RED + "Неверное использование. " + command.getUsage());
        return false;
    }

    private void stopTracking(Player player) {
        UUID playerUUID = player.getUniqueId();
        if (plugin.getRunningTasks().containsKey(playerUUID)) {
            plugin.getRunningTasks().get(playerUUID).cancel();
            plugin.getRunningTasks().remove(playerUUID);
        }
        if (plugin.getTrackingData().containsKey(playerUUID)) {
            plugin.getTrackingData().remove(playerUUID);
            player.setCompassTarget(player.getWorld().getSpawnLocation());
            player.sendMessage(ChatColor.GREEN + "Отслеживание с компаса снято.");
        }
    }

    private void startTracking(Player player, Player target) {
        plugin.getTrackingData().put(player.getUniqueId(), target.getUniqueId());
        player.sendMessage(ChatColor.GREEN + "Ваш компас теперь указывает на игрока " + ChatColor.YELLOW + target.getName() + ".");
        player.sendMessage(ChatColor.GRAY + "Чтобы прекратить отслеживание, введите /compass.");

        BukkitTask task = createTask(player, target);
        plugin.getRunningTasks().put(player.getUniqueId(), task);
    }

    private void updateCompassState(Player player, Player initialTarget, Cancellable task) {
        if (!player.isOnline()) {
            task.cancel();
            return;
        }

        boolean holdingCompass = player.getInventory().getItemInMainHand().getType() == Material.COMPASS ||
                                 player.getInventory().getItemInOffHand().getType() == Material.COMPASS;

        if (!holdingCompass) {
            player.setCompassTarget(player.getWorld().getSpawnLocation());
            return;
        }

        UUID targetUUID = plugin.getTrackingData().get(player.getUniqueId());
        if (targetUUID == null) {
            task.cancel();
            return;
        }

        Player target = Bukkit.getPlayer(targetUUID);
        String message;

        if (target == null || !target.isOnline()) {
            message = ChatColor.YELLOW + initialTarget.getName() + ChatColor.WHITE + " | " + ChatColor.GRAY + "оффлайн";
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

    private BukkitTask createTask(Player player, Player initialTarget) {
        if (plugin.isFolia()) {
            return player.getScheduler().runAtFixedRate(plugin, scheduledTask -> {
                updateCompassState(player, initialTarget, scheduledTask);
            }, 1L, 20L);
        } else {
            BukkitRunnable runnable = new BukkitRunnable() {
                @Override
                public void run() {
                    updateCompassState(player, initialTarget, this);
                }
            };
            return runnable.runTaskTimer(plugin, 0L, 20L);
        }
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
