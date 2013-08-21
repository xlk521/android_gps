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

	private String toast = "GPS��������";
	private String text = "�뿪��GPS";
	private Button button = null;
	private TextView textView = null;
	private LocationManager locationManager = null;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		textView = (TextView)this.findViewById(R.id.textView);
		button = (Button)findViewById(R.id.button);
		//�ж�gps�Ƿ�򿪣����û�������
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
		//����gps
		Intent intent = new Intent(Settings.ACTION_SECURITY_SETTINGS);
		startActivityForResult(intent, 0);
	}
	//��GPS�ķ�������˹����ã����һ�ȡGPS��Ϣ
	private void setGpsAndGetLocation(){
		//���ҷ�����Ϣ
		locationManager = (LocationManager)this.getSystemService(Context.LOCATION_SERVICE);
		//criteriaѡ����ʵĵ���λ�÷���
		Criteria criteria = new Criteria();
		//���ø߾���
		criteria.setAccuracy(Criteria.ACCURACY_FINE);
		//�����Ƿ�Ҫ���ٶ�
        criteria.setSpeedRequired(false);
		//�Ƿ���Ҫ������Ϣ
		criteria.setAltitudeRequired(false);
		//�Ƿ���Ҫ��λ��Ϣ
		criteria.setBearingRequired(false);
		//�Ƿ�������Ӫ���շ�
		criteria.setCostAllowed(true);
		//���öԵ�Դ������
		criteria.setPowerRequirement(Criteria.POWER_HIGH);
		
		//��ȡGPS��Ϣ
		String provider = locationManager.getBestProvider(criteria, true);
		//��ȡ��ǰλ��
		Location location;
		try {
			//��������ò�ѯҪ��getLastKnownLocation��������Ĳ���ΪLocationManager.GPS_PROVIDER
			location = locationManager.getLastKnownLocation(provider);
			if (location.equals(null)) {
				Log.i("000gps","msg" );
			}
			//������λ�ý��д���������ʾ
			showLocation(location);
			//��GPS����״̬������
			locationManager.addGpsStatusListener(new Listener() {
				@Override
				public void onGpsStatusChanged(int event) {
					String TAG = "000==3==";
					switch (event) {
		            //��һ�ζ�λ
		            case GpsStatus.GPS_EVENT_FIRST_FIX:
		                Log.i(TAG, "��һ�ζ�λ");
		                break;
		            //����״̬�ı�
		            case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
		                Log.i(TAG, "����״̬�ı�");
		                //��ȡ��ǰ״̬
		                GpsStatus gpsStatus=locationManager.getGpsStatus(null);
		                //��ȡ���ǿ�����Ĭ�����ֵ
		                int maxSatellites = gpsStatus.getMaxSatellites();
		                //����һ�������������������� 
		                Iterator<GpsSatellite> iters = gpsStatus.getSatellites().iterator();
		                int count = 0;
		                while (iters.hasNext() && count <= maxSatellites) {
		                    GpsSatellite s = iters.next();
		                    count++;
		                }
		                System.out.println("��������" + count + "������");
		                break;
		            //��λ����
		            case GpsStatus.GPS_EVENT_STARTED:
		                Log.i(TAG, "��λ����");
		                break;
		            //��λ����
		            case GpsStatus.GPS_EVENT_STOPPED:
		                Log.i(TAG, "��λ����");
		                break;
		            }
				}
			});

			//����GPS������������ʱ���������Сλ��
			long minTime = 5000;
			float minDistence = 0;
			//���ͬʱ������ʱ��;��룬���Ծ���任Ϊ��׼����ʱʱ��ʧȥ���ã�������Ϊ0����ʱ��Ϊ��׼ ��ͬʱΪ0����ʱˢ��
			locationManager.requestLocationUpdates(provider, minTime, minDistence, new LocationListener() {
				@Override
				public void onStatusChanged(String arg0, int status, Bundle arg2) {
					// GPS��״̬�仯ʱ�������ڿ��á���ʱ�����ú��޷�������״ֱ̬���л�ʱ������
					String TAG = "000==2==";
					switch (status) {
		            //GPS״̬Ϊ�ɼ�ʱ
		            case LocationProvider.AVAILABLE:
		                Log.i(TAG, "��ǰGPS״̬Ϊ�ɼ�״̬");
		                break;
		            //GPS״̬Ϊ��������ʱ
		            case LocationProvider.OUT_OF_SERVICE:
		                Log.i(TAG, "��ǰGPS״̬Ϊ��������״̬");
		                break;
		            //GPS״̬Ϊ��ͣ����ʱ
		            case LocationProvider.TEMPORARILY_UNAVAILABLE:
		                Log.i(TAG, "��ǰGPS״̬Ϊ��ͣ����״̬");
		                break;
		            }
				}
				@Override
				public void onProviderEnabled(String provider) {
					// GPS���û����������
					Location location2 = locationManager.getLastKnownLocation(provider);
					showLocation(location2);
				}
				@Override
				public void onProviderDisabled(String arg0) {
					// �˷�����provider���û��رպ󱻵���
					showLocation(null);
				}
				@Override
				public void onLocationChanged(Location location2) {
					//��λ�ñ任�󱻵���
					showLocation(location2);
					String TAG = "000==1==";
					Log.i(TAG, "ʱ�䣺"+location2.getTime());
		            Log.i(TAG, "���ȣ�"+location2.getLongitude());
		            Log.i(TAG, "γ�ȣ�"+location2.getLatitude());
		            Log.i(TAG, "���Σ�"+location2.getAltitude());
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	//��ʾ��Ϣ
	private void showLocation(Location location){
		if (location != null) {
			double latitude = location.getLatitude();
			double longitude = location.getLongitude();
			String msg = "ά�ȣ�" + latitude + "\n���ȣ�" + longitude;
			textView.setText(msg);
			Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
			Log.i("000gps",msg );
		}
		else{
			textView.setText("�޷���ȡ");
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
}
