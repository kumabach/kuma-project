package me.kuma.kumaanticheat.sampling;

public class PacketData {
    public double X;
    public double Y;
    public double Z;
    public int a_hit;
    public int b_hit;

    public PacketData(double x, double y, double z, int a, int b) {
        X = x;
        Z = z;
        Y = y;
        a_hit = a;
        b_hit = b;
    }
}
