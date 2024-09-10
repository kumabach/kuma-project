package me.kuma.kumaanticheat.listeners;

import com.sun.org.apache.xpath.internal.operations.Bool;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import javax.xml.bind.Marshaller;
import java.util.HashMap;
import java.util.UUID;

public class dealdamage implements Listener {

    public static HashMap<UUID, Long>hitmap = new HashMap<>();

    @EventHandler
    public void onPlayerHit(EntityDamageByEntityEvent event) {
        if(event.getDamager() instanceof Player){
            UUID uuid = event.getDamager().getUniqueId();
            long t = System.currentTimeMillis();
            hitmap.put(uuid, t);
        }
    }

}
