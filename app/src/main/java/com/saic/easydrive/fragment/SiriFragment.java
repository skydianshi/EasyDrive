package com.saic.easydrive.fragment;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.speech.RecognitionListener;
import android.speech.SpeechRecognizer;
import android.support.v4.app.Fragment;
import android.util.AndroidRuntimeException;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;


import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.baidu.speech.EventManager;
import com.baidu.speech.EventManagerFactory;
import com.baidu.speech.VoiceRecognitionService;
import com.baidu.tts.auth.AuthInfo;
import com.baidu.tts.client.SpeechError;
import com.baidu.tts.client.SpeechSynthesizer;
import com.baidu.tts.client.SpeechSynthesizerListener;
import com.baidu.tts.client.TtsMode;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.saic.easydrive.activities.MainActivity;
import com.saic.easydrive.R;
import com.saic.easydrive.service.MusicService;
import com.saic.easydrive.ui.SiriView;
import com.saic.easydrive.ui.chatview.ChatAdapter;
import com.saic.easydrive.ui.chatview.ChatMessage;
import com.saic.easydrive.util.DeviceListActivity;
import com.saic.easydrive.util.HandRec;
import com.saic.easydrive.util.JsoupUtil;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.nodes.Document;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static android.app.Activity.RESULT_OK;

public class SiriFragment extends Fragment implements RecognitionListener,SpeechSynthesizerListener,SurfaceHolder.Callback,
        Camera.PictureCallback{
    private static final String TAG = "VoiceFragment";
    private static final int PHOTO_TAKE = 0;
    private static final int PHONE_CALL = 1;
    private static final int MESSAGE_SEND = 2;
    private static final int APPLICATION_OPEN = 3;
    private static final int TURING_ROBOT = 4;
    private static final int SPEAK_FINISH = 5;
    private static final int MAP_SERVICE = 6;
    private static final int POI_SEARCH = 7;
    private static final int INFORMATION_SEARCH = 8;//查询
    private static final int SEARCHRESULT_SHOW = 9;//显示查询结果
    private static final int CAR_ORDER = 10;//控制汽车指令
    private static final int RETURN_TTS = 11;//汽车返回操作的反馈
    private static final int FIGURE_GET = 12;//获取汽车数据
    private static final int PLAY_MUSIC = 13;//播放音乐
    private static final int NET_SONG = 14;//下一首歌
    private static final int PRE_SONG = 15;//上一首歌
    private static final int STOP_SONG = 16;//停止播放

    private static final int RECOGNIZE_START = 100;//开始识别

    private static final String TURING_URL = "http://www.tuling123.com/openapi/api";
    private static final String TURING_KEY = "f51d65cdc936468c9b1364c977a3ef53";
    private static final String USER_ID = "15365512129";

    boolean is_init = false;//判断语音是否初始化
    private BluetoothDevice _device = null;     //蓝牙设备
    private BluetoothSocket _socket = null;      //蓝牙通信socket
    private final static int REQUEST_CONNECT_DEVICE = 10;    //宏定义查询设备句柄
    private final static String MY_UUID = "00001101-0000-1000-8000-00805F9B34FB";   //SPP服务UUID号
    private BluetoothAdapter _bluetooth = BluetoothAdapter.getDefaultAdapter();  //获取本地蓝牙适配器，即蓝牙设备
    OutputStream os;
    InputStream in;

    //手势识别
    private SurfaceView mSurfaceView;
    private SurfaceHolder mHolder;
    private Camera mCamera;
    private Handler mHandler = new Handler();
    private int mCurrentTimer = 2;//每两秒刷新一次
    private boolean mIsSurfaceCreated = false;//相机界面被创建
    private boolean mIsTimerRunning = false;//是否一直拍照
    private static final int BACK_CAMERA_ID = 0; //后置摄像头
    private static final int FORWORD_CAMERA_ID = 1; //前置摄像头

    private static final int REQUEST_UI = 1;
    private SpeechSynthesizer mSpeechSynthesizer;//用来说话的，哈哈
    //private TextView txtLog;
    private ImageButton begin;
    private ListView chatView;
    //private Button setting;这个用来配置百度语音，需要的时候需要重新调出来，千万不能删
    SiriView siriView;
    private WebView webView;
    private SpeechRecognizer speechRecognizer;
    //唤醒功能
    private EventManager mWpEventManager;
    ArrayList<HashMap<String,String>> contacts;
    String phoneNumber;
    RequestQueue mQueue;//http请求队列
    private String speakWords;//根据指令百度语音所做出的回复，用来判断是否需要再问一次
    private boolean turingSpeak = false;//设置一个bool变量保证指令信息与图灵信息互不干扰
    private List<ChatMessage> msgList = new ArrayList<ChatMessage>();//消息队列
    private String[] heads = new String[10];//显示10条记录
    private String[] descriptions = new String[10];
    private String[] urls = new String[10];
    List<Map<String, Object>> simpleListItems = new ArrayList<>();
    SimpleAdapter simpAdapter;
    JsoupUtil jsoupUtil = new JsoupUtil();//jsoup爬虫工具

    View view;

    /*
   * 处理语音指令
   * */
    private Handler resultHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 0:
                    openCamera();
                    break;
                case 1:
                    String result1 = (String)msg.obj;
                    //takePhone(result1);
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_CALL);
                    //url:统一资源定位符
                    //uri:统一资源标示符（更广）
                    intent.setData(Uri.parse("tel:15365512129"));
                    //开启系统拨号器
                    startActivity(intent);
                    startTTs("正在帮您拨打电话");
                    break;
                case 2:
                    openMess();
                    break;
                case 3:
                    String result3 = (String)msg.obj;
                    openApplication(result3);
                    break;
                case 4:
                    String result4 = (String)msg.obj;
                    requestTuring(result4);
                    break;
                case 5:
                    startTTs("好的，主人，我会想你的，么么哒");
                    getActivity().finish();
                    break;
                case 6:
                    String result6 = (String)msg.obj;
                    handleMapRequest(result6);
                    break;
                case 7:
                    String result7 = (String)msg.obj;
                    handlePoiRequest(result7);
                    break;
                case 8://8，9是连起来使用的，8用来查询（查询涉及网络，要新线程），9用来显示，显示要初始线程，所以要在handler操作，所以需要两个case
                    final String result8 = (String)msg.obj;
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            handleSearch(result8);
                        }
                    }).start();
                    startTTs("为您找到如下网页信息");
                    break;
                case 9:
                    chatView.setAdapter(simpAdapter);
                    chatView.setVisibility(View.VISIBLE);
                    webView.setVisibility(View.INVISIBLE);
                    break;
                case 10:
                    String result10 = (String)msg.obj;
                    try {
                        handleCarOrder(result10);
                    } catch (Exception e) {
                        startTTs("未连接汽车，请连接后重试");
                        e.printStackTrace();
                    }
                    break;
                case 11:
                    String result11 = (String)msg.obj;
                    startTTs(result11);
                    break;
                case 12:
                    startTTs("正在为您获取汽车数据");
                    break;
                case 13:
                    musicService.startPlay();
                    break;
                case 14:
                    musicService.playNextSong();
                    break;
                case 15:
                    musicService.playPreSong();
                    break;
                case 16:
                    musicService.stopPlay();
                    break;
                case 100:
                    startRecognize();
                    break;
            }
        }
    };
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_siri, container, false);
        initView();
        mQueue = Volley.newRequestQueue(getActivity());
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(getActivity(), new ComponentName(getActivity(), VoiceRecognitionService.class));
        speechRecognizer.setRecognitionListener(this);

        //initGesture();
        return view;
    }

    public void initView(){
        //txtLog = (TextView) findViewById(R.id.txtLog);
        begin = (ImageButton) view.findViewById(R.id.btn);
        //setting = (Button)findViewById(R.id.setting);
        webView = (WebView)view.findViewById(R.id.webView);
        //设置WebView属性，能够执行Javascript脚本
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setBlockNetworkImage(false);//解决图片不显示
        webView.getSettings().setDomStorageEnabled(true);//解决地图不显示问题
        //设置Web视图
        webView.setWebViewClient(new HelloWebViewClient ());

        chatView = (ListView)view.findViewById(R.id.chatView);
        chatView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                webView.loadUrl(urls[i]);
                webView.setVisibility(View.VISIBLE);
                chatView.setVisibility(View.INVISIBLE);
            }
        });
        /*setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //startTTs("你好，主人！");
                Intent intent = new Intent(VoiceActivity.this,Setting.class);
                startActivity(intent);
            }
        });*/
        begin.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                musicService.stopPlay();
                startRecognize();
            }
        });
        begin.setOnLongClickListener(new View.OnLongClickListener(){

            @Override
            public boolean onLongClick(View view) {
              /*  Intent intent = new Intent(VoiceActivity.this,Setting.class);
                startActivity(intent);
                return  false;*/

                if(_bluetooth.isEnabled()==false){  //如果蓝牙服务不可用则提示
                    //打开蓝牙
                    new Thread(){
                        public void run(){
                            if(_bluetooth.isEnabled()==false){
                                _bluetooth.enable();
                            }

                        }
                    }.start();
                    return false;
                }
                if(_socket==null){

                    _device = _bluetooth.getRemoteDevice("20:17:11:16:54:68");
                    Toast.makeText(getActivity(), "连接"+_device.getName()+"成功！", Toast.LENGTH_SHORT).show();
                    try {
                        _socket = _device.createRfcommSocketToServiceRecord(UUID.fromString(MY_UUID));
                        _socket.connect();
                        System.out.println("连接成功");
                    }catch (IOException e) {
                        System.out.println("连接失败");
                    }
                    try {
                        os = _socket.getOutputStream();
                        in = _socket.getInputStream();
                        //接收控制汽车后的反馈信号
                        new Thread(returnVoice).start();
                    }catch (IOException e){
                        System.out.println("-------获取通道失败");
                    }
                  /*  Intent serverIntent = new Intent(getActivity(), DeviceListActivity.class); //跳转程序设置
                    startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);  //设置返回宏定义*/
                }
                else {
                    Toast.makeText(getActivity(),"socket连接出错，请重新连接或重启程序！",Toast.LENGTH_SHORT);
                }
                return true;
            }
        });

        siriView = (SiriView) view.findViewById(R.id.siriView);
        // 设置曲线高度，height的取值是0f~1f
        siriView.setWaveHeight(0.3f);
        // 设置曲线的粗细，width的取值大于0f
        siriView.setWaveWidth(5f);
        // 设置曲线颜色
        siriView.setWaveColor(Color.rgb(39, 188, 136));
        // 设置曲线在X轴上的偏移量，默认值为0f
        siriView.setWaveOffsetX(0f);
        // 设置曲线的数量，默认是4
        siriView.setWaveAmount(4);
        // 设置曲线的速度，默认是0.1f
        siriView.setWaveSpeed(0.1f);
        siriView.setVisibility(View.INVISIBLE);
    }

    //播放音乐Service
    private MusicService musicService;
    ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {

        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            //返回一个MsgService对象
            musicService = ((MusicService.MsgBinder)service).getService();

        }
    };

    @Override
    public void onResume() {
        super.onResume();
        MainActivity.toolbar.setTitle("语音出行");
        // 唤醒功能打开步骤
        // 1) 创建唤醒事件管理器
        mWpEventManager = EventManagerFactory.create(getActivity(), "wp");

        // 2) 注册唤醒事件监听器
        mWpEventManager.registerListener(new com.baidu.speech.EventListener() {
            @Override
            public void onEvent(String name, String params, byte[] data, int offset, int length) {
                Log.d(TAG, String.format("event: name=%s, params=%s", name, params));
                try {
                    JSONObject json = new JSONObject(params);
                    if ("wp.data".equals(name)) { // 每次唤醒成功, 将会回调name=wp.data的时间, 被激活的唤醒词在params的word字段
                        String word = json.getString("word");
                        begin.performClick();
                    } else if ("wp.exit".equals(name)) {

                    }
                } catch (JSONException e) {
                    throw new AndroidRuntimeException(e);
                }
            }
        });

        // 3) 通知唤醒管理器, 启动唤醒功能
        HashMap params = new HashMap();
        params.put("kws-file", "assets:///WakeUp.bin"); // 设置唤醒资源, 唤醒资源请到 http://yuyin.baidu.com/wake#m4 来评估和导出
        mWpEventManager.send("wp.start", new JSONObject(params).toString(), null, 0, 0);
        if(!is_init){
            //初始化语音合成
            initialTTS();
            is_init = true;
        }

        //绑定播放音乐服务
        Intent musicIntent = new Intent(getActivity(),MusicService.class);
        getActivity().bindService(musicIntent, conn, Context.BIND_AUTO_CREATE);
    }



    @Override
    public void onPause() {
        super.onPause();
        mWpEventManager.send("wp.stop", null, null, 0, 0);
        mIsTimerRunning = false;
        stopPreview();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode){
            case RESULT_OK:
                onResults(data.getExtras());
                break;
            case REQUEST_CONNECT_DEVICE:     //连接结果，由DeviceListActivity设置返回
                // 响应返回结果
                if (resultCode == RESULT_OK) {   //连接成功，由DeviceListActivity设置返回
                    // MAC地址，由DeviceListActivity设置返回
                    String address = data.getExtras()
                            .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                    // 得到蓝牙设备句柄
                    _device = _bluetooth.getRemoteDevice(address);
                    Toast.makeText(getActivity(), "连接"+_device.getName()+"成功！", Toast.LENGTH_SHORT).show();
                    try {
                        _socket = _device.createRfcommSocketToServiceRecord(UUID.fromString(MY_UUID));
                        _socket.connect();
                        System.out.println("连接成功");
                    }catch (IOException e) {
                        System.out.println("连接失败");
                    }
                    try {
                        os = _socket.getOutputStream();
                        in = _socket.getInputStream();
                        //接收控制汽车后的反馈信号
                        new Thread(returnVoice).start();
                    }catch (IOException e){
                        System.out.println("-------获取通道失败");
                    }
                }
                else {
                    Toast.makeText(getActivity(),"蓝牙连接失败，请重试！",Toast.LENGTH_SHORT);
                }
                break;
            default:break;
        }
    }

    //控制汽车的指令反馈
    Runnable returnVoice = new Runnable() {
        char c;
        StringBuilder res = new StringBuilder();
        @Override
        public void run() {
            try {
                c = (char)((byte)in.read());

                while (c != '>'){
                    if(c!='*'){
                        res.append(c);
                    }else{
                        String line = res.toString();
                        Message message = new Message();
                        if(line.equals("+START ON")){
                            message.obj = "点火成功";
                            message.what = RETURN_TTS;
                            resultHandler.sendMessage(message);
                        }
                        else if(line.equals("+START OF")){
                            message.obj = "熄火完成";
                            message.what = RETURN_TTS;
                            resultHandler.sendMessage(message);
                        }
                        else if(line.equals("+MAIN UP")){
                            message.obj = "正在打开主车窗";
                            message.what = RETURN_TTS;
                            resultHandler.sendMessage(message);
                        }
                        else if(line.equals("+MWIN DO")){
                            message.obj = "正在关闭主车窗";
                            message.what = RETURN_TTS;
                            resultHandler.sendMessage(message);
                        }
                        else if(line.equals("+MRAIN UP")){
                            message.obj = "正在打开次车窗";
                            message.what = RETURN_TTS;
                            resultHandler.sendMessage(message);
                        }
                        else if(line.equals("+MRWIN DO")){
                            message.obj = "次车窗正在关闭";
                            message.what = RETURN_TTS;
                            resultHandler.sendMessage(message);
                        }
                        else if(line.equals("+HLED ON")){
                            message.obj = "远光灯已打开";
                            message.what = RETURN_TTS;
                            resultHandler.sendMessage(message);
                        }
                        else if(line.equals("+HLED OF")){
                            message.obj = "远光灯已关闭";
                            message.what = RETURN_TTS;
                            resultHandler.sendMessage(message);
                        }
                        else if(line.equals("+WIPER ON")){
                            message.obj = "雨刮已打开";
                            message.what = RETURN_TTS;
                            resultHandler.sendMessage(message);
                        }
                        else if(line.equals("+WIPER OF")){
                            message.obj = "雨刮已关闭";
                            message.what = RETURN_TTS;
                            resultHandler.sendMessage(message);
                        }
                        System.out.println(line);
                        res = new StringBuilder();
                    }
                    System.out.println(c);
                    c = (char)((byte)in.read());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };

    @Override
    public void onReadyForSpeech(Bundle bundle) {

    }

    @Override
    public void onBeginningOfSpeech() {

    }

    @Override
    public void onRmsChanged(float v) {

    }

    @Override
    public void onBufferReceived(byte[] bytes) {

    }

    @Override
    public void onEndOfSpeech() {
        siriView.setVisibility(View.INVISIBLE);
        begin.setVisibility(View.VISIBLE);
    }

    @Override
    public void onError(int error) {
        StringBuilder sb = new StringBuilder();
        switch (error) {
            case SpeechRecognizer.ERROR_AUDIO:
                sb.append("音频问题");
                break;
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                sb.append("没有语音输入");
                break;
            case SpeechRecognizer.ERROR_CLIENT:
                sb.append("其它客户端错误");
                break;
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                sb.append("权限不足");
                break;
            case SpeechRecognizer.ERROR_NETWORK:
                sb.append("网络问题");
                break;
            case SpeechRecognizer.ERROR_NO_MATCH:
                sb.append("没有匹配的识别结果");
                break;
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                sb.append("引擎忙");
                break;
            case SpeechRecognizer.ERROR_SERVER:
                sb.append("服务端错误");
                break;
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                sb.append("连接超时");
                break;
        }
        sb.append(":" + error);
        startTTs("对不起，我没听清楚您说的是什么");
        siriView.setVisibility(View.INVISIBLE);
        begin.setVisibility(View.VISIBLE);
    }

    @Override
    public void onResults(Bundle results) {
        ArrayList<String> nbest = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
      /*  String preResult = Arrays.toString(nbest.toArray(new String[nbest.size()]));
        System.out.println(preResult+"------------------");
        String result = preResult.substring(1,preResult.length()-1);*/
        String result = nbest.get(0);
        Toast.makeText(getActivity(),"识别成功："+result,Toast.LENGTH_SHORT).show();
        //将识别结果加入对话框
        ChatMessage msg = new ChatMessage();
        msg.setType(ChatAdapter.VALUE_LEFT_TEXT);
        msg.setValue(result);
        msgList.add(msg);
        chatView.setAdapter(new ChatAdapter(getActivity(),msgList));
        recognize(result);
        //time = 0;
    }

    //对识别结果进行分析
    public void recognize(String temp) {
        Message message = new Message();
        message.obj = temp;
        if(temp.contains("相机")||temp.contains("拍照")||temp.contains("照相")){
            resultHandler.sendEmptyMessage(PHOTO_TAKE);
        } else if(temp.contains("联系")||temp.contains("呼叫")||temp.contains("打电话给")){
            message.what = PHONE_CALL;
            resultHandler.sendMessage(message);
        }else if(temp.contains("短信")||temp.contains("信息")){
            resultHandler.sendEmptyMessage(MESSAGE_SEND);
        }
        else if(temp.contains("出发") || temp.contains("熄火")||temp.contains("玻璃")||temp.contains("远光灯")||temp.contains("雨刮")
                ||temp.contains("温度")||temp.contains("空调")) {
            message.what = CAR_ORDER;
            resultHandler.sendMessage(message);
        }else if(temp.contains("打开")){
            message.what = APPLICATION_OPEN;
            resultHandler.sendMessage(message);
        }
        else if(temp.contains("再见") || temp.contains("拜拜")) {
            resultHandler.sendEmptyMessage(SPEAK_FINISH);
        } else if(temp.contains("导航") || temp.contains("怎么走")||temp.contains("路线")||temp.contains("地铁")||temp.contains("公交")
                ||temp.contains("号线")||temp.contains("在哪")) {
            message.what = MAP_SERVICE;
            resultHandler.sendMessage(message);
        }else if(temp.contains("附近的")){
            message.what = POI_SEARCH;
            resultHandler.sendMessage(message);
        }else if(temp.contains("查一下")){
            message.what = INFORMATION_SEARCH;
            resultHandler.sendMessage(message);
        }else if(temp.contains("获取汽车数据")){
            message.what = FIGURE_GET;
            resultHandler.sendMessage(message);
        }else if(temp.contains("歌")||temp.contains("音乐")){
            message.what = PLAY_MUSIC;
            resultHandler.sendMessage(message);
        }else if(temp.contains("下一首")){
            message.what = NET_SONG;
            resultHandler.sendMessage(message);
        }else if (temp.contains("上一首")){
            message.what = PRE_SONG;
            resultHandler.sendMessage(message);
        }else if(temp.contains("停止")){
            message.what = STOP_SONG;
            resultHandler.sendMessage(message);
        }
        else{
            message.what = TURING_ROBOT;
            resultHandler.sendMessage(message);
            turingSpeak = true;
        }
    }

    @Override
    public void onPartialResults(Bundle bundle) {

    }

    @Override
    public void onEvent(int i, Bundle bundle) {

    }

    @Override
    public void onSynthesizeStart(String s) {
        // 监听到合成开始，在此添加相关操作
        System.out.println("开始合成");
    }

    @Override
    public void onSynthesizeDataArrived(String s, byte[] bytes, int i) {

    }

    @Override
    public void onSynthesizeFinish(String s) {
        // 监听到合成结束，在此添加相关操作
        System.out.println("合成结束");
    }

    @Override
    public void onSpeechStart(String s) {
        // 监听到合成并播放开始，在此添加相关操作
        System.out.println("开始播放");
    }

    @Override
    public void onSpeechProgressChanged(String s, int i) {

    }

    @Override
    public void onSpeechFinish(String s) {
        // 监听到播放结束，在此添加相关操作
        System.out.println("播放结束");
        if(turingSpeak){
            //根据所做的回复判定是否需要再问一次，比如（回复您要定哪里的酒店，这句话里有哪，这时候就需要再问一遍）
            if(speakWords.contains("哪")){
                //开始识别只能在主线程中进行
                resultHandler.sendEmptyMessage(RECOGNIZE_START);
                //清空回复，防止对后面的对话造成影响
                speakWords = "";
            }
            turingSpeak = false;
        }else{
            Log.d(TAG,"这是指令返回的消息！");
        }
    }

    @Override
    public void onError(String s, SpeechError speechError) {
        // 监听到出错，在此添加相关操作
        System.out.println("error");
    }

    //打开相机
    public void openCamera(){
        if(true) {
            startTTs("马上为您打开相机");
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivity(intent);
        }else{
            startTTs("相机打开失败");
        }
    }
    //打开短信界面
    public void openMess(){
        startTTs("马上为您打开短信");
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setType("vnd.android-dir/mms-sms");
        startActivity(intent);
    }
    //打电话
    public  void takePhone(String result){
        //得到通讯录中的所有电话号码
        contacts = getContacts();
        String pinyinOrder = getPinYin(result);
        if(pinyinOrder.length()>6){
            String preOrder = pinyinOrder.substring(0,6);
            String callName = pinyinOrder.substring(6);
            //String preOrder2 = pinyinOrder.substring(0,11);
            //String callName2 = pinyinOrder.substring(11);
            if(preOrder.equals("hujiao")||preOrder.equals("lianxi")){
                callSomeone(callName);
            }
         /*   else if(preOrder2.equals("dadianhuaji")){
                callSomeone(callName2);
            }*/
        }
    }
    //得到通讯录中的所有电话号码
    public ArrayList<HashMap<String,String>> getContacts(){
        Uri rawContactsUri = Uri.parse("content://com.android.contacts/raw_contacts");
        Uri dataUri = Uri.parse("content://com.android.contacts/data");

        ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();

        // 从raw_contacts中读取所有联系人的id("contact_id")
        Cursor rawContactsCursor = getActivity().getContentResolver().query(rawContactsUri,
                new String[] { "contact_id" }, null, null, null);
        if (rawContactsCursor != null) {
            while (rawContactsCursor.moveToNext()) {
                String contactId = rawContactsCursor.getString(0);
                // System.out.println("得到的contact_id="+contactId);

                // 根据contact_id从data表中查询出相应的电话号码和联系人名称, 实际上查询的是视图view_data
                Cursor dataCursor = getActivity().getContentResolver().query(dataUri,
                        new String[] { "data1", "mimetype" }, "contact_id=?",
                        new String[] { contactId }, null);

                if (dataCursor != null) {
                    HashMap<String, String> map = new HashMap<String, String>();
                    while (dataCursor.moveToNext()) {
                        String data1 = dataCursor.getString(0);
                        String mimetype = dataCursor.getString(1);
                        // System.out.println(contactId + ";" + data1 + ";"
                        // + mimetype);
                        if ("vnd.android.cursor.item/phone_v2".equals(mimetype)) {//手机号码
                            map.put("phone", data1);
                        } else if ("vnd.android.cursor.item/name".equals(mimetype)) {//联系人名字
                            map.put("name", data1);
                        }
                    }
                    list.add(map);
                    dataCursor.close();
                }
            }
            rawContactsCursor.close();
        }
        return list;
    }
    //打电话给某人
    public void callSomeone(String name){
        boolean hasName = false;
        Iterator it = contacts.iterator();
        while (it.hasNext()){
            HashMap<String,String> contact = (HashMap<String,String>)it.next();
            if(getPinYin(contact.get("name")).equals(name)){//通过拼音对比更为准确
                phoneNumber = contact.get("phone");
                hasName = true;
                break;
            }
        }
        if(hasName){
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_CALL);
            //url:统一资源定位符
            //uri:统一资源标示符（更广）
            intent.setData(Uri.parse("tel:" + phoneNumber));
            //开启系统拨号器
            startActivity(intent);
            startTTs("正在帮您拨打电话");
        }else{
            startTTs("对不起，没有找到相关联系人");
        }

    }

    //打开外部应用
    public void openApplication(String result){
        if(result.contains("百度")){
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.baidu.com"));
            startActivity(intent);
        }else if(result.contains("地图")){
            Intent LaunchIntent = getActivity().getPackageManager().getLaunchIntentForPackage("com.baidu.BaiduMap");
            startActivity(LaunchIntent);
        }else{
            startTTs("暂时没有增加这个功能，请静待更新");
        }
    }
    //地图请求
    public void handleMapRequest(String result){
        String destination = "上海";
        String startPlace = "北京";
        if(result.contains("导航去")){
            int index = result.indexOf("导航去");
            destination = result.substring(index+3);
            StringBuffer stringBuffer = new StringBuffer("baidumap://map/navi?query="+destination)
                    .append("&type=TIME");
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(stringBuffer.toString()));
            intent.setPackage("com.baidu.BaiduMap");
            startActivity(intent);
            startTTs("正在为您导航");
        }else if(result.contains("怎么走")&&result.contains("到")){
            int index1 = result.indexOf("到");
            int index2 = result .indexOf("怎么走");
            startPlace = result.substring(0,index1);
            destination = result.substring(index1+1,index2);
            System.out.println(startPlace+" "+destination);
            webViewLoadUrl("http://api.map.baidu.com/direction?origin="+startPlace+"&destination="+destination+"&region=中国&mode=driving&output=html");
            startTTs("为您找到以下路线");
        }else if(result.contains("公交")||result.contains("地铁")||result.contains("号线")){
            webViewLoadUrl("http://api.map.baidu.com/line?name="+result+"&output=html");
            startTTs("为您找到以下信息");
        }else if(result.contains(("在哪"))){
            String address = result.substring(0,result.length()-2);
            webViewLoadUrl("http://api.map.baidu.com/geocoder?address="+address+"&output=html");
            startTTs("为您找到以下信息");
        }
    }

    //poi检索
    public void handlePoiRequest(String result){
        int index = result.indexOf("附近的");
        final String query = result.substring(index+3);
        StringRequest stringRequest = new StringRequest("http://api.map.baidu.com/location/ip?ak=qrxwBqIV5h7U10wgfP9ahSeaRMNPvEI3&coor=bd09ll",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("TAG", response);

                        JsonParser parser=new JsonParser();  //创建JSON解析器
                        JsonObject object= (JsonObject) parser.parse(response);  //创建JsonObject对象
                        JsonObject contentObject = object.getAsJsonObject("content");
                        JsonObject pointObject = contentObject.getAsJsonObject("point");
                        String xAxis = pointObject.get("x").getAsString();
                        String yAxis = pointObject.get("y").getAsString();
                        JsonObject detailObject = contentObject.getAsJsonObject("address_detail");
                        String city = detailObject.get("city").getAsString();
                        String province = detailObject.get("province").getAsString();
                        webViewLoadUrl("http://api.map.baidu.com/place/search?query="+query+"&location="+yAxis+","+xAxis+"&radius=1000&region="+city+"&output=html");
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("TAG", error.getMessage(), error);
            }
        });
        mQueue.add(stringRequest);
        startTTs("为您找到以下信息");
    }
    //加载url
    public  void webViewLoadUrl(String url){
        webView.setVisibility(View.VISIBLE);
        chatView.setVisibility(View.INVISIBLE);
        webView.loadUrl(url);
    }

    //查询操作
    public void handleSearch(String result){
        String information = result.substring(3);
        Document document = jsoupUtil.getDocument(information);
        System.out.println(information+"     "+result);
        int index = 10;
        for(int i = 0;i<10;i++){
            //防止没有10条搜索结果，添加try catch操作
            try{
                String [] message = jsoupUtil.getMessage(document,i);
                heads[i] = message[0];
                descriptions[i] = message[1];
                urls[i] = message[2];
            }catch (IndexOutOfBoundsException e){
                index = i;
                System.out.println("只有"+i+"条搜索结果！");
                break;
            }

        }
        simpleListItems.clear();
        for (int i = 0; i < index; i++) {
            Map<String, Object> listem = new HashMap<String, Object>();
            listem.put("head", heads[i]);
            listem.put("url", urls[i]);
            listem.put("description", descriptions[i]);
            simpleListItems.add(listem);
        }
        simpAdapter = new SimpleAdapter(getActivity(), simpleListItems,
                R.layout.simple_item, new String[] { "head", "url", "description" },
                new int[] {R.id.head,R.id.url,R.id.description});
        resultHandler.sendEmptyMessage(SEARCHRESULT_SHOW);
    }
    //图灵请求
    public void requestTuring(final String result){
        StringRequest stringRequest = new StringRequest(TURING_URL+"?&info="+result+"&key="+TURING_KEY+"&userid="+USER_ID,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("TAG", response);

                        JsonParser parser=new JsonParser();  //创建JSON解析器
                        JsonObject object= (JsonObject) parser.parse(response);  //创建JsonObject对象
                        String text = object.get("text").getAsString();
                        speakWords = text;
                        System.out.println("text="+text);
                        startTTs(text);
                        String url;
                        try{
                            url = object.get("url").getAsString();
                            System.out.println(url);
                            webViewLoadUrl(url);

                        }catch (NullPointerException e){
                            Log.d(TAG,"没有返回url地址！");
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("TAG", error.getMessage(), error);
            }
        });
        mQueue.add(stringRequest);
    }
    //汽车指令
    public void handleCarOrder(String result) throws IOException {
        if(result.contains("出发")){
            os.write("+START ON".getBytes());
            System.out.println("------------------------");
        }else if(result.contains("熄火")){
            os.write("+START OF".getBytes());
        }
        else if(result.contains("打开")&&result.contains("远光灯")){
            os.write("+HLED ON".getBytes());
        }
        else if(result.contains("关闭")&&result.contains("远光灯")){
            os.write("+HLED OF".getBytes());
        }
        else if(result.contains("打开")&&result.contains("玻璃")&&result.contains("主")){
            os.write("+MWIN UP".getBytes());
        }
        else if(result.contains("关闭")&&result.contains("玻璃")&&result.contains("主")){
            os.write("+MWIN DO".getBytes());
        }
        else if(result.contains("打开")&&result.contains("玻璃")&&result.contains("副")){
            os.write("+MRWIN UP".getBytes());
        }
        else if(result.contains("关闭")&&result.contains("玻璃")&&result.contains("副")){
            os.write("+MRWIN DO".getBytes());
        }
        else if(result.contains("打开")&&result.contains("雨刮")){
            os.write("+WIPER ON".getBytes());
        }
        else if(result.contains("关闭")&&result.contains("雨刮")){
            os.write("+WIPER OF".getBytes());
        }
        else if(result.contains("温度")){
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            startTTs("现在车内温度15摄氏度，建议打开空调");
        }
    }

    //开启语音识别
    public void startRecognize(){
        //开始识别的时候要停止语音播放
        mSpeechSynthesizer.stop();
        //模拟Siri展示
        siriView.setVisibility(View.VISIBLE);
        begin.setVisibility(View.INVISIBLE);
        chatView.setVisibility(View.VISIBLE);
        webView.setVisibility(View.INVISIBLE);
        Intent intent = new Intent();
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        {

            String args = sp.getString("args", "");
            if (null != args) {
                intent.putExtra("args", args);
            }
        }
        //去除百度对话框界面
        /*boolean api = sp.getBoolean("api", false);
        if (api) {
            speechRecognizer.startListening(intent);
        } else {
            intent.setAction("com.baidu.action.RECOGNIZE_SPEECH");
            startActivityForResult(intent, REQUEST_UI);
        }*/
        speechRecognizer.startListening(intent);
    }

    //将汉字转化为拼音，用这个函数需要导入pinyin4j-2.5.0.jar包，下载地址http://download.csdn.net/download/seamless_yang/6962659#
    public static StringBuffer sb = new StringBuffer();
    public static String getPinYin(String chines) {
        sb.setLength(0);
        char[] nameChar = chines.toCharArray();
        HanyuPinyinOutputFormat defaultFormat = new HanyuPinyinOutputFormat();
        defaultFormat.setCaseType(HanyuPinyinCaseType.LOWERCASE);
        defaultFormat.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
        for (int i = 0; i < nameChar.length; i++) {
            if (nameChar[i] > 128) {
                try {
                    sb.append(PinyinHelper.toHanyuPinyinStringArray(nameChar[i], defaultFormat)[0]);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                sb.append(nameChar[i]);
            }
        }
        return sb.toString();
    }

    //Web视图
    private class HelloWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            //System.out.println(url);
            //不知道为什么到这里url会变化，加上这句话就加载不出来了！
            //view.loadUrl(url);
            return false;
        }
    }


    @Override
    public void onDestroyView() {
        speechRecognizer.destroy();
        if(_socket!=null){
            try {
                _socket.close();
                os.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        super.onDestroyView();
    }

    //初始化语音合成
    private void initialTTS() {
        // 获取语音合成对象实例
        mSpeechSynthesizer = SpeechSynthesizer.getInstance();
        // 设置context
        mSpeechSynthesizer.setContext(getActivity());
        // 设置语音合成状态监听器
        mSpeechSynthesizer.setSpeechSynthesizerListener(this);
        // 设置在线语音合成授权，需要填入从百度语音官网申请的api_key和secret_key
        mSpeechSynthesizer.setApiKey("r0tV0gpCkQmlg3WOV2QcgktL", "7VbYiPOvmDKbejgVvRcsQBd6ACTmBzz5");
        // 设置离线语音合成授权，需要填入从百度语音官网申请的app_id
        mSpeechSynthesizer.setAppId("11193638");
        // 获取语音合成授权信息
        AuthInfo authInfo = mSpeechSynthesizer.auth(TtsMode.MIX);
        // 判断授权信息是否正确，如果正确则初始化语音合成器并开始语音合成，如果失败则做错误处理
        if (authInfo.isSuccess()) {
            mSpeechSynthesizer.initTts(TtsMode.MIX);
            System.out.println("授权成功");
        } else {
            // 授权失败
            System.out.println("授权失败");
        }
    }
    public void startTTs(String words){
        mSpeechSynthesizer.speak(words);
        //将回复加入聊天框
        ChatMessage msg = new ChatMessage();
        msg.setType(ChatAdapter.VALUE_RIGHT_TEXT);
        msg.setValue(words);
        msgList.add(msg);
        chatView.setAdapter(new ChatAdapter(getActivity(),msgList));
    }




    //初始化手势识别
    public void initGesture(){
        mSurfaceView = (SurfaceView) view.findViewById(R.id.surface_view);
        mHolder = mSurfaceView.getHolder();
        mHolder.addCallback(this);

        mHandler.postDelayed(timerRunnable,2000);
        mIsTimerRunning = true;
    }
    @Override
    public void onPictureTaken(byte[] data, Camera camera) {
        try {
            FileOutputStream fos = new FileOutputStream(new File
                    (Environment.getExternalStorageDirectory() + File.separator +
                            "hand.png"));

            //旋转角度，保证保存的图片方向是对的
            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
            Matrix matrix = new Matrix();
            matrix.setRotate(270);
            bitmap = Bitmap.createBitmap(bitmap, 0, 0,
                    bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                HandRec handRec = new HandRec();
                String gesture = handRec.getGesture(Bitmap2StrByBase64(decodeUriAsBitmap(Uri.fromFile(new File(Environment.getExternalStorageDirectory(), "hand.png"))),50));
                if(gesture.equals("手掌打开")){
                    musicService.startPlay();
                }else if(gesture.equals("握拳")){
                    musicService.stopPlay();
                }else if(gesture.contains("食指朝上")){
                    musicService.playNextSong();
                }else if(gesture.equals("手掌朝上")){
                    musicService.playPreSong();
                }
            }
        }).start();

        mCamera.startPreview();
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        mIsSurfaceCreated = true;
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        startPreview();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        mIsSurfaceCreated = false;
    }
    private void startPreview() {
        if (mCamera != null || !mIsSurfaceCreated) {
            Log.d(TAG, "startPreview will return");
            return;
        }
        mCamera = Camera.open(FORWORD_CAMERA_ID);
        Camera.Parameters parameters = mCamera.getParameters();
        //自动对焦
        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        parameters.setPreviewFrameRate(20);
        //设置相机预览方向
        mCamera.setDisplayOrientation(270);
        //设置预览分辨率
        parameters.setPreviewSize(1280, 720);
        //设置保存图片的大小
        parameters.setPictureSize(1280,720);
        mCamera.setParameters(parameters);
        try {
            mCamera.setPreviewDisplay(mHolder);
        } catch (Exception e) {
            Log.d(TAG, e.getMessage());
        }

        mCamera.startPreview();
    }
    private void stopPreview() {
        //释放Camera对象
        if (mCamera != null) {
            try {
                mCamera.setPreviewDisplay(null);
                mCamera.stopPreview();
                mCamera.release();
                mCamera = null;
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }
        }
    }
    private Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            if (mCurrentTimer > 0) {
                mCurrentTimer--;
                mHandler.postDelayed(timerRunnable, 1000);
            } else {
                if(mIsTimerRunning){
                    mCamera.takePicture(null, null, null, SiriFragment.this);
                    //mIsTimerRunning = false;
                    mCurrentTimer = 2;
                    mHandler.post(timerRunnable);
                }
            }
        }
    };

    /**
     * 通过Base32将Bitmap转换成Base64字符串
     * @param bit
     * @return
     */
    public String Bitmap2StrByBase64(Bitmap bit,int sharpness){
        ByteArrayOutputStream bos=new ByteArrayOutputStream();
        bit.compress(Bitmap.CompressFormat.JPEG, sharpness, bos);//参数100表示不压缩
        byte[] bytes=bos.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }

    private Bitmap decodeUriAsBitmap(Uri uri) {
        Bitmap bitmap = null;
        try {
            bitmap = BitmapFactory.decodeStream(getActivity().getContentResolver().openInputStream(uri));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
        return bitmap;
    }

}