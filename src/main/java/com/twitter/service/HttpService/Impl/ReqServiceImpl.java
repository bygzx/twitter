package com.twitter.service.HttpService.Impl;

import com.alibaba.fastjson.JSONObject;
import com.gargoylesoftware.htmlunit.*;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.util.Cookie;
import com.twitter.service.HttpService.ReqService;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.*;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Service;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.net.URL;
import java.util.Set;
import java.util.zip.GZIPInputStream;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

/**
 * @author eric
 * @date 2019/5/14 15:12
 **/
@Slf4j
@Service
public class ReqServiceImpl implements ReqService {

    private static String loginUrl = "https://twitter.com/login";
    @Override
    public JSONObject login(int isProxy) throws Exception {
        JSONObject jsonObject = new JSONObject();
        //getLoginJson(isProxy);
        crawlPageWithoutAnalyseJs(isProxy);
        return null;
    }

    private JSONObject getLoginJson(int isProxy) throws IOException, ScriptException, NoSuchMethodException {
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

        httpGet.addHeader("authority", "twitter.com");
        httpGet.addHeader("method", "GET");
        httpGet.addHeader("path", "/login");
        httpGet.addHeader("scheme", "https");
        httpGet.addHeader("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3");
        httpGet.addHeader("accept-encoding", "gzip, deflate, br");
        httpGet.addHeader("accept-language", "zh-CN,zh;q=0.9,en;q=0.8");
        httpGet.addHeader("cache-control", "max-age=0");
        httpGet.addHeader("upgrade-insecure-requests", "1");
        httpGet.addHeader("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/73.0.3683.103 Safari/537.36");
        HttpResponse response = HttpClients.createDefault().execute(httpGet);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        response.getEntity().writeTo(bos);
        //byte[] data = bos.toByteArray();
        String result = new String(bos.toByteArray(), "utf-8");;
        //System.out.println("result--111-->"+result);
        //解析html
        analysisHtml(result,isProxy,response);
        return null;
    }
    private void analysisHtml(String html,int isProxy,HttpResponse response) throws IOException, ScriptException, NoSuchMethodException {
        Document document = Jsoup.parse(html);
        Elements contents = document.getElementById("doc").getElementById("page-outer").getElementById("page-container").getElementsByTag("div");
        //1、先拿login返回的token
        if(contents!=null)
        {
            Element page_canvas = contents.select("div.page-canvas").first().select("div.signin-wrapper").first().getElementsByTag("form").first();
            Elements inputs = page_canvas.getElementsByTag("fieldset").first().select("input");
            String authenticity_token = "";
            for(Element e:inputs){
                if("authenticity_token".equals(e.attr("name"))){
                    authenticity_token = e.attr("value");
                    break;
                }
            }
            String cookie = "";
            Header[] headers = response.getHeaders("Set-Cookie");
            for (Header header : headers) {
                if (header != null &&header.getName().toLowerCase().equals("set-cookie")) {
                    cookie += header.getValue();
							HeaderElement[] headerElements = header.getElements();
							for (HeaderElement headerElement : headerElements) {
								//if (headerElement != null &&headerElement.getName().equals("verify")) {
										cookie += headerElement.getName() + "=" + headerElement.getValue() + ";";
								//}
							}
                }
            }

            //webClient.closeAllWindows();
            //2、再通过login返回的cookie拿ui_metrics，这里需要java的js引擎获取
            String jsUrl = "https://twitter.com/i/js_inst?c_name=ui_metrics";
            HttpGet httpGet = new HttpGet(jsUrl);
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

            httpGet.addHeader("authority", "twitter.com");
            httpGet.addHeader("method", "GET");
            httpGet.addHeader("path", "/login");
            httpGet.addHeader("scheme", "https");

            httpGet.addHeader("accept-encoding", "gzip, deflate, br");
            httpGet.addHeader("accept-language", "zh-CN,zh;q=0.9,en;q=0.8");
            httpGet.addHeader("cache-control", "max-age=0");
            httpGet.addHeader("upgrade-insecure-requests", "1");
            httpGet.addHeader("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/73.0.3683.103 Safari/537.36");
            httpGet.addHeader("cookie",cookie);
            HttpResponse response1 = HttpClients.createDefault().execute(httpGet);
            String jsTemp = "";
            //读取服务器返回过来的json字符串数据
            jsTemp = EntityUtils.toString(response1.getEntity());
            String js = "";
            String param = "";
            String replaceStr = "\r\n";
            String firstLine = "";
            if(!"".equals(jsTemp)){
                //log.info("jsTemp---->{}",jsTemp);
                String[] jsTempList = jsTemp.split("\r\n");
                if(jsTempList.length==1){
                    jsTempList = jsTemp.split("\n");
                    replaceStr = "\n";
                }
                firstLine = jsTempList[2];
                firstLine = firstLine.substring(firstLine.indexOf(")")+1,firstLine.length()-1);
                //log.info("s.length---->{}",s.length);
                for(int i=0;i<jsTempList.length;i++){
                    if(i==0){
                        continue;
                    }
                    if(i==1){
                        //log.info("sss---->{}",jsTempList[i].substring(jsTempList[i].indexOf("=")+1,jsTempList[i].length()));
                        //js = "var twitter = "+jsTempList[i].substring(jsTempList[i].indexOf("=")+1,jsTempList[i].length());
                        js = "function twitter() {"+replaceStr;
                    }
                    else if(jsTempList[i].indexOf("var")>=0 && i!=1 && jsTempList[i].length()<50 && jsTempList[i].indexOf("var inputs")<0){
                        param = jsTempList[i].replaceAll("var ","").trim().replaceAll(";","");
                        log.info("param---->{}",param);
                            js = js+replaceStr+jsTempList[i];
                    }else if(jsTempList[i].indexOf("inputs = ")>=0){
                        js = js + replaceStr + "return " + param + "};";
                        break;
                    }else{
                        js = js+replaceStr+jsTempList[i];
                    }
                }
                //log.info("js----->{}",js);
                ScriptEngine engine = new ScriptEngineManager()
                        .getEngineByName("nashorn");
                engine.eval(js);
                Invocable invocable = (Invocable) engine;
                //String a = String.valueOf(invocable.invokeFunction("twitter"));
                log.info("firstLine---->{}",firstLine);



            }
            //提交表单

        }
    }


    /**
     * 功能描述：抓取页面时不解析页面的js
     * @param isProxy
     * @throws Exception
     */
    public void crawlPageWithoutAnalyseJs(int isProxy) throws Exception{
        URL link=new URL(loginUrl);
        WebRequest request=new WebRequest(link);
        //request.setCharset("UTF-8");
        request.setAdditionalHeader("authority", "twitter.com");
        request.setAdditionalHeader("authority", "twitter.com");
        request.setAdditionalHeader("method", "GET");
        request.setAdditionalHeader("path", "/login");
        request.setAdditionalHeader("scheme", "https");
        request.setAdditionalHeader("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3");
        request.setAdditionalHeader("accept-encoding", "gzip, deflate, br");
        request.setAdditionalHeader("accept-language", "zh-CN,zh;q=0.9,en;q=0.8");
        request.setAdditionalHeader("cache-control", "max-age=0");
        request.setAdditionalHeader("upgrade-insecure-requests", "1");
        request.setAdditionalHeader("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/73.0.3683.103 Safari/537.36");
        ////设置请求报文头里的User-Agent字段
        //1.创建连接client
        WebClient webClient = new WebClient(BrowserVersion.CHROME);
        //2.设置连接的相关选项
        webClient.getOptions().setCssEnabled(false);
        webClient.getOptions().setJavaScriptEnabled(false);
        webClient.getOptions().setTimeout(10000);
        ProxyConfig proxyConfig = webClient.getOptions().getProxyConfig();
        proxyConfig.setProxyHost("127.0.0.1");
        proxyConfig.setProxyPort(1080);
        webClient.getCookieManager().setCookiesEnabled(true);//开启cookie管理
        webClient.getOptions().setJavaScriptEnabled(true);//开启js解析。对于变态网页，这个是必须的
        //webClient.getOptions().setCssEnabled(true);//开启css解析。对于变态网页，这个是必须的。

        //3.抓取页面
        HtmlPage page = webClient.getPage(request);
        webClient.waitForBackgroundJavaScript(10000);

        //4.关闭模拟窗口
        webClient.close();
        Document document = Jsoup.parse(page.asXml());
        Elements contents = document.getElementById("doc").getElementById("page-outer").getElementById("page-container").getElementsByTag("div");
        //1、先拿login返回的token
        HtmlElement element = page.getHead();
        CookieManager CM = webClient.getCookieManager(); //WC = Your WebClient's name
        Set<Cookie> cookies_ret = CM.getCookies();//返回的Cookie在这里，下次请求的时候可能可以用上啦。

        String authenticity_token = page.getElementsByName("authenticity_token").get(0).getAttribute("value");
        String ui_metrics = page.getElementsByName("ui_metrics").get(0).getAttribute("value");
        log.info("header----->{}","sadasdas");
        /*if(contents!=null) {
            Element page_canvas = contents.select("div.page-canvas").first().select("div.signin-wrapper").first().getElementsByTag("form").first();
            Elements inputs = page_canvas.getElementsByTag("fieldset").first().select("input");
            String authenticity_token = "";
            for (Element e : inputs) {
                if ("authenticity_token".equals(e.attr("name"))) {
                    authenticity_token = e.attr("value");
                    break;
                }
            }
            String cookie = "";
            Header[] headers = response.getHeaders("Set-Cookie");
            for (Header header : headers) {
                if (header != null && header.getName().toLowerCase().equals("set-cookie")) {
                    cookie += header.getValue();
                    HeaderElement[] headerElements = header.getElements();
                    for (HeaderElement headerElement : headerElements) {
                        //if (headerElement != null &&headerElement.getName().equals("verify")) {
                        cookie += headerElement.getName() + "=" + headerElement.getValue() + ";";
                        //}
                    }
                }
            }
        }*/
    }

    /**
     * 功能描述：抓取页面时并解析页面的js
     * @param url
     * @throws Exception
     */
    public void crawlPageWithAnalyseJs(String url) throws Exception{
        //1.创建连接client
        WebClient webClient = new WebClient(BrowserVersion.CHROME);
        //2.设置连接的相关选项
        webClient.getOptions().setCssEnabled(false);
        webClient.getOptions().setJavaScriptEnabled(true);  //需要解析js
        webClient.getOptions().setThrowExceptionOnScriptError(false);  //解析js出错时不抛异常
        webClient.getOptions().setTimeout(10000);  //超时时间  ms
        //3.抓取页面
        HtmlPage page = webClient.getPage(url);
        //4.将页面转成指定格式
        webClient.waitForBackgroundJavaScript(10000);   //等侍js脚本执行完成
        System.out.println(page.asXml());
        //5.关闭模拟的窗口
        webClient.close();
    }


}
