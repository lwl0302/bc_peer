package cn.mrray.blockchain.core.core.manager;

import cn.mrray.blockchain.core.block.Block;
import cn.mrray.blockchain.core.core.service.QueryService;
import cn.mrray.blockchain.core.socket.body.RpcSimpleBlockBody;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class DbBlockManager {
    @Resource
    private QueryService queryService;
    @Resource
    private BlockManager blockManager;
    @Value("${countPerBlock}")
    private int countPerBlock;

    private Block getBlockByNumber(int number) {
        return queryService.findBlockByNumber(number);
    }

    public RpcSimpleBlockBody getLastBlockInfo() {
        RpcSimpleBlockBody rpcSimpleBlockBody = new RpcSimpleBlockBody();
        int height = blockManager.getHeight();
        if (height == 0) {
            rpcSimpleBlockBody.setHash("first block");
            rpcSimpleBlockBody.setNumber(0);
            rpcSimpleBlockBody.setSort(-1);
            return rpcSimpleBlockBody;
        } else {
            rpcSimpleBlockBody.setHash(blockManager.getHashPreviousBlock());
            rpcSimpleBlockBody.setNumber(height);
            rpcSimpleBlockBody.setSort(blockManager.getSort());
            return rpcSimpleBlockBody;
        }
    }

    public Block getBlockByBlockInfo(RpcSimpleBlockBody rpcSimpleBlockBody) {
        int number = rpcSimpleBlockBody.getNumber();
        int sort = rpcSimpleBlockBody.getSort();
        //TransactionInfo transactionInfo = transactionInfoRepository.findFirstByNumberOrderBySortDesc(number);
        int localSort = blockManager.getSort();
        int localNumber = blockManager.getHeight();
        if (number < localNumber) {
            if (sort == countPerBlock) {
                return getBlockByNumber(number + 1);
            } else {
                return getBlockByNumber(number);
            }
        } else if (number == localNumber) {
            if (sort < localSort) {
                return getBlockByNumber(number);
            }
        }
        return null;
    }
}
