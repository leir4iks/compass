package com.leir4iks.compass;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

public class CompassFolia {

    public static BukkitTask schedule(Compass plugin, Player player, Player initialTarget, CompassCommand command) {
        return player.getScheduler().runAtFixedRate(plugin, scheduledTask -> {
            command.updateCompassState(player, initialTarget, scheduledTask);
        }, 1L, 20L);
    }
}
