package me.kuma.kumaanticheat;
import me.kuma.kumaanticheat.commands.startSampling;
import me.kuma.kumaanticheat.forsampling.samplingmng;
import me.kuma.kumaanticheat.listeners.dealdamage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

public final class KumaAntiCheat extends JavaPlugin {

    @Override
    public void onEnable() {

        this.getCommand("sampling").setExecutor(new startSampling(this));
        getServer().getPluginManager().registerEvents(new dealdamage(), this);

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

}
