package com.saic.easydrive.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Created by 张海逢 on 2018/5/3.
 */

public class GetRequestUtil {
    public static void main(String args[]){
        Map<String,String> querys = new HashMap<>();
        querys.put("city","上海");
        String result = doGet("http://chkj02.market.alicloudapi.com/qgtq","62a3baabd33d4e45bd76a82ae6b99d1b",querys);
      /*  querys.put("prov","上海");
        String result = doGet("http://ali-todayoil.showapi.com/todayoil","62a3baabd33d4e45bd76a82ae6b99d1b",querys);*/
        System.out.println(result);
    }

    public static String doGet(String address, String AppCode, Map<String,String> querys){
        HttpURLConnection connection = null;
        BufferedReader reader = null;
        try {
            Set<Map.Entry<String,String>> set = querys.entrySet();
            Iterator<Map.Entry<String,String>> iterator = set.iterator();
            int index = 0;
            while (iterator.hasNext()){
                Map.Entry mapEntry = (Map.Entry)iterator.next();
                if(index==0){
                    index++;
                    address = address + "?" + mapEntry.getKey() + "=" + mapEntry.getValue();
                }else{
                    address = address + "&" + mapEntry.getKey() + "=" + mapEntry.getValue();
                }
            }
            URL url = new URL(address);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(8000);
            connection.setReadTimeout(8000);
            //用setRequestProperty方法设置一个自定义的请求头:action，由于后端判断
            connection.setRequestProperty("Authorization", "APPCODE "+AppCode);
            connection.connect();
            //当调用getInputStream方法时才真正将请求体数据上传至服务器
            InputStream in = connection.getInputStream();
            //下面对获取到的输入流进行读取
            reader = new BufferedReader(new InputStreamReader(in));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null){
                response.append(line);
            }
            return response.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "获取失败";
        }finally {
            if (reader != null){
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (connection != null){
                connection.disconnect();
            }
        }
    }
}
