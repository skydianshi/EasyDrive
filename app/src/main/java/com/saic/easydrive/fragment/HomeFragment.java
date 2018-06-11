package com.saic.easydrive.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.saic.easydrive.R;
import com.saic.easydrive.activities.CheckAgainstActivity;
import com.saic.easydrive.activities.CheckLicenseActivity;
import com.saic.easydrive.activities.MainActivity;
import com.saic.easydrive.util.GetRequestUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


public class HomeFragment extends Fragment implements View.OnClickListener{
	View view;

	private TextView tv_city;
	private TextView tv_date;
	private TextView tv_temp;
	private ImageView image_weather;
	private TextView tv_weather;
	private TextView tv_feel;
	private TextView tv_temp_feel;
	private TextView tv_humidity;
	private TextView tv_rays;
	private TextView tv_winddirect;
	private TextView tv_windLevel;
	private TextView tv_pm;
	private TextView tv_airCondition;
	private TextView tv_washCar;
	private TextView tv_limitCar;
	private TextView tv_92Price;
	private TextView tv_95Price;
	private LinearLayout layout_checkAgainst;
	private LinearLayout layout_checkLicense;

	String city;
	String temphigh;
	String tempnow;
	String sendibletemp;
	String winddirect;
	String windpower;
	String humidity;
	String weather;
	String weekday;
	String date;
	String pm25;
	String airQuality;
	String rayLevel;
	String washLevel;
	String feeling;
	String price_92;
	String price_95;

	Handler handler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what){
				case 0:
					tv_city.setText(city);
					tv_airCondition.setText(airQuality);
					tv_washCar.setText(washLevel);
					tv_date.setText(date+"    "+weekday);
					tv_humidity.setText(humidity);
					tv_pm.setText(pm25);
					tv_rays.setText(rayLevel);
					tv_temp_feel.setText(sendibletemp+"℃");
					tv_temp.setText(tempnow+"℃");
					tv_weather.setText(weather);
					tv_windLevel.setText(windpower);
					tv_winddirect.setText(winddirect);
					tv_feel.setText(feeling);
					break;
				case 1:
					tv_92Price.setText(price_92);
					tv_95Price.setText(price_95);
			}
		}
	};
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		view = inflater.inflate(R.layout.fragment_park, container, false);

		initView();
		new Thread(new Runnable() {
			@Override
			public void run() {
				updateView();
			}
		}).start();
		return view;
	}

	public void initView(){
		tv_city = (TextView)view.findViewById(R.id.tv_city);
		tv_date = (TextView)view.findViewById(R.id.tv_date);
		tv_temp = (TextView)view.findViewById(R.id.tv_temp);
		image_weather = (ImageView)view.findViewById(R.id.image_weather);
		tv_weather = (TextView)view.findViewById(R.id.tv_weather);
		tv_feel = (TextView)view.findViewById(R.id.tv_feeling);
		tv_temp_feel = (TextView)view.findViewById(R.id.tv_temp_feel);
		tv_humidity = (TextView)view.findViewById(R.id.tv_humidity);
		tv_rays = (TextView)view.findViewById(R.id.tv_rays);
		tv_winddirect = (TextView)view.findViewById(R.id.winddirect);
		tv_windLevel = (TextView)view.findViewById(R.id.tv_windLevel);
		tv_pm = (TextView)view.findViewById(R.id.tv_pm);
		tv_airCondition = (TextView)view.findViewById(R.id.tv_airCondition);
		tv_washCar = (TextView)view.findViewById(R.id.tv_washCar);
		tv_limitCar = (TextView)view.findViewById(R.id.tv_limitCar);
		tv_92Price = (TextView)view.findViewById(R.id.tv_92price);
		tv_95Price = (TextView)view.findViewById(R.id.tv_95price);
		layout_checkAgainst = (LinearLayout)view.findViewById(R.id.check_against);
		layout_checkLicense = (LinearLayout)view.findViewById(R.id.check_license);
		layout_checkLicense.setOnClickListener(this);
		layout_checkAgainst.setOnClickListener(this);
	}

	Map<String,String> querys;
	String result;
	public void updateView(){
		querys = new HashMap<>();
		querys.put("city","重庆");
		result = GetRequestUtil.doGet("http://chkj02.market.alicloudapi.com/qgtq","62a3baabd33d4e45bd76a82ae6b99d1b",querys);
		try {
			JSONObject jsonObject = new JSONObject(result);
			JSONObject dataObject = jsonObject.getJSONObject("data");
			city = dataObject.getString("city");
			temphigh = dataObject.getString("temphigh");
			tempnow = dataObject.getString("tempnow");
			sendibletemp = dataObject.getString("sendibletemp");
			winddirect = dataObject.getString("winddirect");
			windpower = dataObject.getString("windpower");
			humidity = dataObject.getString("humidity");
			weather = dataObject.getString("weather");
			weekday = dataObject.getString("week");
			date = dataObject.getString("date");
			JSONObject pm25Object = dataObject.getJSONObject("pm25");
			pm25 = pm25Object.getString("pm2_5");
			airQuality = pm25Object.getString("quality");
			JSONArray jsonArray = dataObject.getJSONArray("index");
			JSONObject washCarObject = jsonArray.getJSONObject(3);
			JSONObject raysObject = jsonArray.getJSONObject(5);
			JSONObject feelingObject = jsonArray.getJSONObject(4);
			rayLevel = raysObject.getString("level");
			washLevel = washCarObject.getString("level");
			feeling = feelingObject.getString("msg");
			handler.sendEmptyMessage(0);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		querys = new HashMap<>();
		querys.put("prov","重庆");
		result = GetRequestUtil.doGet("http://ali-todayoil.showapi.com/todayoil","62a3baabd33d4e45bd76a82ae6b99d1b",querys);
		try {
			JSONObject oilObject = new JSONObject(result);
			JSONObject bodyObject = oilObject.getJSONObject("showapi_res_body");
			JSONArray oilArray = bodyObject.getJSONArray("list");
			JSONObject listObject = oilArray.getJSONObject(0);
			price_92 = listObject.getString("p92");
			price_95 = listObject.getString("p95");
			handler.sendEmptyMessage(1);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()){
			case R.id.check_against:
				Intent intent1 = new Intent(getActivity(),CheckAgainstActivity.class);
				startActivity(intent1);
				break;
			case R.id.check_license:
				Intent intent2 = new Intent(getActivity(),CheckLicenseActivity.class);
				startActivity(intent2);
				break;
		}

	}





	@Override
	public void onResume() {
		MainActivity.toolbar.setTitle("Easy Drive");
		super.onResume();
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void onStart() {
		super.onStart();
	}

	@Override
	public void onStop() {
		super.onStop();
	}
}