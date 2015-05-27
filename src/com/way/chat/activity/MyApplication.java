package com.way.chat.activity;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import com.baidu.location.BDLocation;
import com.baidu.mapapi.SDKInitializer;
import com.way.client.Client;
import com.way.util.Constants;
import com.way.util.SharePreferenceUtil;

import android.app.Application;
import android.app.NotificationManager;
import android.graphics.Bitmap;

public class MyApplication extends Application
{
	private Client client;// 客户端
	private boolean isClientStart;// 客户端连接是否启动
	private NotificationManager mNotificationManager;
	private int newMsgNum = 0;// 后台运行的消息
	private LinkedList<RecentChatEntity> mRecentList;
	private RecentChatAdapter mRecentAdapter;
	private int recentNum = 0;

	private BDLocation location = null;
	private Map<String, BDLocation> stranger = null;
	private Map<String, Bitmap> strangerImage = null;

	@Override
	public void onCreate()
	{
		SharePreferenceUtil util = new SharePreferenceUtil(this,
				Constants.SAVE_USER);
		System.out.println(util.getIp() + " " + util.getPort());
		client = new Client(util.getIp(), util.getPort());// 从配置文件中读ip和地址
		mRecentList = new LinkedList<RecentChatEntity>();
		mRecentAdapter = new RecentChatAdapter(getApplicationContext(),
				mRecentList);

		SDKInitializer.initialize(getApplicationContext());

		super.onCreate();
	}

	public BDLocation getLocation()
	{
		return location;
	}

	public void setLocation(BDLocation location)
	{
		this.location = location;
	}

	public Map<String, BDLocation> getStranger()
	{
		return stranger;
	}

	public void setStranger(Map<String, BDLocation> stranger)
	{
		this.stranger = stranger;
	}

	public Client getClient()
	{
		return client;
	}

	public boolean isClientStart()
	{
		return isClientStart;
	}

	public void setClientStart(boolean isClientStart)
	{
		this.isClientStart = isClientStart;
	}

	public NotificationManager getmNotificationManager()
	{
		return mNotificationManager;
	}

	public void setmNotificationManager(NotificationManager mNotificationManager)
	{
		this.mNotificationManager = mNotificationManager;
	}

	public int getNewMsgNum()
	{
		return newMsgNum;
	}

	public void setNewMsgNum(int newMsgNum)
	{
		this.newMsgNum = newMsgNum;
	}

	public LinkedList<RecentChatEntity> getmRecentList()
	{
		return mRecentList;
	}

	public void setmRecentList(LinkedList<RecentChatEntity> mRecentList)
	{
		this.mRecentList = mRecentList;
	}

	public RecentChatAdapter getmRecentAdapter()
	{
		return mRecentAdapter;
	}

	public void setmRecentAdapter(RecentChatAdapter mRecentAdapter)
	{
		this.mRecentAdapter = mRecentAdapter;
	}

	public int getRecentNum()
	{
		return recentNum;
	}

	public void setRecentNum(int recentNum)
	{
		this.recentNum = recentNum;
	}

	public Map<String, Bitmap> getStrangerImage()
	{
		if (this.strangerImage == null)
			this.strangerImage = new HashMap<String, Bitmap>();
		return strangerImage;
	}

	public void setStrangerImage(Map<String, Bitmap> strangerImage)
	{
		this.strangerImage = strangerImage;
	}

	public void addStrangerImage(String ID, Bitmap image)
	{
		getStrangerImage().put(ID, image);
	}
}
