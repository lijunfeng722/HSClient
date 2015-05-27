package com.way.chat.activity;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.way.chat.activity.SendWhatDialog.OnCustomDialogListener;
import com.way.chat.common.bean.TextMessage;
import com.way.chat.common.bean.User;
import com.way.chat.common.tran.bean.TranObject;
import com.way.chat.common.tran.bean.TranObjectType;
import com.way.client.Client;
import com.way.client.ClientOutputThread;
import com.way.util.BitmapUtil;
import com.way.util.Constants;
import com.way.util.MessageDB;
import com.way.util.MyDate;
import com.way.util.SharePreferenceUtil;

/**
 * 聊天Activity
 * 
 * @author way
 */
public class ChatActivity extends MyActivity implements OnClickListener
{
	private Button mBtnSend;// 发送btn
	private Button mBtnSendWhat;
	private Button mBtnBack;// 返回btn
	private EditText mEditTextContent;
	private TextView mFriendName;
	private ListView mListView;
	private ChatMsgViewAdapter mAdapter;// 消息视图的Adapter
	private List<ChatMsgEntity> mDataArrays = new ArrayList<ChatMsgEntity>();// 消息对象数组
	private SharePreferenceUtil util;
	private User user;
	private User me;
	private File   mPhotoFile = null;
	private Bitmap myBitmap   = null;
	private MessageDB messageDB;
	private MyApplication application;
	private static final int SCALE = 3;
	private String photoSavePath;

	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);// 去掉标题栏
		setContentView(R.layout.chat_acty);
		application = (MyApplication) getApplicationContext();
		messageDB = new MessageDB(this);
		user = (User) getIntent().getSerializableExtra("user");
		me = (User) getIntent().getSerializableExtra("me");
		util = new SharePreferenceUtil(this, Constants.SAVE_USER);
		initView();// 初始化view
		initData();// 初始化数据
	}

	/**
	 * 初始化view
	 */
	public void initView()
	{
		mListView = (ListView) findViewById(R.id.listview);
		mBtnSendWhat = (Button) findViewById(R.id.chat_send_someting);
		mBtnSendWhat.setOnClickListener(this);
		mBtnSend = (Button) findViewById(R.id.chat_send);
		mBtnSend.setOnClickListener(this);
		mBtnBack = (Button) findViewById(R.id.chat_back);
		mBtnBack.setOnClickListener(this);
		mFriendName = (TextView) findViewById(R.id.chat_name);
		mFriendName.setText(user.getName());
		mEditTextContent = (EditText) findViewById(R.id.chat_editmessage);
	}

	/**
	 * 加载消息历史，从数据库中读出
	 */
	public void initData()
	{

		List<ChatMsgEntity> list = messageDB.getMsg(user.getId());
		if (list.size() > 0)
		{
			for (ChatMsgEntity entity : list)
			{
				if (entity.getName().equals(""))
				{
					entity.setName(user.getName());
				}
				if (entity.getImg() == null)
				{
					entity.setImg(user.getImg());
				}
				if (entity.getIdFrom() == Integer.parseInt(util.getId()))
					mDataArrays.add(entity);
			}
			Collections.reverse(mDataArrays);
		}
		mAdapter = new ChatMsgViewAdapter(this, mDataArrays);
		mAdapter.setMyImg(BitmapUtil.toRoundCorner(me.getImg(), 1));
		mAdapter.setFriendImg(BitmapUtil.toRoundCorner(user.getImg(), 1));
		mListView.setAdapter(mAdapter);
		mListView.setSelection(mAdapter.getCount() - 1);
	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		messageDB.close();
	}

	@Override
	public void onClick(View v)
	{
		switch (v.getId())
		{
		case R.id.chat_send:// 发送按钮点击事件
			ChatMsgEntity entity = new ChatMsgEntity();
			entity.setName(util.getName());
			entity.setDate(MyDate.getDateEN());
			entity.setMessage(mEditTextContent.getText().toString());
			entity.setIsComMsg(false);
			entity.setMsgType(ChatMsgEntity.MSG_TYPE_TEXT);
			send(entity);
			break;
		case R.id.chat_back:// 返回按钮点击事件
			finish();// 结束,实际开发中，可以返回主界面
			break;
		case R.id.chat_send_someting:
			SendWhatDialog dialog = new SendWhatDialog(ChatActivity.this,
					R.layout.send_what_dialog, R.style.Theme_dialog);
			dialog.setCustomDialogListener(new OnCustomDialogListener()
			{

				@Override
				public void back(String str)
				{
					if ("选择发送录音".equals(str))
					{
						String savePath = Constants.SAVEPATH + "/"
								+ util.getName() + "/recordFile";
						final String fileName = System.currentTimeMillis()
								+ ".3gp";
						RecordDialog dialog = new RecordDialog(
								ChatActivity.this, R.layout.record_dialog,
								R.style.Theme_dialog, savePath, fileName);
						dialog.setCustomDialogListener(new RecordDialog.OnCustomDialogListener()
						{

							@Override
							public void back(String savePath,
									String recodeFileName)
							{
								// TODO发送录音文件
								File file = new File(savePath + "/"
										+ recodeFileName);
								BufferedInputStream is = null;
								try
								{
									is = new BufferedInputStream(
											new FileInputStream(file));
								} catch (FileNotFoundException e)
								{
									e.printStackTrace();
								}
								byte[] data = new byte[(int) file.length()];
								try
								{
									int len = is.read(data);
								} catch (IOException e)
								{
									e.printStackTrace();
								}
								if (is != null)
									try
									{
										is.close();
									} catch (IOException e)
									{
										e.printStackTrace();
									}
								ChatMsgEntity entity = new ChatMsgEntity();
								entity.setName(util.getName());
								entity.setDate(MyDate.getDateEN());
								entity.setMessage("我是录音");
								entity.setIsComMsg(false);
								entity.setMsgType(ChatMsgEntity.MSG_TYPE_VIOCE);
								entity.setVoiceByte(data);
								entity.setPath(savePath + "/" + fileName);
								send(entity);
							}
						});
						dialog.show();
					} else if ("选择发送图片".equals(str))
					{
						// TODO 发送图片
						System.out.println("进行发送图片");
						Intent intent = null;
						intent = new Intent(Intent.ACTION_GET_CONTENT);
						intent.addCategory(Intent.CATEGORY_OPENABLE);
						intent.setType("image/jpeg");
						startActivityForResult(intent, 0);
					}
				}
			});
			dialog.show();
			break;
		}
}
	private String getPhotoFileName()
	{
		Date date = new Date(System.currentTimeMillis());
		SimpleDateFormat dateFormat = new SimpleDateFormat(
				"'IMG'_yyyyMMdd_HHmmss");
		return dateFormat.format(date) + ".jpg";
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		ContentResolver resolver = getContentResolver();
		
		switch (requestCode)
		{
		case 0://选择图片
			if (Activity.RESULT_OK == resultCode)
			{
		    	Uri originalUri = data.getData();
		    	Cursor cursor = getContentResolver().query(originalUri, null, null, null, null);
		    	cursor.moveToFirst();
		    	String imgPath = cursor.getString(1);
		    	cursor.close();
				try {
					// 使用ContentProvider通过URI获取原始图片
					Bitmap photo = MediaStore.Images.Media.getBitmap(resolver,originalUri);
					if (photo != null) {
						// 为防止原始图片过大导致内存溢出，这里先缩小原图显示，然后释放原始Bitmap占用的内存
						myBitmap = zoomBitmap(photo, photo.getWidth()/SCALE, photo.getHeight()/SCALE);
						// 释放原始图片占用的内存，防止out of memory异常发生
						photo.recycle();
					}
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				String savePath = Constants.SAVEPATH + "/"
						+ util.getName() + "/imgFile";
				File createDir = new File(savePath);
                if (!createDir.exists()) {
                        createDir.mkdirs(); 
                }
				File f = new File(savePath,getPhotoFileName());
				FileOutputStream fOut = null;
                try {
                        fOut = new FileOutputStream(f);
                        myBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
                        fOut.flush();
                        fOut.close();
                } catch (IOException e) {
                        e.printStackTrace();
                }
		    	ChatMsgEntity entity = new ChatMsgEntity();
				entity.setName(util.getName());
				entity.setDate(MyDate.getDateEN());
				entity.setMessage("我是图片");
				entity.setImageByte(myBitmap);
				entity.setIsComMsg(false);
				entity.setMsgType(ChatMsgEntity.MSG_TYPE_IMAGE);
				
				entity.setPath(f.toString());
				send(entity);
			}
			break;
		}
	}
	public Bitmap zoomBitmap(Bitmap bitmap, int width, int height) 
	{

		int w = bitmap.getWidth();

		int h = bitmap.getHeight();

		Matrix matrix = new Matrix();

		float scaleWidth = ((float) width / w);

		float scaleHeight = ((float) height / h);

		matrix.postScale(scaleWidth, scaleHeight);// 利用矩阵进行缩放不会造成内存溢出

		Bitmap newbmp = Bitmap.createBitmap(bitmap, 0, 0, w, h, matrix, true);

		return newbmp;
	}  
	/**
	 * 发送消息
	 */
	private void send(ChatMsgEntity entity)
	{

		if (entity.getMessage().length() > 0)
		{

			messageDB.saveMsg(user.getId(), entity,
					Integer.parseInt(util.getId()));

			mDataArrays.add(entity);
			mAdapter.notifyDataSetChanged();// 通知ListView，数据已发生改变
			mEditTextContent.setText("");// 清空编辑框数据
			mListView.setSelection(mListView.getCount() - 1);// 发送一条消息时，ListView显示选择最后一项
			MyApplication application = (MyApplication) this
					.getApplicationContext();
			Client client = application.getClient();
			ClientOutputThread out = client.getClientOutputThread();
			if (out != null)
			{
				TranObject<TextMessage> o = new TranObject<TextMessage>(
						TranObjectType.MESSAGE);
				TextMessage message = new TextMessage();
				message.setMessage(entity.getMessage());
				message.setMessageType(entity.getMsgType());
				message.setVoiceByte(entity.getVoiceByte());
				message.setImageByte(entity.getImageByte());
				o.setObject(message);
				o.setFromUser(Integer.parseInt(util.getId()));
				o.setToUser(user.getId());
				out.setMsg(o);
			}
			// 下面是添加到最近会话列表的处理，在按发送键之后
			RecentChatEntity entity1 = new RecentChatEntity(user.getId(),
					user.getImg(), 0, user.getName(), MyDate.getDate(),
					entity.getMessage());
			application.getmRecentList().remove(entity1);
			application.getmRecentList().addFirst(entity1);
			application.getmRecentAdapter().notifyDataSetChanged();
		}
	}

	@Override
	public void getMessage(TranObject msg)
	{
		switch (msg.getType())
		{
		case MESSAGE:
			TextMessage tm = (TextMessage) msg.getObject();
			String message = tm.getMessage();
			ChatMsgEntity entity = new ChatMsgEntity(user.getName(),
					MyDate.getDateEN(), message, user.getImg(), true,
					Integer.parseInt(util.getId()));// 收到的消息
			entity.setMsgType(tm.getMessageType());
			if (tm.getMessageType() == ChatMsgEntity.MSG_TYPE_VIOCE)
			{
				String savePath = Constants.SAVEPATH + "/" + util.getName()
						+ "/receivedFile/" + user.getName() + "/recordFile";
				String fileName = System.currentTimeMillis() + ".3gp";
				entity.setPath(savePath + "/" + fileName);
				File saveDir = new File(savePath);
				if (!saveDir.exists())
					saveDir.mkdirs();
				File file = new File(savePath + "/" + fileName);
				BufferedOutputStream os = null;
				try
				{
					os = new BufferedOutputStream(new FileOutputStream(file));
				} catch (FileNotFoundException e)
				{
					e.printStackTrace();
				}
				byte[] data = tm.getVoiceByte();
				try
				{
					os.write(data);
				} catch (IOException e)
				{
					e.printStackTrace();
				}
				if (os != null)
					try
					{
						os.close();
					} catch (IOException e)
					{
						e.printStackTrace();
					}

			}
			if (tm.getMessageType() == ChatMsgEntity.MSG_TYPE_IMAGE)
			{
				String savePath = Constants.SAVEPATH + "/" + util.getName()
						+ "/receivedFile/" + user.getName() + "/imageFile";
				String fileName = System.currentTimeMillis() + ".png";
				entity.setPath(savePath + "/" + fileName);
				File saveDir = new File(savePath);
				if (!saveDir.exists())
					saveDir.mkdirs();
				File file = new File(savePath + "/" + fileName);
				BufferedOutputStream os = null;
				try
				{
					os = new BufferedOutputStream(new FileOutputStream(file));
				} catch (FileNotFoundException e)
				{
					e.printStackTrace();
				}
				Bitmap img = tm.getImageByte();
				entity.setImageByte(img);
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				img.compress(Bitmap.CompressFormat.JPEG, 100, baos);
				try
				{
					os.write(baos.toByteArray());
				} catch (IOException e)
				{
					e.printStackTrace();
				}
				if (os != null)
					try
					{
						os.close();
					} catch (IOException e)
					{
						e.printStackTrace();
					}

			}
			if (msg.getFromUser() == user.getId() || msg.getFromUser() == 0)
			{// 如果是正在聊天的好友的消息，或者是服务器的消息
				if (msg.getFromUser() != 0)
					messageDB.saveMsg(user.getId(), entity,
							Integer.parseInt(util.getId()));

				mDataArrays.add(entity);
				mAdapter.notifyDataSetChanged();
				mListView.setSelection(mListView.getCount() - 1);
				MediaPlayer.create(this, R.raw.msg).start();
			} else
			{
				messageDB.saveMsg(msg.getFromUser(), entity,
						Integer.parseInt(util.getId()));// 保存到数据库
				Toast.makeText(ChatActivity.this,
						"您有新的消息来自：" + msg.getFromUser() + ":" + message, 0)
						.show();// 其他好友的消息，就先提示，并保存到数据库
				MediaPlayer.create(this, R.raw.msg).start();
			}
			break;
		case LOGIN:
			User loginUser = (User) msg.getObject();
			Toast.makeText(ChatActivity.this, loginUser.getId() + "上线了", 0)
					.show();
			MediaPlayer.create(this, R.raw.msg).start();
			break;
		case LOGOUT:
			User logoutUser = (User) msg.getObject();
			Toast.makeText(ChatActivity.this, logoutUser.getId() + "下线了", 0)
					.show();
			MediaPlayer.create(this, R.raw.msg).start();
			break;
		default:
			break;
		}
	}
}