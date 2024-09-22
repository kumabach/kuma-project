package me.kuma.kumaanticheat.listeners;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import me.kuma.kumaanticheat.sampling.PairData;
import java.util.HashMap;
import java.util.UUID;

public class DealDamageListener implements Listener {

    public static HashMap<PairData, Long>hitmap = new HashMap<>();

    @EventHandler
    public void onPlayerHit(EntityDamageByEntityEvent event) {
        if(event.getDamager() instanceof Player){
//            if(event.getDamager() instanceof Player||event.getEntity() instanceof Player)return;
            UUID a_uuid = event.getDamager().getUniqueId();
            UUID b_uuid = event.getEntity().getUniqueId();
            PairData<UUID, UUID> pd1 = new PairData<>(a_uuid, b_uuid);
            PairData<UUID, UUID> pd2 = new PairData<>(b_uuid, a_uuid);
            long t = System.currentTimeMillis();
            hitmap.put(pd1, t);
            hitmap.put(pd2, t);
            Bukkit.getPlayer(a_uuid).sendMessage("a");
        }
    }

}
