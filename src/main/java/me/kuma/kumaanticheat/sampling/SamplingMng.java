package me.kuma.kumaanticheat.sampling;

import me.kuma.kumaanticheat.listeners.DealDamageListener;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.UUID;

import static sun.swing.MenuItemLayoutHelper.max;

public class SamplingMng {

    static final long clt = 500;
    static final int datasize = 300;
    static final int damage_hit_count_threshold = 100;
    public static HashMap<PairData, SamplingMng> sampmap = new HashMap<>();
    public static Integer lastFileNum;
    public Deque<PacketData> dataList = new ArrayDeque<>();
    public BukkitRunnable task;
    public UUID a_uuid;
    public UUID b_uuid;
    private final JavaPlugin plugin;
    private int hitCounter;

    // コンストラクタでJavaPluginのインスタンスを受け取る
    public SamplingMng(JavaPlugin plugin, PairData pd) {
        this.plugin = plugin;
        a_uuid = (UUID) pd.getKey();
        b_uuid = (UUID) pd.getValue();
    }

    public void startTracking() {

        hitCounter = 0;

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

                PairData<UUID, UUID> pd1 = new PairData<>(a_uuid, b_uuid);
                PairData<UUID, UUID> pd2 = new PairData<>(b_uuid, a_uuid);

                if (DealDamageListener.hitmap.get(pd1) == null) {
                    a_flag = 0;
                } else {
                    long lasthit = DealDamageListener.hitmap.get(pd1);
                    a_flag = (t - lasthit > clt ? 0 : 1);
                }
                if (DealDamageListener.hitmap.get(pd2) == null) {
                    b_flag = 0;
                } else {
                    long lasthit = DealDamageListener.hitmap.get(pd2);
                    b_flag = (t - lasthit > clt ? 0 : 1);
                }

                // 座標データをリストに追加
                dataList.addLast(new PacketData(ax, ay, az, a_flag, b_flag));
                if (a_flag == 1) hitCounter++;

                if (dataList.size() == datasize) {
                    if (hitCounter > damage_hit_count_threshold) {
                        makeCsv();
                        dataList.clear();
                    } else {
                        dataList.removeFirst();
                    }
                }

            }


        };

        task.runTaskTimer(this.plugin, 0L, 5L); // プラグインインスタンスを渡す

    }

    private int getListSize() {
        return dataList.size();
    }

    public void stopTracking() {
        if (task != null) {
            task.cancel(); // タスクをキャンセルして停止
            task = null; // タスクをnullに設定
            dataList.clear();
        }
    }

    public void makeCsv() {
        //csvファイルの保存
        Player p = Bukkit.getPlayer(this.a_uuid);
        if (lastFileNum == null) getLastname();
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

                for (PacketData pd : dataList) {
                    double x = pd.X;
                    double y = pd.Y;
                    double z = pd.Z;
                    double a = pd.a_hit;
                    double b = pd.b_hit;
                    writer.write(idx + "," + x + "," + y + "," + z + "," + a + "," + b);
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
