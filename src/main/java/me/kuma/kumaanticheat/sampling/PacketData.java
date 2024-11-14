package me.kuma.kumaanticheat.sampling;

public class PacketData {
    public double X;
    public double Y;
    public double Z;
    public double Ax, Ay, Az;
    public double Bx, By, Bz;
    public int a_hit;
    public int b_hit;

    public PacketData(int a, int b, double ax, double ay, double az, double bx, double by, double bz) {
        a_hit = a;
        b_hit = b;
        Ax = ax;
        Ay = ay;
        Az = az;
        Bx = bx;
        By = bz;
        Bz = bz;
    }
}
