package com.twitter.redis;

import com.alibaba.fastjson.JSONObject;
import com.twitter.exception.RedisException;
import com.twitter.util.serialze.SerializationUtil;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;
import redis.clients.jedis.exceptions.JedisException;

import java.util.List;
import java.util.Set;

/**
 * @author bygzx
 * @date 2019/5/14 14:19
 **/
@Service
public class RedisStringService extends RedisService {

    private final String STATUS_CODE_OK = "OK";

    private String key;

    public String get() throws RedisException {
        return this.get(key);
    }

    public String get(final String key) throws RedisException {
        return execute(new RedisActionCallback<String>() {
            @Override
            public String call(Jedis jedis) {
                return jedis.get(key);
            }
        });
    }

    public boolean set(String value) throws RedisException {
        return this.set(key, value);
    }

    public boolean set(final String key, final String value) throws RedisException {
        return execute(new RedisActionCallback<Boolean>() {
            @Override
            public Boolean call(Jedis jedis) {
                String setResult = jedis.set(key, value);
                return STATUS_CODE_OK.equalsIgnoreCase(setResult);
            }
        });
    }

    public boolean set(String value, int second) throws RedisException {
        return this.set(key, value, second);
    }

    public boolean set(final String key, final String value, final int second) throws RedisException {
        return execute(new RedisActionCallback<Boolean>() {
            @Override
            public Boolean call(Jedis jedis) {
                Transaction transaction = jedis.multi();
                transaction.set(key, value);
                transaction.expire(key, second);
                List<Object> result = transaction.exec();

                if (result != null && result.size() == 2) {
                    Object setResult = result.get(0);
                    Object expireResulr = result.get(1);
                    if (setResult != null && expireResulr != null &&
                            STATUS_CODE_OK.equalsIgnoreCase(setResult.toString())
                            && "1".equalsIgnoreCase(expireResulr.toString())) {
                        return true;
                    }
                }
                return false;
            }
        });
    }

    public boolean setex(final String key, final int seconds, final String value) throws RedisException {
        return execute(new RedisActionCallback<Boolean>() {
            @Override
            public Boolean call(Jedis jedis) {
                String setexResult = jedis.setex(key, seconds, value);
                return STATUS_CODE_OK.equalsIgnoreCase(setexResult);
            }
        });
    }

    /***
     * 获取redis分布式锁
     * @param lockTime 锁持续时间（毫秒）
     * @param timeOut 超时时间（毫秒）
     * @return true：获得锁，false：未获得锁
     * @throws RedisException
     */
    public boolean getLock(int lockTime, long timeOut) throws RedisException {
        return getLock(key, lockTime, timeOut);
    }


    /***
     * 获取redis分布式锁
     * @param lockName 锁名称
     * @param lockTime 锁持续时间（毫秒）
     * @param timeOut 超时时间（毫秒）
     * @return true：获得锁，false：未获得锁
     * @throws RedisException
     */
    public boolean getLock(String lockName, int lockTime, long timeOut) throws RedisException {
        Jedis jedis = null;
        long beginTime = System.currentTimeMillis();
        try {
            jedis = jedisPool.getResource();
            while (System.currentTimeMillis() - beginTime < timeOut) {
                try {
                    String setResult = jedis.set(lockName, "lock ok", "NX", "PX", lockTime);
                    if (STATUS_CODE_OK.equalsIgnoreCase(setResult)) {
                        return true;
                    }
                    Thread.sleep(30);
                } catch (InterruptedException e) {
                    Thread.interrupted();
                }
            }
        } catch (Exception e) {
            throw new RedisException("RedisLock getLock lockName=" + lockName + " Exception", e);
        } finally {
            returnResource(jedis);
        }
        return false;
    }

    /***
     * 获取redis分布式锁
     * @param lockName 锁名称
     * @return true：获得锁，false：释放锁失败
     * @throws RedisException
     */
    public boolean releaseLock(String lockName) throws RedisException {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            jedis.del(lockName);
            return true;
        } catch (Exception e) {
            throw new RedisException("RedisLock releaseLock lockName=" + lockName + " Exception", e);
        } finally {
            returnResource(jedis);
        }
    }

    /***
     * 释放redis分布式锁
     * @return true：释放锁成功，false：释放锁失败
     * @throws RedisException
     */
    public boolean releaseLock() throws RedisException {
        return releaseLock(key);
    }

    public <T> T getValue(String key, Class<T> tClass) throws RedisException {
        String value = get(key);
        if (value != null) {
            return JSONObject.parseObject(value, tClass);
        }
        return null;
    }

    /**
     * 获取缓存
     *
     * @param key
     * @param key
     * @param db  databaseIndex
     */
    public <T> T getObject(String key, int db, Class<T> cls) {
        byte[] value = null;
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            jedis.select(db);
            value = jedis.get(key.getBytes());
        } catch (Exception e) {
            logger.error("getObject出错：key[{}],db[{}]", key, db, e);
        } finally {
            returnResource(jedis);
        }
        if (value != null && value.length > 0) {
            return SerializationUtil.deserialize(value, cls);
        }
        return null;
    }

    /**
     * 设置缓存
     * @param key
     * @param obj
     * @param seconds
     * @param db
     */
    public void set(String key, Object obj, int seconds, int db) {
        if (obj == null) {
            logger.info("key: {}对应的value为空", key);
            return;
        }
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            jedis.select(db);
            jedis.setex(key.getBytes(), seconds, SerializationUtil.serialize(obj));
        } catch (Exception e) {
            logger.error("设置redis缓存出错key：{},{}", key, e.getMessage());
        } finally {
            returnResource(jedis);
        }
    }

    /**
     * 根据key删除缓存
     *
     * @param key
     * @param db
     * @param db  databaseIndex
     */
    public void delByKey(String key, int db) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            jedis.select(db);
            jedis.del(key);
        } catch (JedisException e) {
            logger.error(e.getMessage());
            if (jedis != null) {
                returnResource(jedis);
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
        } finally {
            returnResource(jedis);
        }

    }

    /**
     * 根据前缀删除缓存
     *
     * @param likeKey
     * @param db
     * @param db      databaseIndex
     */
    public void delByPrefix(String likeKey, int db) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            jedis.select(db);
            Set<String> keys = jedis.keys(likeKey + "*");
            if (keys.size() > 0) {
                String[] delKeys = new String[keys.size()];
                int i = 0;
                for (String delKey : keys) {
                    delKeys[i] = delKey;
                    i++;
                }
                jedis.del(delKeys);
            }
        } catch (JedisException e) {
            logger.error(e.getMessage());
        } catch (Exception e) {
            logger.error(e.getMessage());
        } finally {
            returnResource(jedis);
        }
    }

}