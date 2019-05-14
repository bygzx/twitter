package com.twitter.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;


@SpringBootApplication
@EnableAsync
@ComponentScan(basePackages={"com.twitter.controller"
		,"com.twitter.config"
		,"com.twitter.service"
		,"com.twitter.redis"})
public class DemoApplication  {

	/*@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(EntryConfig.class,TaskExecutePool.class);
	}

	public static void main(String[] args) throws Exception {
		SpringApplication.run(EntryConfig.class, args);
	}*/
	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}
}
