package cn.mrray.blockchain.core.gossip.transport;

import cn.mrray.blockchain.core.gossip.GossipProps;
import cn.mrray.blockchain.core.gossip.Gossiper;
import cn.mrray.blockchain.core.gossip.member.LocalMember;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class UdpTransportManager extends AbstractTransportManager implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(UdpTransportManager.class);

    private final DatagramSocket server;
    private final Thread self;
    private final int socketTimeout;
    private final AtomicBoolean keepRunning = new AtomicBoolean(true);

    public UdpTransportManager(Gossiper gossiper) {
        super(gossiper);

        try {
            LocalMember self = gossiper.getSelf();
            SocketAddress socketAddress = new InetSocketAddress(self.getHost(), self.getPort());
            this.server = new DatagramSocket(socketAddress);
        } catch (SocketException e) {
            LOGGER.error("create udp server failed", e);
            throw new RuntimeException(e);
        }
        this.self = new Thread(this);
        this.socketTimeout = GossipProps.CLEANUP_INTERVAL * 2;
    }

    @Override
    public void run() {
        // 接收消息
        while (keepRunning.get()) {
            try {

                // receive message
                byte[] message = read();

                LOGGER.debug("received message");

                gossiper.handleMessage(message);

                // 刷新成员状态
                gossiper.refreshMemberState();

            } catch (IOException e) {
                LOGGER.error("unable to receive message");
                keepRunning.set(false);
            }
        }
    }


    @Override
    public void startEndpoint() {
        self.start();
    }

    @Override
    public void shutdown() {
        keepRunning.set(false);
        server.close();
        super.shutdown();
        self.interrupt();
    }

    @Override
    public void send(URI endpoint, byte[] buf) throws IOException {
        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setSoTimeout(socketTimeout);
            InetAddress dest = InetAddress.getByName(endpoint.getHost());
            DatagramPacket payload = new DatagramPacket(buf, buf.length, dest, endpoint.getPort());
            socket.send(payload);
        }
    }

    @Override
    public byte[] read() throws IOException {
        byte[] buf = new byte[server.getReceiveBufferSize()];
        DatagramPacket p = new DatagramPacket(buf, buf.length);

        // 如果接收不到消息会一直等待
        server.receive(p);
        return p.getData();
    }
}
