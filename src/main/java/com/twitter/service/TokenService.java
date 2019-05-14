package com.twitter.service;

import com.twitter.redis.RedisHashService;
import com.twitter.redis.RedisStringService;
import com.twitter.util.constant.AppConsts;
import com.twitter.util.constant.RedisKeys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import javax.xml.bind.DatatypeConverter;
import java.nio.ByteBuffer;
import java.util.Random;

/**
 * @author bygzx
 * @date 2019/5/14 14:17
 **/
@Slf4j
@Service
public class TokenService {

    @Autowired
    private RedisStringService redisStringService;


    @Value("${user.token.expireMins:6000000}")
    private int expireMins;

    private Random random;

    @PostConstruct
    private void init() {
        this.random = new Random();
    }

    /**
     *
     * @param token
     * @return null 如果没匹配token
     */
    public Integer getUid(String token) {
        if (StringUtils.isEmpty(token) || token.length() != 32) {
            //log.error("[getUid] invalid token:{}", token);
            return null;
        }

        byte[] bytes = DatatypeConverter.parseHexBinary(token);
        ByteBuffer buf = ByteBuffer.wrap(bytes);
        int rand1 = buf.getInt();
        int rand2 = buf.getInt();
        int randAppSrc = buf.getInt();
        int randUid = buf.getInt();
        int appSrc = (rand1 | rand2) ^ randAppSrc;
        int uid = (rand1 & rand2) ^ randUid;

        String appSrcStr = AppConsts.AppSource.valueOf(appSrc).name();
        String tokenKey = RedisKeys.USER_TOKEN_KEY.getKey(appSrcStr, uid);
        String cachedToken = redisStringService.get(tokenKey);

        if (!token.equals(cachedToken)) {
            log.error("[getUid] token not match,but still work, uid:{}, userToken:{}, localToken:{}",
                    uid, token, cachedToken);
            //注释掉return null ，防止某些用户网络不好多次请求导致前端没换对token使用不了
            //return null;
        }

        // 用户每次使用App，都会延长token时间，默认12小时
        redisStringService.expire(tokenKey, expireMins);

        return uid;
    }

    public Integer getUidByOldToken(String token) {
        if (StringUtils.isEmpty(token) || token.length() != 32) {
            //log.error("[getUid] invalid token:{}", token);
            return null;
        }

        byte[] bytes = DatatypeConverter.parseHexBinary(token);
        ByteBuffer buf = ByteBuffer.wrap(bytes);
        int rand1 = buf.getInt();
        int rand2 = buf.getInt();
        int randAppSrc = buf.getInt();
        int randUid = buf.getInt();
        int appSrc = (rand1 | rand2) ^ randAppSrc;
        int uid = (rand1 & rand2) ^ randUid;
        return uid;
    }

    public String createToken(AppConsts.AppSource appSrc, Integer uid) {
        int rand1 = this.random.nextInt(Integer.MAX_VALUE);
        int rand2 = this.random.nextInt(Integer.MAX_VALUE);

        ByteBuffer buf = ByteBuffer.allocate(16);
        buf.putInt(rand1);
        buf.putInt(rand2);
        buf.putInt((rand1 | rand2) ^ appSrc.getVal());
        buf.putInt((rand1 & rand2) ^ uid);

        String token = DatatypeConverter.printHexBinary(buf.array());
        String tokenKey = RedisKeys.USER_TOKEN_KEY.getKey(appSrc.name(), uid);
        redisStringService.set(tokenKey, token, expireMins);
        return token;
    }


    public long removeToken(AppConsts.AppSource appSrc, Integer uid) {
        String tokenKey = RedisKeys.USER_TOKEN_KEY.getKey(appSrc.name(), uid);
        return redisStringService.del(tokenKey);
    }




}
