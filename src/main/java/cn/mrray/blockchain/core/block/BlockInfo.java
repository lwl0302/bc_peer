package cn.mrray.blockchain.core.block;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "t_block_info")
public class BlockInfo {
    @Id
    private int number;
    private String hashPreviousBlock;
    private String hashThisBolck;
    private String channel;

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getHashPreviousBlock() {
        return hashPreviousBlock;
    }

    public void setHashPreviousBlock(String hashPreviousBlock) {
        this.hashPreviousBlock = hashPreviousBlock;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public String getHashThisBolck() {
        return hashThisBolck;
    }

    public void setHashThisBolck(String hashThisBolck) {
        this.hashThisBolck = hashThisBolck;
    }
}
