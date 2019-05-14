package com.twitter.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

/**
 * @author binfeng huang
 * @date 2019/5/13 16:51
 **/
//@Component
    @Service
public class AsyncTask {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Async  //myTaskAsynPool即配置线程池的方法名，此处如果不写自定义线程池的方法名，会使用默认的线程池
    public void doTask1(int i) {
        //logger.info(String.valueOf(i));
        logger.info("Task"+i+" started.{}",Thread.currentThread());
    }
}
