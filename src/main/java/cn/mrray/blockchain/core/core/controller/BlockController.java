package cn.mrray.blockchain.core.core.controller;

import cn.mrray.blockchain.core.core.service.QueryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/block")
public class BlockController {
    @Resource
    private QueryService queryService;

    @GetMapping("/findBlockByHash")
    public ResponseEntity findBlockByHash(String hash) {
        return ResponseEntity.ok(queryService.findBlockByHash(hash));
    }

    @GetMapping("/findBlockByNumber")
    public ResponseEntity findBlockByNumber(int number) {
        return ResponseEntity.ok(queryService.findBlockByNumber(number));
    }

    @GetMapping("/findTransactionByTxId")
    public ResponseEntity findTransactionByTxId(long txId) {
        return ResponseEntity.ok(queryService.findTransactionByTxId(txId));
    }

    @GetMapping("/findBlockByTxId")
    public ResponseEntity findBlockByTxId(long txId) {
        return ResponseEntity.ok(queryService.findBlockByTxId(txId));
    }
}
