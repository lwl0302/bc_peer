package cn.mrray.blockchain.core.block.db;

import org.rocksdb.Options;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DbInitConfig {
    static {
        RocksDB.loadLibrary();
    }

    @Bean
    public RocksDB rocksDB() {
        Options options = new Options().setCreateIfMissing(true);
        try {
            return RocksDB.open(options, "./rocksDB");
        } catch (RocksDBException e) {
            e.printStackTrace();
            return null;
        }
    }
}
