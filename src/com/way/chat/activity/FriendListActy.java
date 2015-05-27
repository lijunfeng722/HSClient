package com.way.chat.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;
import com.supermario.POI.CreatePoi;
import com.supermario.POI.ListPoi;
import com.supermario.POI.UnicodeConvert;
import com.supermario.POI.UpdatePoi;
import com.way.chat.common.bean.TextMessage;
import com.way.chat.common.bean.User;
import com.way.chat.common.tran.bean.TranObject;
import com.way.chat.common.tran.bean.TranObjectType;
import com.way.client.ClientInputThread;
import com.way.client.ClientOutputThread;
import com.way.client.MessageListener;
import com.way.util.BitmapUtil;
import com.way.util.Constants;
import com.way.util.MessageDB;
import com.way.util.MyDate;
import com.way.util.SharePreferenceUtil;
import com.way.util.UserDB;

/**
 * 好友列表的Activity
 * 
 * @author way
 * 
 */
public class FriendListActy extends MyActivity implements OnClickListener
{

	private static final int PAGE1 = 0;// 页面1
	private static final int PAGE2 = 1;// 页面2
	// private List<GroupFriend> group;// 需要传递给适配器的数据
	private SharePreferenceUtil util;
	private UserDB userDB;// 保存好友列表数据库对象
	private MessageDB messageDB;// 消息数据库对象

	// private MyListView myListView;// 好友列表自定义listView
	// private MyExAdapter myExAdapter;// 好
	private FriendListAdapter myExAdapter;// 好

	private ListView mRecentListView;// 最近会话的listView
	private int newNum = 0;

	private ViewPager mPager;
	private List<View> mListViews;// Tab页面
	private LinearLayout layout_body_activity;
	private ImageView img_recent_chat;// 最近会话
	private ImageView img_friend_list;// 好友列表

	private ImageButton myHeadImage;// 头像
	private TextView myName;// 名字

	private ImageView cursor;// 标题背景图片

	private int currentIndex = PAGE2; // 默认选中第2个，可以动态的改变此参数值
	private int offset = 0;// 动画图片偏移量
	private int bmpW;// 动画图片宽度

	private TranObject msg;
	private List<User> list;
	private MenuInflater mi;// 菜单
	/*
	 * private int[] imgs = { R.drawable.icon, R.drawable.f1, R.drawable.f2,
	 * R.drawable.f3, R.drawable.f4, R.drawable.f5, R.drawable.f6,
	 * R.drawable.f7, R.drawable.f8, R.drawable.f9 };// 头像资源
	 */
	Bitmap imgs;
	private MyApplication application;

	private LocationClient mLocationClient = null;
	private BDLocationListener myListener = null;
	Handler handler = null;
	private final int list_request = 0;
	private final int poi_create_request = 1;
	private final int poi_update_request = 2;
	private ListView listVw = null;

	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);// 去掉标题栏
		setContentView(R.layout.friendlist_acty);
		application = (MyApplication) this.getApplicationContext();
		initData();// 初始化数据
		initImageView();// 初始化动画
		initUI();// 初始化界面

		mLocationClient = new LocationClient(getApplicationContext());
		myListener = new MyLocationListener();
		mLocationClient.registerLocationListener(myListener);
		LocationClientOption option = new LocationClientOption();
		option.setLocationMode(LocationMode.Hight_Accuracy);// 设置定位模式
		option.setCoorType("bd09ll");// 返回的定位结果是百度经纬度,默认值gcj02
		option.setScanSpan(1000 * 60);// 设置发起定位请求的间隔时间为60s
		option.setIsNeedAddress(true);// 返回的定位结果包含地址信息
		mLocationClient.setLocOption(option);
		new MyThread(mLocationClient).start();

	}

	@Override
	protected void onResume()
	{// 如果从后台恢复，服务被系统干掉，就重启一下服务
		newNum = application.getRecentNum();// 从新获取一下全局变量
		if (!application.isClientStart())
		{
			Intent service = new Intent(this, GetMsgService.class);
			startService(service);
		}
		new SharePreferenceUtil(this, Constants.SAVE_USER).setIsStart(false);
		NotificationManager manager = application.getmNotificationManager();
		if (manager != null)
		{
			manager.cancel(Constants.NOTIFY_ID);
			application.setNewMsgNum(0);// 把消息数目置0
			application.getmRecentAdapter().notifyDataSetChanged();
		}
		super.onResume();
	}

	/**
	 * 初始化系统数据
	 */
	private void initData()
	{
		userDB = new UserDB(FriendListActy.this);// 本地用户数据库
		messageDB = new MessageDB(this);// 本地消息数据库
		util = new SharePreferenceUtil(this, Constants.SAVE_USER);

		Intent it = getIntent();
		
		if (it.getStringExtra("From")==null)
		{// 如果为空，说明是从后台切换过来的，需要从数据库中读取好友列表信息
			list = userDB.getUser();
		} else if ("StrangerMsg".equals(it.getStringExtra("From")))
		{
			User newfriend = (User)it.getSerializableExtra("newFriend");
			list = userDB.getUser();
			list.add(newfriend);
			userDB.updateUser(list);
		} else if ("LoginActy".equals(it.getStringExtra("From")))
		{// 如果是登录界面切换过来的，就把好友列表信息保存到数据库
			msg = (TranObject)  it.getSerializableExtra(Constants.MSGKEY);// 从intent中取出消息对象
			list = (List<User>) msg.getObject();
			userDB.updateUser(list);
		}

	}

	/**
	 * 初始化动画
	 */
	private void initImageView()
	{
		cursor = (ImageView) findViewById(R.id.tab_bg);
		bmpW = BitmapFactory.decodeResource(getResources(),
				R.drawable.topbar_select).getWidth();// 获取图片宽度
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		int screenW = dm.widthPixels;// 获取分辨率宽度
		// System.out.println("屏幕宽度:" + screenW);
		offset = (screenW / 2 - bmpW) / 2;// 计算偏移量:屏幕宽度/3，平分为3分，如果是3个view的话，再减去图片宽度，因为图片居中，所以要得到两变剩下的空隙需要再除以2
		Matrix matrix = new Matrix();
		matrix.postTranslate(screenW / 2 + bmpW, 0);// 初始化位置
		cursor.setImageMatrix(matrix);// 设置动画初始位置
	}

	private void initUI()
	{
		mi = new MenuInflater(this);
		layout_body_activity = (LinearLayout) findViewById(R.id.bodylayout);

		img_recent_chat = (ImageView) findViewById(R.id.tab1);
		img_recent_chat.setOnClickListener(this);
		img_friend_list = (ImageView) findViewById(R.id.tab2);
		img_friend_list.setOnClickListener(this);

		myHeadImage = (ImageButton) findViewById(R.id.friend_list_myImg);
		myName = (TextView) findViewById(R.id.friend_list_myName);

		cursor = (ImageView) findViewById(R.id.tab_bg);

		imgs = BitmapUtil.toRoundCorner( list.get(0).getImg(), 2);
		Drawable drawable = new BitmapDrawable(imgs);
		myHeadImage.setImageDrawable(drawable);

		myName.setText(list.get(0).getName());
		layout_body_activity.setFocusable(true);

		mPager = (ViewPager) findViewById(R.id.viewPager);
		mListViews = new ArrayList<View>();
		LayoutInflater inflater = LayoutInflater.from(this);
		View lay1 = inflater.inflate(R.layout.tab1, null);
		View lay2 = inflater.inflate(R.layout.tab2, null);
		mListViews.add(lay1);
		mListViews.add(lay2);
		mPager.setAdapter(new MyPagerAdapter(mListViews));
		mPager.setCurrentItem(PAGE2);
		mPager.setOnPageChangeListener(new MyOnPageChangeListener());

		myHeadImage.setOnClickListener(new Button.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
			
				Intent intent = new Intent(FriendListActy.this, MyMsg.class);
				User u = new User();
				u.setImg(list.get(0).getImg());
				intent.putExtra("user", u);
				startActivity(intent);
			}

		});

		// 下面是最近会话界面处理
		mRecentListView = (ListView) lay1.findViewById(R.id.tab1_listView);
		mRecentListView.setAdapter(application.getmRecentAdapter());// 先设置空对象，要么从数据库中读出

		// 下面是处理好友列表界面处理
		listVw = (ListView) lay2.findViewById(R.id.friend_listVw);
		myExAdapter = new FriendListAdapter(this, list);
		myExAdapter.setMyImgs(list.get(0).getImg());
		listVw.setAdapter(myExAdapter);

	}

	@Override
	public void onClick(View v)
	{
		switch (v.getId())
		{
		case R.id.tab1:
			mPager.setCurrentItem(PAGE1);// 点击页面1
			break;
		case R.id.tab2:
			mPager.setCurrentItem(PAGE2);// 点击页面1
			break;
		default:
			break;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		mi.inflate(R.menu.friend_list, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		if (messageDB != null)
			messageDB.close();
	}

	@Override
	// 菜单选项添加事件处理
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
		case R.id.friend_menu_add:
			addDialog(FriendListActy.this, "添加好友", "请选择添加方式！");
			break;
		case R.id.friend_menu_exit:
			exitDialog(FriendListActy.this, "提示", "亲！您真的要退出吗？");
			break;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	// 完全退出提示窗
	private void addDialog(Context context, String title, String msg)
	{
		new AlertDialog.Builder(context)
				.setTitle(title)
				.setMessage(msg)
				.setPositiveButton("账号添加",
						new DialogInterface.OnClickListener()
						{

							@Override
							public void onClick(DialogInterface dialog,
									int which)
							{
								Intent intent = new Intent();
								intent.setClass(FriendListActy.this,
										AddFriendWithIDActy.class);
								startActivity(intent);
							}
						})
				.setNegativeButton("摇一摇", new DialogInterface.OnClickListener()
				{
					@Override
					public void onClick(DialogInterface dialog, int which)
					{
						Intent intent = new Intent();
						intent.setClass(FriendListActy.this, ShakeActy.class);
						startActivity(intent);
					}
				}).create().show();
	}

	private void exitDialog(Context context, String title, String msg)
	{
		new AlertDialog.Builder(context).setTitle(title).setMessage(msg)
				.setPositiveButton("确定", new DialogInterface.OnClickListener()
				{

					@Override
					public void onClick(DialogInterface dialog, int which)
					{
						// 关闭服务
						if (application.isClientStart())
						{
							Intent service = new Intent(
									FriendListActy.this,
									GetMsgService.class);
							stopService(service);
						}
						close();// 父类关闭方法
					}
				}).setNegativeButton("取消", null).create().show();
	}

	@Override
	public void getMessage(TranObject msg)
	{// 重写父类的方法，处理消息
		switch (msg.getType())
		{
		case MESSAGE:
			newNum++;
			application.setRecentNum(newNum);// 保存到全局变量
			TextMessage tm = (TextMessage) msg.getObject();
			String message = tm.getMessage();
			ChatMsgEntity entity = new ChatMsgEntity("", MyDate.getDateEN(),
					message, null, true, Integer.parseInt(util.getId()));// 收到的消息
			messageDB.saveMsg(msg.getFromUser(), entity,
					Integer.parseInt(util.getId()));// 保存到数据库
			Toast.makeText(FriendListActy.this,
					"亲！新消息哦 " + msg.getFromUser() + ":" + message, 0).show();// 提示用户
			MediaPlayer.create(this, R.raw.msg).start();// 声音提示
			User user2 = userDB.selectInfo(msg.getFromUser());// 通过id查询对应数据库该好友信息
			RecentChatEntity entity2 = new RecentChatEntity(msg.getFromUser(),
					user2.getImg(), newNum, user2.getName(), MyDate.getDate(),
					message);
			application.getmRecentAdapter().remove(entity2);// 先移除该对象，目的是添加到首部
			application.getmRecentList().addFirst(entity2);// 再添加到首部
			application.getmRecentAdapter().notifyDataSetChanged();
			break;
		case LOGIN:
			User loginUser = (User) msg.getObject();
			Toast.makeText(FriendListActy.this,
					"亲！" + loginUser.getId() + "上线了哦", 0).show();
			MediaPlayer.create(this, R.raw.msg).start();
			break;
		case LOGOUT:
			User logoutUser = (User) msg.getObject();
			Toast.makeText(FriendListActy.this,
					"亲！" + logoutUser.getId() + "下线了哦", 0).show();
			MediaPlayer.create(this, R.raw.msg).start();
			break;
		default:
			break;
		}
	}

	@Override
	public void onBackPressed()
	{// 捕获返回按键事件，进入后台运行
		// 发送广播，通知服务，已进入后台运行
		Intent i = new Intent();
		i.setAction(Constants.BACKKEY_ACTION);
		sendBroadcast(i);

		util.setIsStart(true);// 设置后台运行标志，正在运行
		finish();// 再结束自己
	}

	// ViewPager页面切换监听
	public class MyOnPageChangeListener implements OnPageChangeListener
	{

		int one = offset * 2 + bmpW;// 页卡1 -> 页卡2 偏移量

		public void onPageSelected(int arg0)
		{
			Animation animation = null;
			switch (arg0)
			{
			case PAGE1:// 切换到页卡1
				if (currentIndex == PAGE2)
				{// 如果之前显示的是页卡2
					animation = new TranslateAnimation(0, -one, 0, 0);
				}
				break;
			case PAGE2:// 切换到页卡2
				if (currentIndex == PAGE1)
				{// 如果之前显示的是页卡1
					animation = new TranslateAnimation(-one, 0, 0, 0);
				}
				break;
			default:
				break;
			}
			currentIndex = arg0;// 动画结束后，改变当前图片位置
			animation.setFillAfter(true);// True:图片停在动画结束位置
			animation.setDuration(300);
			cursor.startAnimation(animation);
		}

		public void onPageScrolled(int arg0, float arg1, int arg2)
		{
		}

		public void onPageScrollStateChanged(int arg0)
		{
		}
	}


	protected void updateLoaderTOLBS(JSONObject json) throws JSONException
	{
		JSONArray jArray = json.getJSONArray("pois");
		JSONObject poi = jArray.getJSONObject(0);
		String id = poi.getString("id");
		System.out.println("该title（用户）id:" + id);
		String title = getLoaderID();
		BDLocation location = ((MyApplication) this.getApplication())
				.getLocation();
		Map<String, String> map = new HashMap<String, String>();
		map.put("latitude", "" + location.getLatitude());
		map.put("longitude", "" + location.getLongitude());
		map.put("title", title);
		UpdatePoi.update(id, "96621", "3", map, handler, poi_update_request,
				this);
	}

	protected void addLoaderTOLBS()
	{
		BDLocation location = ((MyApplication) this.getApplication())
				.getLocation();

		if (location != null)
		{
			CreatePoi.create(getLoaderID(), "" + location.getLatitude(), ""
					+ location.getLongitude(), "3", "96621",
					Constants.AK, handler, poi_create_request,
					this);
		} else
		{
			System.out.println("location == null");
		}
	}

	private String getLoaderID()
	{

		return this.util.getId();
	}

	private class MyHandler extends Handler
	{
		public void handleMessage(Message msg)
		{
			Bundle bundle = msg.getData();
			String result = bundle.getString("result");

			if (msg.what == poi_create_request)
			{
				System.out.println("what==poi_create_request");
			} else if (msg.what == list_request)
			{
				System.out.println("what==list_request");
				System.out.println("result:" + UnicodeConvert.convert(result));
				// JSON 解析
				try
				{
					System.out.println("JSON解析:");
					JSONObject json = new JSONObject(result);
					int size = json.getInt("size");
					System.out.println("size:" + size);
					if (size == 0)// 无该title（用户）记录
					{
						System.out.println("无该title（用户）记录");
						addLoaderTOLBS();
					} else if (size == 1)
					// 有则刷新
					{
						updateLoaderTOLBS(json);
					} else
					{
						System.out.println("本title（用户）的记录数量为:" + size);
					}

				} catch (JSONException e)
				{
					e.printStackTrace();
				}
			} else if (msg.what == list_request)
			{
				System.out.println("what == list_request");
			}

		}
	};

	private class MyLocationListener implements BDLocationListener
	{

		@Override
		public void onReceiveLocation(BDLocation location)
		{

			((MyApplication) FriendListActy.this.getApplication())
					.setLocation(location);

			handler = new MyHandler();
			Map<String, String> map = new HashMap<String, String>();
			String title = getLoaderID();
			map.put("title", title);
			ListPoi.list("96621", map, handler, FriendListActy.this,
					list_request);
		}
	}

	private class MyThread extends Thread
	{

		private LocationClient mLocationClient;

		public MyThread(LocationClient mLocationClient)
		{
			this.mLocationClient = mLocationClient;
		}

		@Override
		public void run()
		{

			mLocationClient.start();
			super.run();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		System.out.println("requestCode=" + requestCode);
		System.out.println("resultCode=" + resultCode);
		switch (resultCode)
		{
		case -1:// 删除好友
			User user = (User) data.getSerializableExtra("user");
			list.remove(user);
			break;

		}
		super.onActivityResult(requestCode, resultCode, data);
	}
}
