package com.twitter.controller;

import com.alibaba.fastjson.JSONObject;
import com.twitter.util.controller.AbstractController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;


/**
 * @author binfeng huang
 * @date 2019/5/13 10:31
 **/
@Slf4j
@RestController
@RequestMapping("/twitter")
public class TwitterController extends AbstractController {
    @PostMapping("/hello")
    public Object hello() {
        log.info("Hello!!!");
        JSONObject jsonObject = new JSONObject();
        Date date = new Date();
        jsonObject.put("date",date);
        jsonObject.put("as","asd");
        jsonObject.put("bs1",null);
        jsonObject.put("bs2","");
        jsonObject.put("success1",false);
        jsonObject.put("success2",true);
        return buildSuccess(jsonObject);
    }
}
