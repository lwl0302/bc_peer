package cn.mrray.blockchain.core.gossip.detector;

import cn.mrray.blockchain.core.gossip.GossipProps;
import org.apache.commons.math3.distribution.ExponentialDistribution;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 节点的状态探测器
 */
public class StateDetector {

    private static final Logger LOGGER = LoggerFactory.getLogger(StateDetector.class);

    private final DescriptiveStatistics descriptiveStatistics;
    private final long miniSamples;
    private volatile long latestHeartbeat = -1;
    private final String distribution;

    public StateDetector() {
        this.descriptiveStatistics = new DescriptiveStatistics(GossipProps.DETECTOR_WINDOWSIZE);
        this.miniSamples = GossipProps.DETECTOR_MINISAMPLES;
        this.distribution = GossipProps.DETECTOR_DISTRIBUTION;
    }

    /**
     * 记录心跳
     *
     * @param heartbeat
     */
    public synchronized void recordHeartbeat(long heartbeat) {
        if (heartbeat <= latestHeartbeat) {
            return;
        }
        if (latestHeartbeat != -1) {
            descriptiveStatistics.addValue(heartbeat - latestHeartbeat);
        }
        latestHeartbeat = heartbeat;
    }

    public Double computePhiMeasure(long now) {
        if (latestHeartbeat == -1 || descriptiveStatistics.getN() < miniSamples) {
            return null;
        }
        long delta = now - latestHeartbeat;
        double probability;
        if (distribution.equals("normal")) {
            double standardDeviation = descriptiveStatistics.getStandardDeviation();
            standardDeviation = standardDeviation < 0.1 ? 0.1 : standardDeviation;
            probability = new NormalDistribution(descriptiveStatistics.getMean(), standardDeviation).cumulativeProbability(delta);
        } else {
            probability = new ExponentialDistribution(descriptiveStatistics.getMean()).cumulativeProbability(delta);
        }
        final double eps = 1e-12;
        if (1 - probability < eps) {
            probability = 1.0;
        }
        return -1.0d * Math.log10(1.0d - probability);
    }

}
