package com.way.chat.activity;

import java.util.HashMap;
import java.util.Map;

import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
//import android.media.Image;
import android.os.Bundle;
import android.os.Vibrator;
import android.provider.MediaStore.Images;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.mapapi.cloud.CloudListener;
import com.baidu.mapapi.cloud.CloudManager;
import com.baidu.mapapi.cloud.CloudPoiInfo;
import com.baidu.mapapi.cloud.CloudSearchResult;
import com.baidu.mapapi.cloud.DetailSearchResult;
import com.baidu.mapapi.cloud.NearbySearchInfo;
import com.way.chat.common.bean.User;
import com.way.chat.common.tran.bean.TranObject;
import com.way.chat.common.tran.bean.TranObjectType;
import com.way.client.Client;
import com.way.client.ClientOutputThread;
import com.way.util.Constants;
import com.way.util.SharePreferenceUtil;

public class ShakeActy extends MyActivity implements CloudListener
{

	private SharePreferenceUtil util;
	private Button shakeBtn = null;
	private Map<String, Images> strangerImage = new HashMap<String, Images>();
	private MyApplication myApplication = null;
	// 定义sensor管理器
	private SensorManager mSensorManager;
	// 震动
	private Vibrator vibrator;
	// 震动监听器
	private sensorListener sensor;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.shake_acty);
		shakeBtn = (Button) findViewById(R.id.buttonInShakeActy);
		shakeBtn.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				mSensorManager.registerListener(sensor, mSensorManager
						.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
				// 还有SENSOR_DELAY_UI、SENSOR_DELAY_FASTEST、SENSOR_DELAY_GAME等，
				// 根据不同应用，需要的反应速率不同，具体根据实际情况设定
						SensorManager.SENSOR_DELAY_NORMAL);

			}
		});
		myApplication = (MyApplication) this.getApplication();
		util = new SharePreferenceUtil(this, Constants.SAVE_USER);
		CloudManager.getInstance().init(ShakeActy.this);
		NearbySearchInfo info = new NearbySearchInfo();
		// ע�⣺�˴���д��akΪ����˵�ak��������SDK��key����ϸ��Ϣ��鿴LBS�Ƽ����Ĺٷ��ĵ�
		info.ak = Constants.AK;
		info.geoTableId = 96621;
		info.q = "";
		info.radius = 1000;
		String str = ((MyApplication) getApplicationContext()).getLocation()
				.getLongitude()
				+ ","
				+ ((MyApplication) getApplicationContext()).getLocation()
						.getLatitude();
		System.out.println("str:" + str);
		info.location = str;
		CloudManager.getInstance().nearbySearch(info);
		mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		// 震动
		vibrator = (Vibrator) getSystemService(Service.VIBRATOR_SERVICE);
		// 监听器
		sensor = new sensorListener();
		// 加速度传感器
	}

	public void onGetDetailSearchResult(DetailSearchResult result, int error)
	{
		if (result != null)
		{
			if (result.poiInfo != null)
			{
			} else
			{
			}
		} else
		{
		}
	}

	public void onGetSearchResult(CloudSearchResult result, int error)
	{
		if (result != null && result.poiList != null
				&& result.poiList.size() > 0)
		{
			Map<String, BDLocation> stranger = new HashMap<String, BDLocation>();
			for (CloudPoiInfo info : result.poiList)
			{
				System.out.println(info.title + "-->  lat:" + info.longitude
						+ " lon:" + info.latitude);

				if (!info.title.equals(util.getId()))
				{
					BDLocation location = new BDLocation();
					location.setLongitude(info.longitude);
					location.setLatitude(info.latitude);
					stranger.put(info.title, location);

					Client client = myApplication.getClient();
					ClientOutputThread out = client.getClientOutputThread();
					TranObject<User> o = new TranObject<User>(
							TranObjectType.FriendCheck);
					User u = new User();
					u.setId(Integer.parseInt(info.title));
					o.setObject(u);
					out.setMsg(o);
				}

			}
			myApplication.setStranger(stranger);
		}
	}

	@Override
	public void getMessage(TranObject msg)
	{
		if (msg != null)
		{
			switch (msg.getType())
			{
			case FriendCheck:
				User friend = (User) msg.getObject();
				System.out.println(friend);
				if (friend.getId() == 0)
				{
					BitmapFactory.decodeResource(getResources(),
							R.drawable.icon);
				} else
				{
					myApplication.addStrangerImage("" + friend.getId(),
							getStrangerIcon(1));
				}
				break;
			default:
				break;
			}
		}
	}

	private Bitmap getStrangerIcon(int i)
	{
		return BitmapFactory.decodeResource(getResources(), 1);
	}

	private class sensorListener implements SensorEventListener
	{
		@Override
		public void onAccuracyChanged(Sensor arg0, int arg1)
		{
		}

		@Override
		public void onSensorChanged(SensorEvent event)
		{
			int sensorType = event.sensor.getType();
			// values[0]:X轴，values[1]：Y轴，values[2]：Z轴
			float[] values = event.values;
			if (sensorType == Sensor.TYPE_ACCELEROMETER)
			{

				if ((Math.abs(values[0]) > 14 || Math.abs(values[1]) > 14 || Math
						.abs(values[2]) > 14))
				{
					// dosomething
					Toast.makeText(ShakeActy.this, "(*_*)", Toast.LENGTH_SHORT)
							.show();
					// 摇动手机后，再伴随震动提示~~
					vibrator.vibrate(300);
					Intent intent = new Intent();
					intent.setClass(ShakeActy.this, MapActy.class);
					startActivity(intent);
					ShakeActy.this.finish();
				}
			}
		}
	}

	@Override
	protected void onPause()
	{
		mSensorManager.unregisterListener(sensor);
		super.onPause();
	}

	@Override
	protected void onStop()
	{
		mSensorManager.unregisterListener(sensor);
		super.onStop();
	}
}
