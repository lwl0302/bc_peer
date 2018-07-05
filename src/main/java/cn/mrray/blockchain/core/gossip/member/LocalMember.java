package cn.mrray.blockchain.core.gossip.member;

import cn.mrray.blockchain.core.gossip.detector.StateDetector;

public class LocalMember extends Member {

    // Member状态探测器
    private transient StateDetector detector;

    public LocalMember() {
    }

    public LocalMember(String id, String host, int port, long heartbeat, String cluster) {
        super(id, host, port, heartbeat, cluster);
        detector = new StateDetector();
    }

    public void recoredHeartbeat(long heartbeat) {
        detector.recordHeartbeat(heartbeat);
    }


    public Double detect(long now) {
        return detector.computePhiMeasure(now);
    }

//    @Override
//    public String toString() {
//        return "LocalMember{" +
//                "id='" + id + '\'' +
//                ", uri='" + getUri() + '\'' +
//                ", heartbeat=" + heartbeat +
//                ", cluster='" + cluster + '\'' +
//                ", detect='" + detect(System.nanoTime()) + '\'' +
//                '}';
//    }
}
