package com.twitter.redis;

import com.twitter.exception.RedisException;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;

import java.util.List;
import java.util.Map;

/**
 * @author bygzx
 * @date 2019/5/14 14:29
 **/
@Service
public class RedisHashService extends RedisService   {
    private String key;

    public Long hset(final String key, final String field, final String value) throws RedisException {
        return execute(new RedisActionCallback<Long>() {
            @Override
            public Long call(Jedis jedis) {
                return jedis.hset(key, field, value);
            }
        });
    }

    public Long hset(String field, String value) throws RedisException {
        return hset(this.key, field, value);
    }

    public String hget(final String key, final String field) throws RedisException {
        return execute(new RedisActionCallback<String>() {
            @Override
            public String call(Jedis jedis) {
                return jedis.hget(key, field);
            }
        });
    }

    public Map<String, String> hgetAll(final String key) throws RedisException {
        return execute(new RedisActionCallback<Map<String, String>>() {
            @Override
            public Map<String, String> call(Jedis jedis) {
                return jedis.hgetAll(key);
            }
        });
    }

    /**
     * Redis Hmget 命令用于返回哈希表中，一个或多个给定字段的值。
     如果指定的字段不存在于哈希表，那么返回一个 nil 值。
     * @param key
     * @param fields
     * @return 一个包含多个给定字段关联值的表，表值的排列顺序和指定字段的请求顺序一样。
     */
    public List<String> hmget(final String key, final String... fields) {
        return execute(new RedisActionCallback<List<String>>() {
            @Override
            public List<String> call(Jedis jedis) {
                return jedis.hmget(key, fields);
            }
        });
    }

    public Long hincrBy(final String key, final String field, final long value) {
        return execute(new RedisActionCallback<Long>() {
            @Override
            public Long call(Jedis jedis) {
                return jedis.hincrBy(key, field, value);
            }
        });
    }

    public Long hdel(final String key, final String... fields) {
        return execute(new RedisActionCallback<Long>() {
            @Override
            public Long call(Jedis jedis) {
                return jedis.hdel(key, fields);
            }
        });
    }

}
