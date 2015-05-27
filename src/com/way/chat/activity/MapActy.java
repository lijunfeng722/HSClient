package com.way.chat.activity;

import java.util.Iterator;
import java.util.Map;

import com.baidu.location.BDLocation;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BaiduMap.OnMarkerClickListener;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.way.chat.activity.R;
import com.way.util.Constants;
import com.way.util.SharePreferenceUtil;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class MapActy extends Activity
{
	private SharePreferenceUtil util;
	private MapView mMapView;
	private BaiduMap mBaiduMap;
	private Marker mMarker = null;
	private MyApplication application = null;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.map_acty);
		mMapView = (MapView) findViewById(R.id.bmapViewInActitvityMap);
		mBaiduMap = mMapView.getMap();
		mBaiduMap.setMyLocationEnabled(true);
		mBaiduMap.setMaxAndMinZoomLevel(18, 14);
		application = (MyApplication) getApplication();
		BDLocation location = ((MyApplication) getApplication()).getLocation();
		LatLng ll = new LatLng(location.getLatitude(), location.getLongitude());
		MapStatusUpdate u = MapStatusUpdateFactory.newLatLngZoom(ll, 14);
		mBaiduMap.animateMapStatus(u);

		util = new SharePreferenceUtil(this, Constants.SAVE_USER);
		// set Loader's Icon
		BitmapDescriptor bd = BitmapDescriptorFactory
				.fromBitmap(getLoaderIcon());

		OverlayOptions oo1 = new MarkerOptions().position(ll).icon(bd)
				.zIndex(5);
		mMarker = (Marker) (mBaiduMap.addOverlay(oo1));

		new ShowStrangerThread().start();

		mBaiduMap.setOnMarkerClickListener(new OnMarkerClickListener()
		{

			@Override
			public boolean onMarkerClick(Marker marker)
			{
				LatLng latlng = marker.getPosition();
				String text = null;
				if (marker == mMarker)
				{
					text = "我";
					text += "经度" + latlng.longitude + "纬度 " + latlng.latitude;
					Toast.makeText(MapActy.this, text, Toast.LENGTH_SHORT)
							.show();
				} else
				{
					text = marker.getTitle();
					text += "经度" + latlng.longitude + "纬度 " + latlng.latitude;
					Intent intent = new Intent();
					intent.putExtra("ID", marker.getTitle());
					intent.setClass(MapActy.this, PinTuActy.class);
					startActivity(intent);
					MapActy.this.finish();
				}
				return true;
			}
		});

	}

	private Bitmap getLoaderIcon()
	{
		return BitmapFactory.decodeResource(getResources(), R.drawable.locate_icon);
	}

	private Bitmap getStrangerIcon(String id)
	{
		return BitmapFactory.decodeResource(getResources(), R.drawable.locate_icon);
	}

	@Override
	protected void onPause()
	{
		super.onPause();
		mMapView.onPause();
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		mMapView.onResume();
	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		// activity ���ʱͬʱ��ٵ�ͼ�ؼ�
		mMapView.onDestroy();
	}

	private class ShowStrangerThread extends Thread
	{
		@Override
		public void run()
		{
			Iterator<Map.Entry<String, BDLocation>> it = ((MyApplication) getApplicationContext())
					.getStranger().entrySet().iterator();
			while (it.hasNext())
			{

				Map.Entry<String, BDLocation> tStranger = it.next();
				System.out.println("Stranger :" + tStranger.getKey());
				BDLocation loc = tStranger.getValue();
				LatLng tL = new LatLng(loc.getLatitude(), loc.getLongitude());
				BitmapDescriptor bdOfStranger = BitmapDescriptorFactory
						.fromBitmap(getStrangerIcon(tStranger.getKey()));
				OverlayOptions oo = new MarkerOptions().position(tL)
						.icon(bdOfStranger).zIndex(5);
				Marker tMarker = (Marker) (mBaiduMap.addOverlay(oo));
				tMarker.setTitle(tStranger.getKey());
				// markerList.add(tMarker);
			}
			super.run();
		}
	}
}