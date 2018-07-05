package cn.mrray.blockchain.core.socket.body;

import cn.mrray.blockchain.core.core.model.Transaction;

public class RpcTransactionBody extends BaseBody {

    private Transaction transaction;

    public RpcTransactionBody() {
        super();
    }

    public RpcTransactionBody(Transaction transaction) {
        super();
        this.transaction = transaction;
    }

    public Transaction getTransaction() {
        return transaction;
    }

    public void setTransaction(Transaction transaction) {
        this.transaction = transaction;
    }
}
