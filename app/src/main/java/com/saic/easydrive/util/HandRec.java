package com.saic.easydrive.util;

/**
 * Created by 张海逢 on 2018/4/30.
 */


import org.apache.commons.codec.binary.Base64;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

import javax.net.ssl.SSLException;

public class HandRec {

    public static void main(String[] args) throws Exception{

        File file = new File("C:/skydianshi/1.jpg");
        byte[] buff = getBytesFromFile(file);
        String url = "https://api-cn.faceplusplus.com/humanbodypp/beta/gesture";
        HashMap<String, String> map = new HashMap<>();
        HashMap<String, byte[]> byteMap = new HashMap<>();
        map.put("api_key", "cctdj2poMLDeKEWohDk-pHDPBwaRjzny");
        map.put("api_secret", "t27vk9h8Q6gDm-qabwEJgqSOI23yyHjD");
        map.put("return_landmark", "1");
        map.put("return_attributes", "gender,age,smiling,headpose,facequality,blur,eyestatus,emotion,ethnicity,beauty,mouthstatus,eyegaze,skinstatus");
        byteMap.put("image_file", buff);
        //map.put("image_base64", getImgStr("C:/skydianshi/1.jpg"));
        try{
            byte[] bacd = post(url, map, byteMap);
            String str = new String(bacd);
            System.out.println(str);
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    double gestures [] = new double[20];
    public String getGesture(String imageStr){
        /*File file = new File(path);
        byte[] buff = getBytesFromFile(file);*/
        String url = "https://api-cn.faceplusplus.com/humanbodypp/beta/gesture";
        HashMap<String, String> map = new HashMap<>();
        HashMap<String, byte[]> byteMap = new HashMap<>();
        map.put("api_key", "cctdj2poMLDeKEWohDk-pHDPBwaRjzny");
        map.put("api_secret", "t27vk9h8Q6gDm-qabwEJgqSOI23yyHjD");
        map.put("return_landmark", "1");
        map.put("return_attributes", "gender,age,smiling,headpose,facequality,blur,eyestatus,emotion,ethnicity,beauty,mouthstatus,eyegaze,skinstatus");
        //byteMap.put("image_file", buff);
        map.put("image_base64", imageStr);
        try{
            byte[] bacd = post(url, map, byteMap);
            String str = new String(bacd);
            JSONObject jsonObject = new JSONObject(str);
            JSONArray handsObject = jsonObject.getJSONArray("hands");
            JSONObject gestureObject = handsObject.getJSONObject(0);
            JSONObject gesture = gestureObject.getJSONObject("gesture");

            gestures[0] = gesture.getDouble("thumb_up");
            gestures[1] = gesture.getDouble("namaste");
            gestures[2] = gesture.getDouble("ok");
            gestures[3] = gesture.getDouble("beg");
            gestures[4] = gesture.getDouble("unknown");
            gestures[5] = gesture.getDouble("index_finger_up");
            gestures[6] = gesture.getDouble("thanks");
            gestures[7] = gesture.getDouble("phonecall");
            gestures[8] = gesture.getDouble("palm_up");
            gestures[9] = gesture.getDouble("big_v");
            gestures[10] = gesture.getDouble("double_finger_up");
            gestures[11] = gesture.getDouble("thumb_down");
            gestures[12] = gesture.getDouble("fist");
            gestures[13] = gesture.getDouble("rock");
            gestures[14] = gesture.getDouble("heart_d");
            gestures[15] = gesture.getDouble("hand_open");
            gestures[16] = gesture.getDouble("heart_b");
            gestures[17] = gesture.getDouble("heart_c");
            gestures[18] = gesture.getDouble("victory");
            gestures[19] = gesture.getDouble("heart_a");
        }catch (Exception e) {
            e.printStackTrace();
        }
        int max=0;
        for(int i=0;i<20;i++) {
            if (gestures[i] > max) {
                max = i;
            }
        }
        switch (max){
            case 0:
                return "点赞";
            case 1:
                return "合十";
            case 2:
                return "OK";
            case 3:
                return "作揖";
            case 4:
                return "unknown";
            case 5:
                return "食指朝上";
            case 6:
                return "谢谢";
            case 7:
                return "打电话";
            case 8:
                return "手掌朝上";
            case 9:
                return "大V";
            case 10:
                return "双食指朝上";
            case 11:
                return "大拇指朝下";
            case 12:
                return "握拳";
            case 13:
                return "ROCK";
            case 14:
                return "比心D";
            case 15:
                return "手掌打开";
            case 16:
                return "比心B";
            case 17:
                return "比心";
            case 18:
                return "胜利C";
            case 19:
                return "比心A";
            default:
                return null;
        }
    }



    public static String getImgStr(String imgFile){
        //将图片文件转化为字节数组字符串，并对其进行Base64编码处理
        InputStream in = null;
        byte[] data = null;
        //读取图片字节数组
        try
        {
            in = new FileInputStream(imgFile);
            data = new byte[in.available()];
            in.read(data);
            in.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return new String(Base64.encodeBase64(data));
    }

    private final static int CONNECT_TIME_OUT = 30000;
    private final static int READ_OUT_TIME = 50000;
    private static String boundaryString = getBoundary();
    protected static byte[] post(String url, HashMap<String, String> map, HashMap<String, byte[]> fileMap) throws Exception {
        HttpURLConnection conne;
        URL url1 = new URL(url);
        conne = (HttpURLConnection) url1.openConnection();
        conne.setDoOutput(true);
        conne.setUseCaches(false);
        conne.setRequestMethod("POST");
        conne.setConnectTimeout(CONNECT_TIME_OUT);
        conne.setReadTimeout(READ_OUT_TIME);
        conne.setRequestProperty("accept", "*/*");
        conne.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundaryString);
        conne.setRequestProperty("connection", "Keep-Alive");
        conne.setRequestProperty("user-agent", "Mozilla/4.0 (compatible;MSIE 6.0;Windows NT 5.1;SV1)");
        DataOutputStream obos = new DataOutputStream(conne.getOutputStream());
        Iterator iter = map.entrySet().iterator();
        while(iter.hasNext()){
            Map.Entry<String, String> entry = (Map.Entry) iter.next();
            String key = entry.getKey();
            String value = entry.getValue();
            obos.writeBytes("--" + boundaryString + "\r\n");
            obos.writeBytes("Content-Disposition: form-data; name=\"" + key
                    + "\"\r\n");
            obos.writeBytes("\r\n");
            obos.writeBytes(value + "\r\n");
        }
        if(fileMap != null && fileMap.size() > 0){
            Iterator fileIter = fileMap.entrySet().iterator();
            while(fileIter.hasNext()){
                Map.Entry<String, byte[]> fileEntry = (Map.Entry<String, byte[]>) fileIter.next();
                obos.writeBytes("--" + boundaryString + "\r\n");
                obos.writeBytes("Content-Disposition: form-data; name=\"" + fileEntry.getKey()
                        + "\"; filename=\"" + encode(" ") + "\"\r\n");
                obos.writeBytes("\r\n");
                obos.write(fileEntry.getValue());
                obos.writeBytes("\r\n");
            }
        }
        obos.writeBytes("--" + boundaryString + "--" + "\r\n");
        obos.writeBytes("\r\n");
        obos.flush();
        obos.close();
        InputStream ins = null;
        int code = conne.getResponseCode();
        try{
            if(code == 200){
                ins = conne.getInputStream();
            }else{
                ins = conne.getErrorStream();
            }
        }catch (SSLException e){
            e.printStackTrace();
            return new byte[0];
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buff = new byte[4096];
        int len;
        while((len = ins.read(buff)) != -1){
            baos.write(buff, 0, len);
        }
        byte[] bytes = baos.toByteArray();
        ins.close();
        return bytes;
    }
    private static String getBoundary() {
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for(int i = 0; i < 32; ++i) {
            sb.append("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789_-".charAt(random.nextInt("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789_".length())));
        }
        return sb.toString();
    }
    private static String encode(String value) throws Exception{
        return URLEncoder.encode(value, "UTF-8");
    }

    public static byte[] getBytesFromFile(File f) {
        if (f == null) {
            return null;
        }
        try {
            FileInputStream stream = new FileInputStream(f);
            ByteArrayOutputStream out = new ByteArrayOutputStream(1000);
            byte[] b = new byte[1000];
            int n;
            while ((n = stream.read(b)) != -1)
                out.write(b, 0, n);
            stream.close();
            out.close();
            return out.toByteArray();
        } catch (IOException e) {
        }
        return null;
    }
}