package me.kuma.kumaanticheat.forsampling;

import me.kuma.kumaanticheat.listeners.dealdamage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static sun.swing.MenuItemLayoutHelper.max;

public class samplingmng {

    public static HashMap<pairdata, samplingmng> sampmap = new HashMap<>();

    private List<packetdata> xlist = new ArrayList<>();
    public BukkitRunnable task;
    private JavaPlugin plugin;
    public UUID a_uuid;
    public UUID b_uuid;
    public static Integer lastFileNum;

    static final long clt = 500;

    // コンストラクタでJavaPluginのインスタンスを受け取る
    public samplingmng(JavaPlugin plugin, pairdata pd) {
        this.plugin = plugin;
        a_uuid = (UUID) pd.getKey();
        b_uuid = (UUID) pd.getValue();
    }

    public void startTracking() {

        if (task != null) {
            task.cancel(); // すでにタスクが存在する場合はキャンセル
        }

        Player A = Bukkit.getPlayer(this.a_uuid);
        Player B = Bukkit.getPlayer(this.b_uuid);

        task = new BukkitRunnable() {
            @Override
            public void run() {

                double ax = A.getLocation().getX();
                double ay = A.getLocation().getY();
                double az = A.getLocation().getZ();

                double bx = B.getLocation().getX();
                double by = B.getLocation().getY();
                double bz = B.getLocation().getZ();

                ax -= bx;
                ay -= by;
                az -= bz;

                long t = System.currentTimeMillis(); // System.currentTimeMillis() を System.nanoTime() に変更

                int a_flag;
                int b_flag;

                if (dealdamage.hitmap.get(a_uuid) == null) a_flag = 0;
                else {
                    long lasthit = dealdamage.hitmap.get(a_uuid);
                    a_flag = (t - lasthit > clt ? 0 : 1);
                }

                A.sendMessage(String.valueOf(a_flag));

                if (dealdamage.hitmap.get(b_uuid) == null) b_flag = 0;
                else {
                    long lasthit = dealdamage.hitmap.get(b_uuid);
                    b_flag = (t - lasthit > clt ? 0 : 1);
                }


                // 座標データをリストに追加
                xlist.add(new packetdata(ax, ay, az, a_flag, b_flag));
            }
        };

        task.runTaskTimer(this.plugin, 0L, 1L); // プラグインインスタンスを渡す

    }

    public void stopTracking() {
        if (task != null) {
            task.cancel(); // タスクをキャンセルして停止
            task = null; // タスクをnullに設定
            makecsv();
            xlist = new ArrayList<>();
        }
    }

    public void makecsv() {
        //csvファイルの保存
        Player p = Bukkit.getPlayer(this.a_uuid);
        if(lastFileNum == null) getLastname();
        lastFileNum++;
        p.sendMessage(String.valueOf(lastFileNum));
        File csvFile = new File(plugin.getDataFolder(), "data_" + lastFileNum + ".csv");

        // ファイルが存在しない場合は作成
        try {
            // 親ディレクトリが存在しない場合は作成
            csvFile.getParentFile().mkdirs();
            // ファイルを作成
            csvFile.createNewFile();

            // データを書き込む
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(csvFile, true))) {

                writer.write("index,X,Y,Z,a,b");
                writer.newLine();

                int idx = 0;

                for (packetdata pd : xlist) {
                    double x = pd.X;
                    double y = pd.Y;
                    double z = pd.Z;
                    double a = pd.a_hit;
                    double b = pd.b_hit;
                    writer.write(String.valueOf(idx) + "," + String.valueOf(x) + "," + String.valueOf(y) + "," + String.valueOf(z) + "," + String.valueOf(a) + "," + String.valueOf(b));
                    writer.newLine();
                    idx++;
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void getLastname() {
        File[] files = this.plugin.getDataFolder().listFiles((dir, name) -> name.startsWith("data_") && name.endsWith(".csv"));
        lastFileNum = 0;

        if (files == null || files.length == 0) return;

        for (File f : files) {
            String name = f.getName();
            String n = name.substring(5, name.length() - 4);
            lastFileNum = max(lastFileNum, Integer.parseInt(n));
        }
    }

}
