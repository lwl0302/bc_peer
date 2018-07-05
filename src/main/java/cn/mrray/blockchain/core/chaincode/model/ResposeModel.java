package cn.mrray.blockchain.core.chaincode.model;

import java.io.Serializable;
import cn.mrray.blockchain.core.core.model.Transaction;

public class ResposeModel implements Serializable {
    private String value;
    private Transaction transaction;

    public String getValue() {
        return value;
    }

    public ResposeModel setValue(String value) {
        this.value = value;
        return this;
    }

    public Transaction getTransaction() {
        return transaction;
    }

    public ResposeModel setTransaction(Transaction transaction) {
        this.transaction = transaction;
        return this;
    }
}
