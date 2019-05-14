package com.twitter.controller;

import com.alibaba.fastjson.JSONObject;
import com.twitter.config.AsyncTask;
import com.twitter.util.controller.AbstractController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


/**
 * @author bygzx
 * @date 2019/5/13 10:31
 **/
@Slf4j
@RestController
@RequestMapping("/twitter")
public class TwitterController extends AbstractController {

    @Autowired
    private AsyncTask asyncTask;

    private static ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(5,10, 30, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(50));

    @PostMapping("/hello")
    public Object hello() {
        log.info("Hello!!!");
        JSONObject jsonObject = new JSONObject();
        Date date = new Date();
        jsonObject.put("date",date);
        jsonObject.put("as","我是嘿嘿嘿");
        jsonObject.put("bs1",null);
        jsonObject.put("bs2","");
        jsonObject.put("success1",false);
        jsonObject.put("success2",true);
        ss();
        return buildSuccess(jsonObject);
    }

    @PostMapping("/syncData")
    public Object sync(){
        threadPoolExecutor.submit(() ->
        {
            log.info("threadPoolExecutor");
            log.info(Thread.currentThread().getName());

        });
        return buildSuccess();
    }
    //@Async
    public void ss(){
        asyncTask.doTask1(1);
    }

}
