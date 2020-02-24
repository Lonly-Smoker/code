package com.xu1900.code.util;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import javax.swing.text.html.parser.Entity;
import java.io.IOException;

public class CheckShareLinkEnableUtil {
    static CloseableHttpClient httpClient= HttpClients.createDefault();
    /**
     * 检查链接是否有效
     */
    public static boolean check(String link) throws IOException {
        HttpGet httpGet=new HttpGet(link);
        //设置请求头消息
        httpGet.setHeader("User-Agent","Mozilla/5.0 (Winodws NT 6.1; Win64; x64 rv:50.0) Gecko/20100101 Firefox/50.0");
        CloseableHttpResponse response = httpClient.execute(httpGet);
        HttpEntity entity=response.getEntity();     //获取返回实体
        String result= EntityUtils.toString(entity,"UTF-8");
        if(result.contains("请输入提取码")||result.contains("永久有效")){
            return true;
        }else
        {
            return false;
        }
    }


}
