package cn.mrray.blockchain.core.kafka;

public class QueueTimmer {
    private static volatile long lastAdd = System.currentTimeMillis();

    private static volatile long lastPack = System.currentTimeMillis() + 30000;

    private static volatile boolean pbftWorking = false;

    public static boolean isPbftWorking() {
        return pbftWorking;
    }

    public static void setPbftWorking(boolean pbftWorking) {
        QueueTimmer.pbftWorking = pbftWorking;
    }

    public static long getLastPack() {
        return lastPack;
    }

    public static void setLastPack(long lastPack) {
        QueueTimmer.lastPack = lastPack;
    }

    public static long getLastAdd() {
        return lastAdd;
    }

    public static void setLastAdd(long lastAdd) {
        QueueTimmer.lastAdd = lastAdd;
    }
}
