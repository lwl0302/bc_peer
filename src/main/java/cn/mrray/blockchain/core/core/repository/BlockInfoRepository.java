package cn.mrray.blockchain.core.core.repository;


import cn.mrray.blockchain.core.block.BlockInfo;

public interface BlockInfoRepository extends BaseRepository<BlockInfo> {
    //BlockInfo findByNumber(int number);

    BlockInfo findByHashThisBolck(String hash);

    //BlockInfo findByHashPreviousBlock(String hash);

    BlockInfo findFirstByOrderByNumberDesc();

    //BlockInfo findFirstByOrderByNumber();
}