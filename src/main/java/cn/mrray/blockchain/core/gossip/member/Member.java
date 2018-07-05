package cn.mrray.blockchain.core.gossip.member;

import java.net.URI;

public class Member implements Comparable<Member> {

    protected String id;

    protected String host;

    protected int port;

    protected volatile long heartbeat;

    protected String cluster;

    public Member() {
    }

    public Member(String id, String host, int port, long heartbeat, String cluster) {
        this.id = id;
        this.host = host;
        this.port = port;
        this.heartbeat = heartbeat;
        this.cluster = cluster;
    }

    @Override
    public int compareTo(Member other) {
        return this.computeAddress().compareTo(other.computeAddress());
    }

    public String computeAddress() {
        return String.format("udp://%s:%s", host, port);
    }

    public URI getUri() {
        return URI.create(String.format("udp://%s:%s", this.host, this.port));
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public long getHeartbeat() {
        return heartbeat;
    }

    public void setHeartbeat(long heartbeat) {
        this.heartbeat = heartbeat;
    }

    public String getCluster() {
        return cluster;
    }

    public void setCluster(String cluster) {
        this.cluster = cluster;
    }

    @Override
    public String toString() {
        return "Member{" +
                "id='" + id + '\'' +
                ", host='" + host + '\'' +
                ", port=" + port +
                ", heartbeat=" + heartbeat +
                ", cluster='" + cluster + '\'' +
                '}';
    }
}
