package com.twitter.config;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.*;

import javax.annotation.PostConstruct;

@ImportResource({"classpath:/applicationContext.xml"})
@ComponentScan(basePackages={"com.twitter.controller"})
@EnableConfigurationProperties
@EnableAutoConfiguration
public class EntryConfig {
	final static Logger log = LoggerFactory.getLogger(EntryConfig.class);
	@Autowired
	private TaskThreadPool config;

	@Value("${executor.thread.num:3}")
	private int executorThreadNum;
	
	@PostConstruct
	private void init() {
		log.info("[init]   executorThreadNum:{}",  executorThreadNum);
	}
	/**
	 * 1.先定义一个convert转换消息的对象
	 * 2.添加fastjson的配置信息，比如：是否要格式化返回的json数据
	 * 3.在convert中添加配置信息
	 * 4.将convert添加到converters当中
	 */
	/*@Bean
	public HttpMessageConverters fastJsonHttpMessageConverters() {
		log.info("****************configureMessageConverters1****************{}");

		//1.先定义一个convert转换消息的对象
		FastJsonHttpMessageConverter fastConverter = new FastJsonHttpMessageConverter();
		//2.添加fastjson的配置信息，比如：是否要格式化返回的json数据
		FastJsonConfig fastJsonConfig = new FastJsonConfig();
		fastJsonConfig.setSerializerFeatures(SerializerFeature.PrettyFormat
				,SerializerFeature.WriteNonStringKeyAsString //将不是String类型的key转换成String类型，否则前台无法将Json字符串转换成Json对象
				,SerializerFeature.WriteNullBooleanAsFalse //Boolean字段如果为null,输出为false,而非null
				,SerializerFeature.WriteDateUseDateFormat  //全局修改日期格式,默认为false。JSON.DEFFAULT_DATE_FORMAT = "yyyy-MM-dd";JSON.toJSONString(obj, SerializerFeature.WriteDateUseDateFormat);
				);

		//处理中文乱码问题(不然出现中文乱码)
		List<MediaType> fastMediaTypes = new ArrayList<MediaType>();
		fastMediaTypes.add(MediaType.APPLICATION_JSON_UTF8);
		fastConverter.setSupportedMediaTypes(fastMediaTypes);

		//3.在convert中添加配置信息
		fastConverter.setFastJsonConfig(fastJsonConfig);

		return new HttpMessageConverters(fastConverter);
	}

	@Bean
	public MappingJackson2HttpMessageConverter getMappingJackson2HttpMessageConverter() {
		MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter = new MappingJackson2HttpMessageConverter();
		//设置日期格式
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.setDateFormat(CustomDateFormat.instance);
		objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
		mappingJackson2HttpMessageConverter.setObjectMapper(objectMapper);
		//设置中文编码格式
		List<MediaType> list = new ArrayList<MediaType>();
		list.add(MediaType.APPLICATION_JSON_UTF8);
		mappingJackson2HttpMessageConverter.setSupportedMediaTypes(list);
		return mappingJackson2HttpMessageConverter;
	}
*/
}
