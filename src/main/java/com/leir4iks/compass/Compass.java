package com.leir4iks.compass;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class Compass extends JavaPlugin {

    private final Map<UUID, UUID> trackingData = new HashMap<>();
    private final Map<UUID, BukkitTask> runningTasks = new HashMap<>();
    private boolean isFolia = false;

    @Override
    public void onEnable() {
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            isFolia = true;
        } catch (ClassNotFoundException e) {
            isFolia = false;
        }

        this.getCommand("compass").setExecutor(new CompassCommand(this));
    }

    @Override
    public void onDisable() {
        runningTasks.values().forEach(BukkitTask::cancel);
        runningTasks.clear();
        trackingData.clear();
    }

    public Map<UUID, UUID> getTrackingData() {
        return trackingData;
    }

    public Map<UUID, BukkitTask> getRunningTasks() {
        return runningTasks;
    }

    public boolean isFolia() {
        return isFolia;
    }
}
