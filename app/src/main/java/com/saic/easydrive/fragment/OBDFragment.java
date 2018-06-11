package com.saic.easydrive.fragment;


import android.app.Activity;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


import com.saic.easydrive.R;
import com.saic.easydrive.activities.MainActivity;
import com.saic.easydrive.obd.CompassView;
import com.saic.easydrive.obd.RoundProgressBar;
import com.saic.easydrive.obd.obdreader.IPostListener;
import com.saic.easydrive.obd.obdreader.MyService;
import com.saic.easydrive.obd.obdreader.ObdCommand;
import com.saic.easydrive.util.DeviceListActivity;


public class OBDFragment extends Fragment {
    //得到OBD数据并可以结合这些数据做一定的分析
    View view;
    private MyService.MyBinder binder;
    private Handler handler;
    private Handler addJobHandler;
    private IPostListener callback;
    private Intent startServiceIntent;
    private String address;
    private ObdCommand command;
    private String message;

    private CompassView speedPointer;
    private RoundProgressBar speedRoundProgressBar;
    private TextView speed_show;
    private CompassView RPMPointer;
    private RoundProgressBar RPMRoundProgressBar;
    private TextView RPMshow;
    private CompassView MAFPointer;
    private RoundProgressBar MAFProgressBar;
    private CompassView MAPPointer;
    private RoundProgressBar MAPProgressBar;
    private CompassView ECTPointer;
    private RoundProgressBar ECTProgressBar;
    private CompassView TPositionPointer;
    private RoundProgressBar TPositionProgressBar;

    private boolean is_startService = false;

    private BluetoothAdapter _bluetooth = BluetoothAdapter.getDefaultAdapter();  //获取本地蓝牙适配器，即蓝牙设备

    private ServiceConnection conn = new ServiceConnection() {
        //当该activity与service连接成功时调用此方法
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            System.out.println("service connected");
            //获取service中返回的Mybind对象
            binder = (MyService.MyBinder)iBinder;

            //更新数据
            Handler updateHandler = new Handler();
            updateHandler.postDelayed(getFigure,500);
        }
        //断开连接时调用此方法
        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            System.out.println("disconnected");
        }
    };
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_obd, container, false);
        initialView();
        if(!is_startService){
            startServiceIntent = new Intent(getActivity(),MyService.class);
            getActivity().bindService(startServiceIntent,conn, Service.BIND_AUTO_CREATE);
            getActivity().startService(startServiceIntent);
            is_startService = true;//防止没连接上每次都要延迟，影响用户体验，删除这一句即可每次都询问连接
        }
        return  view;
    }



    public void initialView(){
        speedPointer = (CompassView)view.findViewById(R.id.speed_compass_pointer);
        speedRoundProgressBar = (RoundProgressBar)view.findViewById(R.id.speed_roundProgressBar1);
        speed_show = (TextView)view.findViewById(R.id.speedshow);
        speedRoundProgressBar.setRoundWidth(30);
        speedPointer.updateDirection(0);

        RPMPointer = (CompassView)view.findViewById(R.id.RPM_compass_pointer);
        RPMRoundProgressBar = (RoundProgressBar)view.findViewById(R.id.RPM_roundProgressBar1);
        RPMshow = (TextView)view.findViewById(R.id.RPM);
        RPMRoundProgressBar.setRoundWidth(30);
        RPMPointer.updateDirection(0);

        MAFPointer = (CompassView)view.findViewById(R.id.MAFCompassView);
        MAFProgressBar = (RoundProgressBar)view.findViewById(R.id.MAFProgressBar);
        MAFProgressBar.setRoundWidth(30);
        MAFPointer.updateDirection(0);

        MAPPointer = (CompassView)view.findViewById(R.id.MAPCompassView);
        MAPProgressBar = (RoundProgressBar)view.findViewById(R.id.MAPProgressBar);
        MAPProgressBar.setRoundWidth(30);
        MAPPointer.updateDirection(0);

        ECTPointer = (CompassView)view.findViewById(R.id.ECTCompassView);
        ECTProgressBar = (RoundProgressBar)view.findViewById(R.id.ECTProgressBar);
        ECTProgressBar.setRoundWidth(30);
        ECTPointer.updateDirection(0);

        TPositionPointer = (CompassView)view.findViewById(R.id.TPositionCompassView);
        TPositionProgressBar = (RoundProgressBar)view.findViewById(R.id.TPositionProgressBar);
        TPositionProgressBar.setRoundWidth(30);
        TPositionPointer.updateDirection(0);

        //如果打开本地蓝牙设备不成功，提示信息，结束程序
        if (_bluetooth == null){
            Toast.makeText(getActivity(), "无法打开手机蓝牙，请确认手机是否有蓝牙功能！", Toast.LENGTH_LONG).show();
            getActivity().finish();
            return;
        }
        //打开蓝牙
        new Thread(){
            public void run(){
                if(_bluetooth.isEnabled()==false){
                    _bluetooth.enable();
                }
            }
        }.start();
        handler = new Handler();

//实现接口中的方法用来回调
        callback = new IPostListener() {
            @Override
            public void stateUpdate(ObdCommand obdCommand,String s) {
                message = s.toString();
                command = obdCommand;
                handler.post(update);
            }

            @Override
            public void toast() {
                Toast.makeText(getActivity(),"连接汽车失败，请确保蓝牙打开以及汽车设备处于开启状态！",Toast.LENGTH_SHORT).show();
                getActivity().unbindService(conn);
            }

            @Override
            public void updateServiceState() {
                is_startService = true;
            }
        };
    }


    Runnable getFigure = new Runnable() {
        @Override
        public void run() {
            binder.setListener(callback);//将service中的接口与这里的接口绑定起来·
            addJobHandler = new Handler();
            addJobHandler.post(addJobRunnable);
        }
    };


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }


    Runnable update = new Runnable() {
        @Override
        public void run() {
            TextView t;
            int index = command.getIndex();
            switch (index){
                case 0:
                    System.out.println("收到数据错误或未收到数据");
                    break;
                case 1:
                    float rpmDirection = (float)(Float.parseFloat(message)*1.5);//通过速度计算度数
                    RPMRoundProgressBar.setMax(18000);
                    RPMPointer.updateDirection(rpmDirection*240/18000);
                    RPMshow.setText(message+"rpm");
                    RPMRoundProgressBar.setProgress(Integer.parseInt(message));
                    break;
                case 2:
                    t = (TextView)view.findViewById(R.id.airFlowTextView);
                    t.setText(message.substring(0,message.length()-6)+"g/s");
                    float MAFDirection = (float)(Float.parseFloat(message.substring(0,message.length()-4))*1.5);//通过速度计算度数
                    MAFPointer.updateDirection(MAFDirection*240/720);
                    MAFProgressBar.setMax(720);
                    MAFProgressBar.setProgress(Integer.parseInt(message.substring(0,message.length()-6)));
                    break;
                case 3:
                    t = (TextView)view.findViewById(R.id.MAPTextView);
                    t.setText(message);
                    float MAPDirection = (float)(Float.parseFloat(message.substring(0,message.length()-3))*1.5);//通过速度计算度数
                    MAPPointer.updateDirection(MAPDirection*240/300);
                    MAPProgressBar.setMax(300);
                    MAPProgressBar.setProgress(Integer.parseInt(message.substring(0,message.length()-3)));
                    break;
                case 4:
                    t = (TextView)view.findViewById(R.id.ECTTextView);
                    t.setText(message.substring(0,message.length()-3)+"℃");
                    int temperature = Integer.parseInt(message.substring(0,message.length()-3));
                    float ECTDirection;
                    ECTProgressBar.setMax(300);
                    if(temperature<0){
                        ECTDirection = (float)((300+temperature)*1.5);
                        ECTPointer.updateDirection(ECTDirection*240/300);
                        ECTProgressBar.setProgress(300+temperature);
                    }else{
                        ECTDirection = (float)(temperature*1.5);//通过速度计算度数
                        ECTPointer.updateDirection(ECTDirection*240/300);
                        ECTProgressBar.setProgress(temperature);
                    }

                    break;
                case 5:
                    float speedDirection = (float)(Float.parseFloat(message)*1.5);//通过速度计算度数
                    speedPointer.updateDirection(speedDirection*240/300);
                    speed_show.setText(message+"km/h");
                    speedRoundProgressBar.setMax(300);
                    speedRoundProgressBar.setProgress(Integer.parseInt(message));
                    break;
                case 6:
                    t = (TextView)view.findViewById(R.id.TPositionTextView);
                    t.setText(message);
                    float TPositionDirection = (float)(Float.parseFloat(message.substring(0,message.length()-1))*1.5);//通过速度计算度数
                    TPositionPointer.updateDirection(TPositionDirection*240/100);
                    TPositionProgressBar.setMax(100);
                    TPositionProgressBar.setProgress(Integer.parseInt(message.substring(0,message.length()-3)));
                    break;
            }
        }
    };


    Runnable addJobRunnable = new Runnable() {
        @Override
        public void run() {
            binder.addJob();
            addJobHandler.postDelayed(this,10);
        }
    };


    @Override
    public void onResume() {
        //更新数据
   /*     if(isFirstTime){
            updateFigure();
            isFirstTime = false;
        }*/
        MainActivity.toolbar.setTitle("汽车数据");
        super.onResume();
    }
}