package cn.mrray.blockchain.core.block.db;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * Created by Viki on 2018-06-25.
 */
@Component
public class DataBaseServiceImpl implements DataBaseService {
    @Value("${DBconfig}")
    private int DBconfig;
    @Resource
    private CouchDBStore couchDBStore;
    @Resource
    private DbStore dbStore;
    @Resource
    private MongoDBStore mongoDBStore;

    @Override
    public void put(String key, String value) {
        if(DBconfig==1){
            dbStore.put(key,value);
        }
        else if(DBconfig==2){
            couchDBStore.put(key,value);
        }
        else if(DBconfig==3){
            mongoDBStore.put(key,value);
        }

    }

    @Override
    public String get(String key) {
        String result = "";
        if(DBconfig==1){
            result = dbStore.get(key);
        }
        else if(DBconfig==2){
            result= couchDBStore.get(key);
        }else if(DBconfig==3){
            mongoDBStore.get(key);
        }
        return result;
    }

    @Override
    public void update(String key, String value) {
        if(DBconfig==1){
            dbStore.put(key,value);
        }
        else if(DBconfig==2){
            couchDBStore.update(key,value);
        }else if(DBconfig==3){
            mongoDBStore.update(key,value);
        }
    }

    @Override
    public String select(String key) {
        String result = "";
        if(DBconfig==1){
            result="不支持复合查询";
        }
        else if(DBconfig==2){
            result=couchDBStore.select(key);
        }else if(DBconfig==3){
            mongoDBStore.select(key);
        }
        return  result;
    }

    @Override
    public void remove(String key) {
        if(DBconfig==1){
            dbStore.remove(key);
        }
        else if(DBconfig==2){

        }else if(DBconfig==3){
            mongoDBStore.remove(key);
        }
    }
}
