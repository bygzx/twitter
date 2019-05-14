package com.twitter.demo;

import com.twitter.config.AsyncTask;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.Async;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.ExecutionException;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
@ComponentScan(basePackages={"com.twitter.demo"})
public class DemoApplicationTests {


	@Autowired
	private AsyncTask asyncTask;

	@Test
	public void contextLoads() {
	}

	@Test
	public void AsyncTaskTest() throws InterruptedException, ExecutionException {
		//AsyncTask asyncTask = new AsyncTask();
		for (int i = 0; i < 100; i++) {
			asyncTask.doTask1(i);
		}

		log.info("All tasks finished.");
	}

	/*@Async  //myTaskAsynPool即配置线程池的方法名，此处如果不写自定义线程池的方法名，会使用默认的线程池
	public void doTask1(int i) {
		//logger.info(String.valueOf(i));
		log.info("Task"+i+" started.{}",Thread.currentThread());
	}*/

}
