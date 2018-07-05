package cn.mrray.blockchain.core.block.db;

import cn.mrray.blockchain.core.core.model.Version;
import cn.mrray.blockchain.core.core.model.VersionedValue;
import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.lightcouch.CouchDbClient;

import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * rocksDB对于存储接口的实现
 * @author wuweifeng wrote on 2018/3/13.
 */
@Component
public class CouchDBStoreImpl implements CouchDBStore {
    @Resource
    private CouchDbClient couchDB;
    ObjectMapper objectMapper = new ObjectMapper();
    private Gson gson = new Gson();
    private org.slf4j.Logger logger = LoggerFactory.getLogger(getClass());
    @Override
    public void put(String key, String value) {
        long startTime = 0;
        long endTime = 0;
        float seconds = 0;
        startTime =System.currentTimeMillis();
        VersionedValue versionedValue = JSON.parseObject(value, VersionedValue.class);
        endTime = System.currentTimeMillis();
        seconds = (endTime - startTime) / 1000F;
        logger.info("類型轉換時間為："+ seconds);
        Map<String, Object> map = new HashMap<String, Object>();
            try {
                map = gson.fromJson(versionedValue.getValue(), map.getClass());
            }catch (Exception e){
                map.put("value",value);
            }finally {
                map.put("_id",key);
                map.put("version",versionedValue.getVersion());
                startTime =System.currentTimeMillis();
                couchDB.save(map);
                endTime =System.currentTimeMillis();
                seconds = (endTime - startTime) / 1000F;
                logger.info("數據存儲時間為："+ seconds);
            }
    }


    @Override
    public String get(String key) {
        Map<String, Object> map = new HashMap<String, Object>();
        Map<String, Object> value = couchDB.find(Map.class,key);
       // map = gson.fromJson(value, map.getClass());
        Version version = null;
        String valueIput = "";
        String result = "" ;
        try {
            version = objectMapper.readValue(objectMapper.writeValueAsString(value.get("version")),Version.class);
            value.remove("_id");
            value.remove("_rev");
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
        Map<String, Object> mapb = new HashMap<String, Object>();
        String valueinfo = couchDB.find(String.class ,key);
        mapb = gson.fromJson(valueinfo, map.getClass());
        String rev = mapb.get("_rev").toString();
        try {
            map = gson.fromJson(versionedValue.getValue(), map.getClass());
        }catch (Exception e){
            map.put("value",value);
        }finally {
            map.put("_id",key);
            map.put("_rev",rev);
            map.put("version",versionedValue.getValue());
            couchDB.update(map);
        }
    }

    @Override
    public String select(String jsonQuery) {
        JavaType type = objectMapper.getTypeFactory().constructParametricType(ArrayList.class, Map.class);
        List<VersionedValue> result = new ArrayList<VersionedValue>();
        String resultStr = "";
        try {
            List<Map<String,Object>> mapList = (List<Map<String,Object>>)objectMapper.readValue(couchDB.findDocs(jsonQuery ,JsonObject.class).toString(), type);
            for(Map<String,Object> map :mapList){
                VersionedValue versionedValue = new VersionedValue();
                Version version  = objectMapper.readValue(objectMapper.writeValueAsString(map.get("version")),Version.class);
                map.remove("_id");
                map.remove("_rev");
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


}
