package com.leir4iks.compass;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

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
            if (plugin.getTrackingData().remove(player.getUniqueId()) != null) {
                player.setCompassTarget(player.getWorld().getSpawnLocation());
                player.sendMessage(ChatColor.GREEN + "Отслеживание с компаса снято.");
            } else {
                player.sendMessage(ChatColor.YELLOW + "Вы и так никого не отслеживали. Используйте " + ChatColor.AQUA + "/compass <ник>");
            }
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

            plugin.getTrackingData().put(player.getUniqueId(), target.getUniqueId());
            player.sendMessage(ChatColor.GREEN + "Ваш компас теперь указывает на игрока " + ChatColor.YELLOW + target.getName() + ".");
            player.sendMessage(ChatColor.GRAY + "Чтобы прекратить отслеживание, введите /compass.");
            return true;
        }

        sender.sendMessage(ChatColor.RED + "Неверное использование. " + command.getUsage());
        return false;
    }
}
