package com.twitter.redis;

import com.twitter.exception.RedisException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.exceptions.JedisConnectionException;

import java.util.Set;

/**
 * @author bygzx
 * @date 2019/5/14 14:21
 **/
@Service
public class RedisService {

    protected Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired(required = false)
    protected JedisPool jedisPool;

    @Value("${redis.retryCount:1}")
    protected int retryCount;

    public JedisPool getPool() {
        return jedisPool;
    }

    public void setPool(JedisPool jedisPool) {
        this.jedisPool = jedisPool;
    }

    public boolean exists(final String key) {
        return execute(new RedisActionCallback<Boolean>() {
            @Override
            public Boolean call(Jedis jedis) {
                return jedis.exists(key);
            }
        });
    }

    /**
     * 删除key
     * @param keys 要删除的keys
     * @return 返回删除的个数
     * @throws RedisException
     */
    public long del(final String...keys) throws RedisException {
        return execute(new RedisActionCallback<Long>() {
            @Override
            public Long call(Jedis jedis) {
                return jedis.del(keys);
            }
        });
    }

    /**
     * 为某个key设置过期时间
     * @param key
     * @param seconds 过期时间，单位秒
     * @return 设置成功返回true ，失败返回false
     * @throws RedisException
     */
    public boolean expire(final String key, final int seconds) throws RedisException {
        return execute(new RedisActionCallback<Boolean>() {
            @Override
            public Boolean call(Jedis jedis) {
                long result = jedis.expire(key , seconds);
                return result == 1;
            }
        });
    }

    /**
     * 根据pattern获取keys
     * @param pattern
     * @return
     */
    public Set<String> keys(final String pattern) {
        return execute(new RedisActionCallback<Set<String>>() {
            @Override
            public Set<String> call(Jedis jedis) {
                return jedis.keys(pattern);
            }
        });
    }

    /**
     * 返回 key 所储存的值的类型
     * @param key
     * @return
     */
    public String type(final String key) {
        return execute(new RedisActionCallback<String>() {
            @Override
            public String call(Jedis jedis) {
                return jedis.type(key);
            }
        });
    }

    /**
     * 将Jedis实例返还JedisPool
     * @param jedis
     */
    public void returnResource(Jedis jedis) {
        try {
            if (jedis != null) {
                jedis.close();
            }
        } catch (Exception e) {
            logger.error("return jedis to jedisPool exception", e);
        }
    }

    public <T> T execute(RedisActionCallback<T> callback) {
        int executeCount = 0;
        int maxExecuteCount = this.retryCount + 1;
        Exception exception = null;

        while (executeCount < maxExecuteCount) {
            Jedis jedis = null;
            try {
                jedis = jedisPool.getResource();
                return callback.call(jedis);
            } catch (JedisConnectionException jce) {
                logger.error(jce.getMessage(), jce);
                // 出现连接异常时，不抛异常，视情况进行重试
                exception = jce;
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                // 出现其它异常时，直接抛出异常
                throw new RedisException(e);
            } finally {
                returnResource(jedis);
                executeCount++;
            }

            if (executeCount < maxExecuteCount) {
                logger.warn("Retry Redis operation");
            }
        }

        throw new RedisException(exception);
    }

}
