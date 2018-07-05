package cn.mrray.blockchain.core.block.db;

import org.lightcouch.CouchDbClient;
import org.rocksdb.Options;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author wuweifeng wrote on 2018/3/13.
 */
@Configuration
public class CouchDBInitConfig {

    @Bean
    public CouchDbClient couchDB() {
        CouchDbClient dbClient = new CouchDbClient("ConfigInfo.properties");
        return dbClient;
    }
}
