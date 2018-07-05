package cn.mrray.blockchain.core.block.db;

/**
 * key-value型DB数据库操作接口
 * @author wuweifeng wrote on 2018/3/26.
 */
public interface MongoDBStore {
    /**
     * 数据库key value
     *
     * @param key
     *         key
     * @param value
     *         value
     */
    void put(String key, String value);

    /**
     * get By Key
     *
     * @param key
     *         key
     * @return value
     */
    String get(String key);

    /**
     * 数据库key value
     *
     * @param key
     *         key
     * @param value
     *         value
     */
    void update(String key, String value);

    /**
     * 数据库key value
     *
     * @param key
     *         key
     */
    String select(String key);

    /**
     * remove by key
     *
     * @param key key
     */
    void remove(String key);


}
