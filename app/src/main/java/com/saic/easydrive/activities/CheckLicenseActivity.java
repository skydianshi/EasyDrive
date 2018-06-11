package com.saic.easydrive.activities;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.saic.easydrive.R;
import com.saic.easydrive.ui.MyToolbar;
import com.saic.easydrive.util.GetRequestUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class CheckLicenseActivity extends BaseActivity {

    EditText et_licenseNum;
    EditText et_number;
    Button button;
    TextView tv_result;
    String score;
    MyToolbar tb;

    Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    tv_result.setText("您当前已扣" + score + "分");
                    break;
                case 1:
            }
        }
    };

    @Override
    protected View getContentView() {
        return inflateView(R.layout.activity_check_license);
    }

    @Override
    protected void setContentViewAfter(View contentView) {
        initView();
    }


    public void initView(){
        et_licenseNum = (EditText)findViewById(R.id.licenseNum);
        et_number = (EditText)findViewById(R.id.number);
        button = (Button)findViewById(R.id.button);
        tv_result = (TextView)findViewById(R.id.result);
        tb = (MyToolbar)findViewById(R.id.toolbar);
        tb.setTitleTextColor(getResources().getColor(R.color.white));
        tb.setTitle("驾驶证查询");

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Map<String,String> querys = new HashMap<>();
                        querys.put("licenseid",et_licenseNum.getText().toString());
                        querys.put("licensenumber",et_number.getText().toString());
                        String result = GetRequestUtil.doGet("http://jisujszkf.market.alicloudapi.com/driverlicense/query","62a3baabd33d4e45bd76a82ae6b99d1b",querys);
                        try {
                            JSONObject jsonObject = new JSONObject(result);
                            JSONObject resultObject = jsonObject.getJSONObject("result");
                            score = resultObject.getString("score");
                            handler.sendEmptyMessage(0);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();

            }
        });
    }
}
