package me.kuma.kumaanticheat;
import me.kuma.kumaanticheat.commands.SamplingCommand;
import me.kuma.kumaanticheat.listeners.DealDamageListener;
import org.bukkit.plugin.java.JavaPlugin;

public final class KumaAntiCheat extends JavaPlugin {

    @Override
    public void onEnable() {

        this.getCommand("sampling").setExecutor(new SamplingCommand(this));
        getServer().getPluginManager().registerEvents(new DealDamageListener(), this);

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

}
