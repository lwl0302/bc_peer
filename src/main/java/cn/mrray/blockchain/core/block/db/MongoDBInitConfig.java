package cn.mrray.blockchain.core.block.db;

import cn.mrray.blockchain.core.util.PropertiesPo;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.lightcouch.CouchDbClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author wuweifeng wrote on 2018/3/13.
 */
@Configuration
public class MongoDBInitConfig {
    private static final PropertiesPo configProper = new PropertiesPo();
    @Bean
    public DBCollection mongoDB() {
//        Mongo conn = new Mongo("192.168.125.8" , 27017);
//        DB mydb = conn.getDB("new_database");
//        DBCollection dbCollection = mydb.getCollection("test");
        Mongo conn = new Mongo(configProper.getValueByKey("mongodb.ip")  , Integer.parseInt(configProper.getValueByKey("mongodb.port")));
        DB mydb = conn.getDB(configProper.getValueByKey("mongodb.database"));
        DBCollection dbCollection = mydb.getCollection(configProper.getValueByKey("mongodb.collection"));
//        // 连接到 mongodb 服务
//        MongoClient mongoClient = new MongoClient( configProper.getValueByKey("mongodb.ip") , Integer.parseInt(configProper.getValueByKey("mongodb.port")) );
//
//        // 连接到数据库
//        MongoDatabase mongoDatabase = mongoClient.getDatabase(configProper.getValueByKey("mongodb.database"));
//
//        MongoCollection<Document> collection = mongoDatabase.getCollection(configProper.getValueByKey("mongodb.collection"));
//

        return dbCollection;
    }
}
