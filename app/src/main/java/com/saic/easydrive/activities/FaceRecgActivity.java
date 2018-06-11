package com.saic.easydrive.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.saic.easydrive.R;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class FaceRecgActivity extends BaseActivity implements SurfaceHolder.Callback, Camera.PictureCallback {
    private SurfaceView mSurfaceView;
    private SurfaceHolder mHolder;
    private Camera mCamera;
    private boolean mIsSurfaceCreated = false;
    private boolean mIsTimerRunning = false;
    Bitmap recgFace = null;

    private static final int BACK_CAMERA_ID = 0; //后置摄像头
    private static final int FORWORD_CAMERA_ID = 1; //前置摄像头

    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 0:
                    Toast.makeText(FaceRecgActivity.this,"认证通过",Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent();
                    intent.putExtra("result",1);
                    setResult(2,intent);
                    finish();
                    break;
                case 1:
                    Toast.makeText(FaceRecgActivity.this,"认证未通过",Toast.LENGTH_SHORT).show();
                    Intent intent2 = new Intent();
                    intent2.putExtra("result",0);
                    setResult(2,intent2);
                    finish();
                    break;
            }
        }
    };

    @Override
    protected View getContentView() {
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                    | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
            window.setNavigationBarColor(Color.TRANSPARENT);
        }
        return inflateView(R.layout.activity_face);
    }

    @Override
    protected void setContentViewAfter(View contentView) {
        initView();
        Handler picHandler = new Handler();
        picHandler.postDelayed(takepicture,2000);
    }


    public void initView(){
        mSurfaceView = (SurfaceView) findViewById(R.id.faceView);
        mHolder = mSurfaceView.getHolder();
        mHolder.addCallback(this);
    }

    Runnable takepicture = new Runnable() {
        @Override
        public void run() {
            mCamera.takePicture(null, null, null, FaceRecgActivity.this);
        }
    };

    //想阿里API发送请求，将拍摄的bitmap与存储的bitmap进行对比
    private void sendRequestWithHttpURLConnection(final Bitmap bitmap){
        //开启线程来发起网络请求
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection connection = null;
                BufferedReader reader = null;
                try {
                    URL url = new URL("http://rlsbbd.market.alicloudapi.com/face/verify");
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setConnectTimeout(8000);
                    connection.setReadTimeout(8000);
                    connection.setRequestProperty("Content-Type", "application/json");
                    connection.setDoOutput(true);
                    //用setRequestProperty方法设置一个自定义的请求头:action，由于后端判断
                    connection.setRequestProperty("Authorization", "APPCODE 62a3baabd33d4e45bd76a82ae6b99d1b");
                    //获取conn的输出流
                    OutputStream os = connection.getOutputStream();
                    //读取assets目录下的person.xml文件，将其字节数组作为请求体
                    byte[] requestBody = ("{\"type\":1,"
                            + "\"content_1\":\""+bitmapToBase64(BitmapFactory.decodeResource(getResources(),R.mipmap.myself))+"\","
                            +"\"content_2\":\""+bitmapToBase64(bitmap)+"\"}").getBytes();
                    //将请求体写入到conn的输出流中
                    os.write(requestBody);
                    //记得调用输出流的flush方法
                    os.flush();
                    //关闭输出流
                    os.close();
                    //当调用getInputStream方法时才真正将请求体数据上传至服务器
                    InputStream in = connection.getInputStream();
                    //下面对获取到的输入流进行读取
                    reader = new BufferedReader(new InputStreamReader(in));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null){
                        response.append(line);
                    }
                    System.out.println(response.toString());
                    JSONObject jsonObject = new JSONObject(response.toString());
                    double confidence = jsonObject.getDouble("confidence");
                    if(confidence>60){
                        handler.sendEmptyMessage(0);
                    }else{
                        handler.sendEmptyMessage(1);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
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
        }).start();
    }


    public static String bitmapToBase64(Bitmap bitmap) {
        String result = null;
        ByteArrayOutputStream baos = null;
        try {
            if (bitmap != null) {
                baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                baos.flush();
                baos.close();
                byte[] bitmapBytes = baos.toByteArray();
                result = Base64.encodeToString(bitmapBytes, Base64.DEFAULT);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (baos != null) {
                    baos.flush();
                    baos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    @Override
    public void onPictureTaken(byte[] data, Camera camera) {
        try {
            FileOutputStream fos = new FileOutputStream(new File
                    (Environment.getExternalStorageDirectory() + File.separator +
                            "face.png"));

            //旋转角度，保证保存的图片方向是对的
            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
            Matrix matrix = new Matrix();
            matrix.setRotate(90);
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
        recgFace = decodeUriAsBitmap(Uri.fromFile(new File(Environment.getExternalStorageDirectory(), "face.png")));
        mCamera.startPreview();
        sendRequestWithHttpURLConnection(recgFace);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mIsSurfaceCreated = true;
    }
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        startPreview();
    }
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mIsSurfaceCreated = false;
    }
    private void startPreview() {
        if (mCamera != null || !mIsSurfaceCreated) {
            Log.d("skydianshi", "startPreview will return");
            return;
        }
        mCamera = Camera.open(FORWORD_CAMERA_ID);
        Camera.Parameters parameters = mCamera.getParameters();
        //自动对焦
        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        parameters.setPreviewFrameRate(20);
        //设置相机预览方向
        mCamera.setDisplayOrientation(90);
        //设置预览分辨率
        parameters.setPreviewSize(1280, 720);
        //设置保存图片的大小
        parameters.setPictureSize(1280,720);
        mCamera.setParameters(parameters);
        try {
            mCamera.setPreviewDisplay(mHolder);
        } catch (Exception e) {
            Log.d("skydianshi", e.getMessage());
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
                Log.e("skydianshi", e.getMessage());
            }
        }
    }

    private Bitmap decodeUriAsBitmap(Uri uri) {
        Bitmap bitmap = null;
        try {
            bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(uri));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
        return bitmap;
    }

    @Override
    public void onPause() {
        super.onPause();
        stopPreview();
    }

}