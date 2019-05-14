package com.twitter.config;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;


import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.support.config.FastJsonConfig;
import com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter;

/**
 * @author binfeng huang
 * @date 2019/5/13 13:35
 **/
@Slf4j
@SpringBootConfiguration
public class MyMessageConverter implements WebMvcConfigurer {



    /**
     * 替换使用 FastJson 解析返回结果
     * 1.先定义一个convert转换消息的对象
     * 2.添加fastjson的配置信息，比如：是否要格式化返回的json数据
     * 3.在convert中添加配置信息
     * 4.将convert添加到converters当中
     */
    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        log.info("****************configureMessageConverters1****************{}",converters.toString());

        //1.先定义一个convert转换消息的对象
        FastJsonHttpMessageConverter fastConverter = new FastJsonHttpMessageConverter();
        //2.添加fastjson的配置信息，比如：是否要格式化返回的json数据
        FastJsonConfig fastJsonConfig = new FastJsonConfig();
        fastJsonConfig.setSerializerFeatures(SerializerFeature.PrettyFormat);

        //处理中文乱码问题(不然出现中文乱码)
        List<MediaType> fastMediaTypes = new ArrayList<MediaType>();
        fastMediaTypes.add(MediaType.APPLICATION_JSON_UTF8);
        fastConverter.setSupportedMediaTypes(fastMediaTypes);

        //3.在convert中添加配置信息
        fastConverter.setFastJsonConfig(fastJsonConfig);
        //4.将convert添加到converters当中
        converters.add(fastConverter);
    }

    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        FastJsonHttpMessageConverter fjc = new FastJsonHttpMessageConverter();
        FastJsonConfig fj = new FastJsonConfig();
        fj.setSerializerFeatures(SerializerFeature.DisableCircularReferenceDetect);
        fjc.setFastJsonConfig(fj);
        converters.add(fjc);
        FastJsonConfig fj1 = new FastJsonConfig();
        fj1.setSerializerFeatures(SerializerFeature.DisableCircularReferenceDetect);
        fjc.setFastJsonConfig(fj);

        /*for (HttpMessageConverter<?> messageConverter : converters) {
            log.info("****************extendMessageConverters2****************{}",messageConverter); //2
        }*/
    }
}
