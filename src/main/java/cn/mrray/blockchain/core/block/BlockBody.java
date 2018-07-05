package cn.mrray.blockchain.core.block;

import cn.mrray.blockchain.core.core.model.Transaction;

import java.util.List;

/**
 * 区块body，里面存放交易的数组
 */
public class BlockBody {
    private List<Transaction> instructions;

    @Override
    public String toString() {
        return "BlockBody{" +
                "instructions=" + instructions +
                '}';
    }

    public List<Transaction> getInstructions() {
        return instructions;
    }

    public void setInstructions(List<Transaction> instructions) {
        this.instructions = instructions;
    }
}
