package cn.mrray.blockchain.core.core.repository;


import cn.mrray.blockchain.core.block.TransactionInfo;

public interface TransactionInfoRepository extends BaseRepository<TransactionInfo> {
    TransactionInfo findByTxId(long txId);

    //TransactionInfo findFirstByNumberOrderByIdDesc(int number);

    TransactionInfo findFirstByNumberOrderBySortDesc(int number);

    TransactionInfo findFirstByOrderByIdDesc();
}
