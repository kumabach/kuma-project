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

    static final long clt = 100;
    static final int datasize = 91;
    static final int damage_hit_count_threshold = 10;
    static final double pi = 3.1415926353;
    public static HashMap<PairData, SamplingMng> sampmap = new HashMap<>();
    public static Integer lastFileNum;
    public Deque<PacketData> dataList = new ArrayDeque<>();
    public BukkitRunnable task;
    public UUID a_uuid;
    public UUID b_uuid;
    private final JavaPlugin plugin;
    private int hitCounter;
    int playerType;

    // コンストラクタでJavaPluginのインスタンスを受け取る
    public SamplingMng(JavaPlugin plugin, PairData pd, int type) {
        this.plugin = plugin;
        this.playerType = type;
        a_uuid = (UUID) pd.getKey();
        b_uuid = (UUID) pd.getValue();
    }

    public void startTracking() {

        hitCounter = 0;

        if (task != null) {
            task.cancel();
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

                long t = System.currentTimeMillis();

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

                dataList.addLast(new PacketData(ax, ay, az, a_flag, b_flag));
                if (a_flag == 1) hitCounter++;

                A.sendMessage(String.valueOf(a_flag));
                B.sendMessage(String.valueOf(a_flag));

                if (dataList.size() == datasize) {
                    if (hitCounter > damage_hit_count_threshold) {
                        makeCsv();
                        hitCounter = 0;
                        dataList.clear();
                    } else {
                        if(dataList.getFirst().a_hit == 1)hitCounter--;
                        dataList.removeFirst();
                    }
                }

            }


        };

        task.runTaskTimer(this.plugin, 0L, 1L);

    }

    public void stopTracking() {
        if (task != null) {
            task.cancel();
            task = null;
            dataList.clear();
        }
    }

    public void makeCsv() {
        // CSVファイルの保存
        Player p = Bukkit.getPlayer(this.a_uuid);
        if (lastFileNum == null) getLastname();
        lastFileNum++;

        // プレイヤータイプのディレクトリを作成
        File playerTypeDir = new File(plugin.getDataFolder(), "PlayerType_" + playerType);
        playerTypeDir.mkdirs();

        File csvFile = new File(playerTypeDir, "data_" + lastFileNum + ".csv");

        try {
            csvFile.createNewFile();

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(csvFile, true))) {
                writer.write("index,dr,dtheta,dy,a,b");
                writer.newLine();

                int idx = 0;
                double fx = dataList.getFirst().X;
                double fz = dataList.getFirst().Z;

                double lastTheta = Math.atan2(fz, fx);
                double lasty = dataList.getFirst().Y;
                double lastr = Math.sqrt(fx * fx + fz * fz);

                dataList.removeFirst();

                for (PacketData pd : dataList) {
                    double x = pd.X;
                    double y = pd.Y;
                    double z = pd.Z;
                    double a = pd.a_hit;
                    double b = pd.b_hit;

                    double r = Math.sqrt(x * x + z * z);
                    double theta1 = Math.atan2(z, x); // polar変換
                    double theta2, theta3;

                    theta2 = theta1 - pi * 2;
                    theta3 = theta1 + pi * 2;

                    double now = theta1;
                    double dt = lastTheta - theta1;

                    if (abs(dt) > abs(lastTheta - theta2)) {
                        dt = lastTheta - theta2;
                        now = theta2;
                    }

                    if (abs(dt) > abs(lastTheta - theta3)) {
                        dt = lastTheta - theta3;
                        now = theta3;
                    }

                    double dr = r - lastr;
                    double dtheta = dt;
                    double dy = y - lasty;

                    lastTheta = now;
                    lastr = r;
                    lasty = y;

                    writer.write(idx + "," + dr + "," + dtheta + "," + dy + "," + a + "," + b);
                    writer.newLine();
                    idx++;
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private double abs(double a) {
        if(a<(double)0)return (double)-1*a;
        else return a;
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
