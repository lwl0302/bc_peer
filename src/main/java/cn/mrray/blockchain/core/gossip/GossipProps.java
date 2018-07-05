package cn.mrray.blockchain.core.gossip;

public class GossipProps {

    // Gossip 发送同步请求的间隔
    public static final int GOSSIP_INTERVAL = 10;

    // 判断Member 状态的时间差
    public static final int CLEANUP_INTERVAL = 5000;

    // 统计样本的阈值 5 ~ 12 为理想范围
    public static final int DETECTOR_THRESHOLD = 10;

    // 统计样本的最小数
    public static final int DETECTOR_MINISAMPLES = 5;

    // 统计样本的最小数
    public static final int DETECTOR_WINDOWSIZE = 5000;


    public static final String DETECTOR_DISTRIBUTION = "normal";

}
