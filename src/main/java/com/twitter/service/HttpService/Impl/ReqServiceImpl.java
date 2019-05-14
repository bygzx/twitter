package com.twitter.service.HttpService.Impl;

import com.alibaba.fastjson.JSONObject;
import com.twitter.service.HttpService.ReqService;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Service;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * @author eric
 * @date 2019/5/14 15:12
 **/
@Slf4j
@Service
public class ReqServiceImpl implements ReqService {

    private static String loginUrl = "https://twitter.com/login";
    @Override
    public JSONObject login(int isProxy) throws IOException {
        JSONObject jsonObject = new JSONObject();
        getLoginJson(isProxy);
        return null;
    }

    private JSONObject getLoginJson(int isProxy) throws IOException {
        JSONObject jsonObject = new JSONObject();
        HttpGet httpGet = new HttpGet(loginUrl);
        if(isProxy==1){
            HttpHost proxy = new HttpHost("127.0.0.1",1080);
            RequestConfig requestConfig = RequestConfig.custom()
                    .setProxy(proxy)
                    .setConnectTimeout(10000)
                    .setSocketTimeout(10000)
                    .setConnectionRequestTimeout(3000)
                    .build();
            httpGet.setConfig(requestConfig);
        }
        httpGet.addHeader("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/73.0.3683.103 Safari/537.36");
        httpGet.addHeader("authority", "twitter.com");
        httpGet.addHeader("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3");
        httpGet.addHeader("authority", "twitter.com");
        httpGet.addHeader("accept-encoding", "gzip, deflate, br");
        httpGet.addHeader("accept-language", "zh-CN,zh;q=0.9,en;q=0.8");
        HttpResponse response = HttpClients.createDefault().execute(httpGet);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        response.getEntity().writeTo(bos);
        //byte[] data = bos.toByteArray();
        String result = new String(bos.toByteArray(), "utf-8");;
        System.out.println("result--111-->"+result);
        //解析html
        analysisHtml(result);
        return null;
    }
    private void analysisHtml(String html){
        Document document = Jsoup.parse(html);
        Elements contents = document.getElementById("doc").getElementById("page-outer").getElementById("page-container").getElementsByTag("div");
        //Elements form = content.getElementsByTag("form");
        //Elements page_outer = page_outer.getElementsByTag("form");
        if(contents!=null)
        {
            Element page_canvas = contents.select("div.page-canvas").first().select("div.signin-wrapper").first().getElementsByTag("form").first();
            String contents2 = page_canvas.getElementsByTag("fieldset").first().select("input").get(3).attr("value");
            for(Element element:contents){
                Elements elements = element.getElementsByClass("page-canvas");
                if(elements!=null){
                    log.info("asdasdasd");
                }
            }

        }
    }
}
