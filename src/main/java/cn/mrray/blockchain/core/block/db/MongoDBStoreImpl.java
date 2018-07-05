package cn.mrray.blockchain.core.block.db;

import cn.mrray.blockchain.core.core.model.Version;
import cn.mrray.blockchain.core.core.model.VersionedValue;
import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.*;

/**
 * rocksDB对于存储接口的实现
 * @author wuweifeng wrote on 2018/3/13.
 */
@Component
public class MongoDBStoreImpl implements MongoDBStore {
    @Resource
    private DBCollection mongoDB;
    ObjectMapper objectMapper = new ObjectMapper();
    private Gson gson = new Gson();
    private org.slf4j.Logger logger = LoggerFactory.getLogger(getClass());
    @Override
    public void put(String key, String value) {
        VersionedValue versionedValue = JSON.parseObject(value, VersionedValue.class);
        Map<String, Object> map = new HashMap<String, Object>();
            try {
                map = objectMapper.readValue(versionedValue.getValue().toString(), Map.class);
            }catch (Exception e){
                map.put("value",value);
            }finally {
                map.put("_id",key);
                map.put("version",versionedValue.getVersion());
                BasicDBObject savemap = null;
                try {
                    savemap = objectMapper.readValue(objectMapper.writeValueAsString(map),BasicDBObject.class);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                mongoDB.save(savemap);
            }
    }


    @Override
    public String get(String key) {
        BasicDBObject queryObject = new BasicDBObject("_id",key);
        DBCursor findIterable = mongoDB.find(queryObject);
        Iterator<DBObject> mongoCursor = findIterable.iterator();


        Version version = null;
        String valueIput = "";
        String result = "" ;
        try {
            Map<String,Object> value =objectMapper.readValue( mongoCursor.next().toString(),Map.class);
            version = objectMapper.readValue(value.get("version").toString(),Version.class);
            value.remove("_id");
            value.remove("version");
            valueIput = objectMapper.writeValueAsString(value);
            VersionedValue versionedValue = new VersionedValue();
            versionedValue.setValue(valueIput);
            versionedValue.setVersion(version);
            result = objectMapper.writeValueAsString(versionedValue);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }

    @Override
    public void update(String key, String value) {
        VersionedValue versionedValue = JSON.parseObject(value, VersionedValue.class);
        Map<String, Object> map = new HashMap<String, Object>();
        try {
            map = objectMapper.readValue(versionedValue.getValue().toString(), Map.class);
        }catch (Exception e){
            map.put("value",value);
        }finally {
            map.put("_id",key);
            map.put("version",versionedValue.getValue());
            DBObject updateCondition=new BasicDBObject();
            DBObject updateValue=new BasicDBObject();
            String input = "";
            Bson inputres = null;
            try {
                input =objectMapper.writeValueAsString(map);
                inputres = objectMapper.readValue(input,BasicDBObject .class);
                updateValue.put("$set",inputres);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mongoDB.update(updateCondition,updateValue);
        }
    }

    @Override
    public String select(String jsonQuery) {
        List<VersionedValue> result = new ArrayList<VersionedValue>();
        String resultStr = "";
        try {
            BasicDBObject queryObject  = objectMapper.readValue(jsonQuery,BasicDBObject .class);
            DBCursor findIterable = mongoDB.find(queryObject);
            Iterator<DBObject> mongoCursor = findIterable.iterator();

            while(mongoCursor.hasNext()){
                Map<String,Object> map = mongoCursor.next().toMap();
                VersionedValue versionedValue = new VersionedValue();
                Version version  = objectMapper.readValue(objectMapper.writeValueAsString(map.get("version")),Version.class);
                map.remove("_id");
                map.remove("version");
                String  valueIput = objectMapper.writeValueAsString(map);
                versionedValue.setValue(valueIput);
                versionedValue.setVersion(version);
                result.add(versionedValue);
            }
            resultStr =  objectMapper.writeValueAsString(result);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return resultStr;
    }

    @Override
    public void remove(String key) {
        BasicDBObject queryObject  = new BasicDBObject("_id",key);
        mongoDB.remove(queryObject);
    }


}
