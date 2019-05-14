package com.twitter.service.HttpService;

import com.alibaba.fastjson.JSONObject;

import java.io.IOException;

/**
 * @author eric
 * @date 2019/5/14 15:11
 **/
public interface ReqService {
    /**
     *
     * @param isProxy 0-不用代理 1-用
     * @return
     */
    JSONObject login(int isProxy) throws IOException;
}
