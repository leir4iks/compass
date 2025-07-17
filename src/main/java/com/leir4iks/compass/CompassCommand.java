package com.leir4iks.compass;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CompassMeta;
import org.bukkit.persistence.PersistentDataType;

public class CompassCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command can only be used by a player.");
            return true;
        }

        ItemStack itemInHand = player.getInventory().getItemInMainHand();
        if (itemInHand.getType() != Material.COMPASS) {
            player.sendMessage(ChatColor.RED + "Возьмите компас в руку, чтобы использовать эту команду.");
            return true;
        }

        if (!(itemInHand.getItemMeta() instanceof CompassMeta meta)) {
            return true;
        }

        if (args.length == 0) {
            if (meta.getPersistentDataContainer().has(Compass.TRACKING_KEY, PersistentDataType.STRING)) {
                meta.getPersistentDataContainer().remove(Compass.TRACKING_KEY);
                meta.setLodestone(null);
                meta.setLodestoneTracked(false);
                meta.setDisplayName(null);
                itemInHand.setItemMeta(meta);
                player.sendMessage(ChatColor.GREEN + "Компас в вашей руке больше ни за кем не следит.");
            } else {
                player.sendMessage(ChatColor.YELLOW + "Этот компас и так ни за кем не следил.");
            }
            return true;
        }

        if (args.length == 1) {
            Player target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                player.sendMessage(ChatColor.RED + "Игрок " + args[0] + " не найден на сервере.");
                return true;
            }

            if (target.equals(player)) {
                player.sendMessage(ChatColor.RED + "Вы не можете отслеживать самого себя.");
                return true;
            }

            meta.getPersistentDataContainer().set(Compass.TRACKING_KEY, PersistentDataType.STRING, target.getUniqueId().toString());
            meta.setDisplayName(ChatColor.AQUA + "Компас (Отслеживает: " + target.getName() + ")");
            itemInHand.setItemMeta(meta);

            player.sendMessage(ChatColor.GREEN + "Компас в вашей руке теперь указывает на игрока " + ChatColor.YELLOW + target.getName() + ".");
            return true;
        }

        sender.sendMessage(ChatColor.RED + "Неверное использование. " + command.getUsage());
        return false;
    }
}
