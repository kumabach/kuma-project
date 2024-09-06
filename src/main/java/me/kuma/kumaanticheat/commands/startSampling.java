package me.kuma.kumaanticheat.commands;

import me.kuma.kumaanticheat.KumaAntiCheat;
import me.kuma.kumaanticheat.forsampling.samplingmng;
import me.kuma.kumaanticheat.forsampling.pairdata;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import java.util.UUID;

public class startSampling implements CommandExecutor {

    private final KumaAntiCheat plugin;

    // コンストラクタでKumaAntiCheatのインスタンスを受け取る
    public startSampling(KumaAntiCheat plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if(args.length!=3){
            sender.sendMessage("/sampling start/stop playerA playerB");
            return false;
        }

        Player a = Bukkit.getPlayer(args[1]);
        Player b = Bukkit.getPlayer(args[2]);

        if(a==null||b==null){
            sender.sendMessage(ChatColor.RED + "No such player.");
            return false;
        }

        int result = args[1].compareToIgnoreCase(args[2]);

        if(result<0){
            Player r = a;
            a=b;
            b=r; //swap
        }

        UUID a_uuid = a.getUniqueId();
        UUID b_uuid = b.getUniqueId();
        pairdata<UUID, UUID> pd = new pairdata<>(a_uuid, b_uuid);

        if(args[0].equalsIgnoreCase("start")){
            if(!startsamp(pd)){
                sender.sendMessage(ChatColor.RED + "The task has already started!");
                return false;
            }
        }
        else if(args[0].equalsIgnoreCase("stop")){
            if(!stopsamp(pd)){
                sender.sendMessage(ChatColor.RED + "The task does not exist!");
                return false;
            }
        }
        else return false;
        return true;
    }

    private boolean startsamp(pairdata pd){
        samplingmng s = samplingmng.sampmap.get(pd);
        if(s==null){
            samplingmng newsamp = new samplingmng(this.plugin,pd);
            samplingmng.sampmap.put(pd, newsamp);
        } else if (s.task!=null)return false;

        samplingmng.sampmap.get(pd).startTracking();
        return true;
    }

    private boolean stopsamp(pairdata pd){
        samplingmng s = samplingmng.sampmap.get(pd);
        if(s==null||s.task==null)return false;
        samplingmng.sampmap.get(pd).stopTracking();
        return true;
    }
}
