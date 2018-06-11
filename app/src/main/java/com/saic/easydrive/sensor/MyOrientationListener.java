package com.saic.easydrive.sensor;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class MyOrientationListener implements SensorEventListener {
	private SensorManager mySensorManager;
	private Sensor accelerometer; // 加速度传感器
	private Sensor magnetic; // 地磁场传感器
	private float[] accelerometerValues = new float[3];
	private float[] magneticFieldValues = new float[3];
	private Context myContext;
	private float lastX;
	private onOrientationListener myOrientationListener;
	public void start(){//开启方向传感器
		//先通过系统服务来得到传感器管理对象mySensorManager
		mySensorManager=(SensorManager) myContext.getSystemService(Context.SENSOR_SERVICE);
		if (mySensorManager!=null) {//如果传感器管理对象不为空，则可以通过传感器管理对象来获得方向传感器对象
			// 初始化加速度传感器
			accelerometer = mySensorManager
					.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
			// 初始化地磁场传感器
			magnetic = mySensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

		}
		if(accelerometer!=null&&magnetic!=null){
			mySensorManager.registerListener(this,
					accelerometer, Sensor.TYPE_ACCELEROMETER);
			mySensorManager.registerListener(this, magnetic,
					Sensor.TYPE_MAGNETIC_FIELD);
		}

	}

	public void stop(){//解除注册方向传感器监听事件
		mySensorManager.unregisterListener(this);
	}

	public MyOrientationListener(Context myContext) {//方向传感器的一个构造器
		super();
		this.myContext = myContext;
	}
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
	}
	//监听方向发生变化
	@Override
	public void onSensorChanged(SensorEvent event) {//精度发生改变的时候
		if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
			accelerometerValues = event.values;
		}
		if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
			magneticFieldValues = event.values;
		}
		float[] values = new float[3];
		float[] R = new float[9];
		SensorManager.getRotationMatrix(R, null, accelerometerValues,
				magneticFieldValues);
		SensorManager.getOrientation(R, values);
		values[0] = (float) Math.toDegrees(values[0]);
		myOrientationListener.onOrientationChanged(lastX);//就需要把上一次的X轴坐标传入，在MainActivity中的回调方法中去获取即可
		lastX=values[0];

	}


	public void setMyOrientationListener(onOrientationListener myOrientationListener) {
		this.myOrientationListener = myOrientationListener;
	}

	/**
	 *写一下这边学到的东西，这个类是一个对象，是为了给主程序调用，然后更新主程序中的值，但是在这里又无法直接调用主程序的方法，所以写一个接口给
	 *主程序实现回调，距离的使用方法就是：1、在这个对象中写一个接口并定义一个方法，方法中的参数用来传值 2、在这个类中定义一个接口对象，并在更新值的方法
	 *中调用接口中的方法从而将值传给这个方法的参数  3、在主程序中定义这样一个接口，并实现接口中的方法  4、在主程序中定义这个类对象并将接口指向这个类中的接口
	 *从而完成绑定，即更新这个类中的值就可以调用这个类的接口，而这个类的接口就是主程序中定义的接口，从而就调用主程序中接口实现的方法，完成回调更新。
	 **/
	//写一个接口实现方向改变的监听产生的回调
	public interface onOrientationListener{
		void onOrientationChanged(float x);//回调的方法
	}
}


