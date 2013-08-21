package com.example.androidgps;

import java.util.Iterator;

import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.GpsStatus.Listener;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.provider.Settings;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.location.Criteria;

@SuppressLint("NewApi")
public class MainActivity extends Activity {

	private String toast = "GPS正常可用";
	private String text = "请开启GPS";
	private Button button = null;
	private TextView textView = null;
	private LocationManager locationManager = null;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		textView = (TextView)this.findViewById(R.id.textView);
		button = (Button)findViewById(R.id.button);
		//判断gps是否打开，如果没有则将其打开
		ifGpsCanBeUseOrOpen();
		setGpsAndGetLocation();
		button.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				setGpsAndGetLocation();
			}
		});
	}

	@SuppressLint("ShowToast")
	private void ifGpsCanBeUseOrOpen(){
		LocationManager locationM = (LocationManager)this.getSystemService(Context.LOCATION_SERVICE);
		if (locationM.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)) {
			Toast.makeText(this, toast, Toast.LENGTH_SHORT).show();
			return;
		}
		Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
		//开启gps
		Intent intent = new Intent(Settings.ACTION_SECURITY_SETTINGS);
		startActivityForResult(intent, 0);
	}
	//对GPS的服务进行人工设置，并且获取GPS信息
	private void setGpsAndGetLocation(){
		//查找服务信息
		locationManager = (LocationManager)this.getSystemService(Context.LOCATION_SERVICE);
		//criteria选择合适的地理位置服务
		Criteria criteria = new Criteria();
		//设置高精度
		criteria.setAccuracy(Criteria.ACCURACY_FINE);
		//设置是否要求速度
        criteria.setSpeedRequired(false);
		//是否需要海拔信息
		criteria.setAltitudeRequired(false);
		//是否需要方位信息
		criteria.setBearingRequired(false);
		//是否允许运营商收费
		criteria.setCostAllowed(true);
		//设置对电源的需求
		criteria.setPowerRequirement(Criteria.POWER_HIGH);
		
		//获取GPS信息
		String provider = locationManager.getBestProvider(criteria, true);
		//获取当前位置
		Location location;
		try {
			//如果不设置查询要求，getLastKnownLocation方法传入的参数为LocationManager.GPS_PROVIDER
			location = locationManager.getLastKnownLocation(provider);
			if (location.equals(null)) {
				Log.i("000gps","msg" );
			}
			//将地理位置进行处理，包括显示
			showLocation(location);
			//对GPS设置状态监听器
			locationManager.addGpsStatusListener(new Listener() {
				@Override
				public void onGpsStatusChanged(int event) {
					String TAG = "000==3==";
					switch (event) {
		            //第一次定位
		            case GpsStatus.GPS_EVENT_FIRST_FIX:
		                Log.i(TAG, "第一次定位");
		                break;
		            //卫星状态改变
		            case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
		                Log.i(TAG, "卫星状态改变");
		                //获取当前状态
		                GpsStatus gpsStatus=locationManager.getGpsStatus(null);
		                //获取卫星颗数的默认最大值
		                int maxSatellites = gpsStatus.getMaxSatellites();
		                //创建一个迭代器保存所有卫星 
		                Iterator<GpsSatellite> iters = gpsStatus.getSatellites().iterator();
		                int count = 0;
		                while (iters.hasNext() && count <= maxSatellites) {
		                    GpsSatellite s = iters.next();
		                    count++;
		                }
		                System.out.println("搜索到：" + count + "颗卫星");
		                break;
		            //定位启动
		            case GpsStatus.GPS_EVENT_STARTED:
		                Log.i(TAG, "定位启动");
		                break;
		            //定位结束
		            case GpsStatus.GPS_EVENT_STOPPED:
		                Log.i(TAG, "定位结束");
		                break;
		            }
				}
			});

			//设置GPS监听器，更新时间或者是最小位移
			long minTime = 5000;
			float minDistence = 0;
			//如果同时设置了时间和距离，则以距离变换为基准，此时时间失去作用；距离设为0后以时间为基准 ；同时为0则随时刷新
			locationManager.requestLocationUpdates(provider, minTime, minDistence, new LocationListener() {
				@Override
				public void onStatusChanged(String arg0, int status, Bundle arg2) {
					// GPS的状态变化时，例如在可用、暂时不可用和无服务三个状态直接切换时被调用
					String TAG = "000==2==";
					switch (status) {
		            //GPS状态为可见时
		            case LocationProvider.AVAILABLE:
		                Log.i(TAG, "当前GPS状态为可见状态");
		                break;
		            //GPS状态为服务区外时
		            case LocationProvider.OUT_OF_SERVICE:
		                Log.i(TAG, "当前GPS状态为服务区外状态");
		                break;
		            //GPS状态为暂停服务时
		            case LocationProvider.TEMPORARILY_UNAVAILABLE:
		                Log.i(TAG, "当前GPS状态为暂停服务状态");
		                break;
		            }
				}
				@Override
				public void onProviderEnabled(String provider) {
					// GPS被用户开启后调用
					Location location2 = locationManager.getLastKnownLocation(provider);
					showLocation(location2);
				}
				@Override
				public void onProviderDisabled(String arg0) {
					// 此方法在provider被用户关闭后被调用
					showLocation(null);
				}
				@Override
				public void onLocationChanged(Location location2) {
					//当位置变换后被调用
					showLocation(location2);
					String TAG = "000==1==";
					Log.i(TAG, "时间："+location2.getTime());
		            Log.i(TAG, "经度："+location2.getLongitude());
		            Log.i(TAG, "纬度："+location2.getLatitude());
		            Log.i(TAG, "海拔："+location2.getAltitude());
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	//显示信息
	private void showLocation(Location location){
		if (location != null) {
			double latitude = location.getLatitude();
			double longitude = location.getLongitude();
			String msg = "维度：" + latitude + "\n经度：" + longitude;
			textView.setText(msg);
			Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
			Log.i("000gps",msg );
		}
		else{
			textView.setText("无法获取");
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
}
