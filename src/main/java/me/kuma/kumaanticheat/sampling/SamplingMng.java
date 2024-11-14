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

import static com.google.common.primitives.UnsignedBytes.min;
import static sun.swing.MenuItemLayoutHelper.max;

public class SamplingMng {

    static final long clt = 100;
    static final int datasize = 91;
    static final int damage_hit_count_threshold = 10;
    public static HashMap<PairData, SamplingMng> sampmap = new HashMap<>();
    public static Integer lastFileNum;
    public Deque<PacketData> dataList = new ArrayDeque<>();
    public BukkitRunnable task;
    public UUID a_uuid;
    public UUID b_uuid;
    private final JavaPlugin plugin;
    private int hitCounter;
    public int playerType;

    // コンストラクタでJavaPluginのインスタンスを受け取る
    public SamplingMng(JavaPlugin plugin, PairData pd) {
        this.plugin = plugin;
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

                dataList.addLast(new PacketData(a_flag, b_flag, ax, ay, az, bx, by, bz));
                if (a_flag == 1) hitCounter++;

                if(ax== 0 && az == 0) {
                    hitCounter = 0;
                    dataList.clear();
                }

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
            lastFileNum = null;
        }
    }

    public void makeCsv() {
        // CSVファイルの保存
        Player p = Bukkit.getPlayer(this.a_uuid);

        // プレイヤータイプのディレクトリを作成
        File playerTypeDir = new File(plugin.getDataFolder(), "PlayerType_" + playerType);
        playerTypeDir.mkdirs();

        if (lastFileNum == null) getLastname(playerTypeDir);
        lastFileNum++;

        File csvFile = new File(playerTypeDir, "type_" + playerType + "_" + lastFileNum + ".csv");

        try {
            csvFile.createNewFile();

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(csvFile, true))) {
                writer.write("r,dt,dy,a,b,ax,ay,az,bx,by,bz");
                writer.newLine();

                double fx = dataList.getFirst().X;
                double fz = dataList.getFirst().Z;

                double lastTheta = Math.atan2(fz, fx);
                double lasty = dataList.getFirst().Y;
                double lastr = Math.sqrt(fx * fx + fz * fz);

                dataList.removeFirst();

                for (PacketData pd : dataList) {

                    double a = pd.a_hit;
                    double b = pd.b_hit;

                    double ax = pd.Ax;
                    double ay = pd.Ay;
                    double az = pd.Az;
                    double bx = pd.Bx;
                    double by = pd.By;
                    double bz = pd.Bz;

                    //aaaa

                    double r = Math.sqrt(x * x + z * z);

                    double theta1, theta2, theta3;

                    theta1 = Math.atan2(z, x);
                    theta2 = theta1 - Math.PI * 2;
                    theta3 = theta1 + Math.PI * 2;

                    double dt1 = Math.abs(lastTheta - theta1);
                    double dt2 = Math.abs(lastTheta - theta2);
                    double dt3 = Math.abs(lastTheta - theta3);


                    double dt = Math.min(Math.min(dt1, dt2),dt3);

                    lastTheta = theta1;

                    //

                    double xx =



                    writer.write(r + "," + dt + "," + y + "," + a + "," + b + "," + ax + "," + ay + "," + az + "," + bx + "," + by + "," + bz);
                    writer.newLine();
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void getLastname(File td) {
        File[] files = td.listFiles((dir, name) -> name.startsWith("type_") && name.endsWith(".csv"));

        lastFileNum = 0;

        if (files == null || files.length == 0) return;

        for (File f : files) {
            String name = f.getName();
            String n = name.substring(5, name.length() - 4);
            lastFileNum = max(lastFileNum, Integer.parseInt(n));
        }
    }

}
