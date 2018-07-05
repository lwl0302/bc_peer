package cn.mrray.blockchain.core.gossip.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.UUID;

public abstract class SysUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(SysUtils.class);

    public static String uuid() {
        return UUID.randomUUID().toString();
    }

    /**
     * 使用样本IP 获取本地ID
     *
     * @param sample 样本IP
     * @return 本地IP地址
     */
    public static String localIp(String sample) {
        try {
            // 截取样本IP的前面部分
            String prefix = sample.substring(0, sample.lastIndexOf("."));
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();
                Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
                while (inetAddresses.hasMoreElements()) {
                    InetAddress inetAddress = inetAddresses.nextElement();
                    String hostAddress = inetAddress.getHostAddress();
                    if (hostAddress.startsWith(prefix)) {
                        return hostAddress;
                    }
                }
            }
        } catch (SocketException e) {
            LOGGER.error("get host ip failed. {}", e);
        }
        return null;
    }

}
