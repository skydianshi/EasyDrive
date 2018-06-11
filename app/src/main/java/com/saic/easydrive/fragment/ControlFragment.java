package com.saic.easydrive.fragment;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.saic.easydrive.R;
import com.saic.easydrive.activities.FaceRecgActivity;
import com.saic.easydrive.activities.MainActivity;


public class ControlFragment extends Fragment implements View.OnClickListener{

    private ImageView img_lock_left;
    private ImageView img_lock_right;
    private ImageView img_confirm;
    private ImageView img_fire;
    private ImageView img_wiper;
    private ImageView img_window;
    private ImageView img_light;
    private TextView tv_remind;

    private View view;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_control, null);
        initView();
        return view;
    }

    public void initView(){
        img_lock_left = (ImageView)view.findViewById(R.id.img_lock_left);
        img_lock_right = (ImageView)view.findViewById(R.id.img_lock_right);
        img_confirm = (ImageView)view.findViewById(R.id.img_confirm);
        img_fire = (ImageView)view.findViewById(R.id.img_firecontrol);
        img_wiper = (ImageView)view.findViewById(R.id.img_wipercontrol);
        img_window = (ImageView)view.findViewById(R.id.img_windowcontrol);
        img_light = (ImageView)view.findViewById(R.id.img_lightcontrol);
        tv_remind = (TextView) view.findViewById(R.id.tv_remind);
        img_confirm.setOnClickListener(this);
        img_fire.setOnClickListener(this);
        img_wiper.setOnClickListener(this);
        img_window.setOnClickListener(this);
        img_light.setOnClickListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onResume() {
        MainActivity.toolbar.setTitle("汽车控制");
        super.onResume();
    }

    boolean fire = false;
    boolean window = false;
    boolean wiper = false;
    boolean light = false;
    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.img_confirm:
                Intent faceIntent = new Intent(getActivity(), FaceRecgActivity.class);
                startActivityForResult(faceIntent,1);
                break;
            case R.id.img_firecontrol:
                if(is_confirm){
                    if(fire){
                        img_fire.setImageBitmap(BitmapFactory.decodeResource(getResources(),R.mipmap.fire_off));
                        fire = false;
                    }else{
                        img_fire.setImageBitmap(BitmapFactory.decodeResource(getResources(),R.mipmap.fire_on));
                        fire = true;
                    }
                }else{
                    Toast.makeText(getActivity(),"请先验证身份后再进行操作！",Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.img_lightcontrol:
                if(is_confirm){
                    if(light){
                        img_light.setImageBitmap(BitmapFactory.decodeResource(getResources(),R.mipmap.carlight_off));
                        light = false;
                    }else{
                        img_light.setImageBitmap(BitmapFactory.decodeResource(getResources(),R.mipmap.carlight_on));
                        light = true;
                    }
                }else{
                    Toast.makeText(getActivity(),"请先验证身份后再进行操作！",Toast.LENGTH_SHORT).show();
                }

                break;
            case R.id.img_windowcontrol:
                if(is_confirm){
                    if(window){
                        img_window.setImageBitmap(BitmapFactory.decodeResource(getResources(),R.mipmap.carwindow_off));
                        window = false;
                    }else{
                        img_window.setImageBitmap(BitmapFactory.decodeResource(getResources(),R.mipmap.carwindow_on));
                        window = true;
                    }
                }else{

                }
                break;
            case R.id.img_wipercontrol:
                if(is_confirm){
                    if(wiper){
                        img_wiper.setImageBitmap(BitmapFactory.decodeResource(getResources(),R.mipmap.roundmenu1_off));
                        wiper = false;
                    }else{
                        img_wiper.setImageBitmap(BitmapFactory.decodeResource(getResources(),R.mipmap.roundmenu1_on));
                        wiper = true;
                    }
                }else{
                    Toast.makeText(getActivity(),"请先验证身份后再进行操作！",Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    boolean is_confirm = false;//验证用户
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case 1:
                try {
                    int result = data.getIntExtra("result",-1);
                    if(result == 1){
                        is_confirm = true;
                        img_lock_left.setImageBitmap(BitmapFactory.decodeResource(getResources(),R.mipmap.lock_on));
                        img_lock_right.setImageBitmap(BitmapFactory.decodeResource(getResources(),R.mipmap.lock_on));
                        tv_remind.setText("已验证身份，可以进行控制操作");
                    }else{
                        is_confirm = false;
                        img_lock_left.setImageBitmap(BitmapFactory.decodeResource(getResources(),R.mipmap.lock_off));
                        img_lock_right.setImageBitmap(BitmapFactory.decodeResource(getResources(),R.mipmap.lock_off));
                        Toast.makeText(getActivity(),"验证未通过，请确认身份或重试",Toast.LENGTH_SHORT).show();
                    }
                }catch (Exception e){
                    is_confirm = false;
                }
                break;
        }
    }
}