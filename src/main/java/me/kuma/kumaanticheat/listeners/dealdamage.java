package me.kuma.kumaanticheat.listeners;

import com.sun.org.apache.xpath.internal.operations.Bool;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import me.kuma.kumaanticheat.forsampling.pairdata;
import java.util.HashMap;
import java.util.UUID;

public class dealdamage implements Listener {

    public static HashMap<pairdata, Long>hitmap = new HashMap<>();

    @EventHandler
    public void onPlayerHit(EntityDamageByEntityEvent event) {
        if(event.getDamager() instanceof Player){
//            if(event.getDamager() instanceof Player||event.getEntity() instanceof Player)return;
            UUID a_uuid = event.getDamager().getUniqueId();
            UUID b_uuid = event.getEntity().getUniqueId();
            pairdata<UUID, UUID> pd1 = new pairdata<>(a_uuid, b_uuid);
            pairdata<UUID, UUID> pd2 = new pairdata<>(b_uuid, a_uuid);
            long t = System.currentTimeMillis();
            hitmap.put(pd1, t);
            hitmap.put(pd2, t);
            Bukkit.getPlayer(a_uuid).sendMessage("a");
        }
    }

}
