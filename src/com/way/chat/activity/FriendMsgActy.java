package com.way.chat.activity;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.way.chat.common.bean.User;
import com.way.chat.common.tran.bean.TranObject;
import com.way.chat.common.tran.bean.TranObjectType;
import com.way.client.Client;
import com.way.client.ClientOutputThread;
import com.way.util.BitmapUtil;
import com.way.util.Constants;
import com.way.util.DialogFactory;
import com.way.util.SharePreferenceUtil;

public class FriendMsgActy extends MyActivity
{

	private Button BtnChat;
	private Button BtnDelete;
	private TextView nameView;
	private TextView idView;
	private TextView emailView;
	private ImageView iconImgVw;
	private User user;
	private User me;
	private MyApplication application;
	private SharePreferenceUtil util;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.friendmsg_acty);
		user = (User) getIntent().getSerializableExtra("user");
		me = (User) getIntent().getSerializableExtra("me");
		application = (MyApplication) this.getApplicationContext();
		iconImgVw = (ImageView)findViewById(R.id.ImgVwInFMSG);
		BtnChat = (Button) findViewById(R.id.startBtnInFMSG);
		BtnDelete = (Button) findViewById(R.id.deleteBtnInFMSG);
		nameView = (TextView) findViewById(R.id.nametvInFMSG);
		idView = (TextView) findViewById(R.id.idTvInFMSG);
		emailView = (TextView) findViewById(R.id.emailTvInFMSG);
		iconImgVw.setImageBitmap(BitmapUtil.toRoundCorner(user.getImg(), 1));
		nameView.setText(user.getName());
		idView.setText(user.getId() + " ");
		emailView.setText(user.getEmail());
		util = new SharePreferenceUtil(this, Constants.SAVE_USER);
		// submitCheck(user.getId());
		BtnChat.setOnClickListener(new Button.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				Intent intent = new Intent(FriendMsgActy.this, ChatActivity.class);
				intent.putExtra("me", me);
				intent.putExtra("user", user);
				startActivity(intent);
			}

		});
		BtnDelete.setOnClickListener(new Button.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				submitDelete(user.getId());
				Intent intent = new Intent();
				intent.setClass(FriendMsgActy.this, FriendListActy.class);
				startActivity(intent);
			}

		});

	}

	private Dialog mDialog = null;

	private void showRequestDialog()
	{
		if (mDialog != null)
		{
			mDialog.dismiss();
			mDialog = null;
		}
		mDialog = DialogFactory.creatRequestDialog(this, "正在删除...");
		mDialog.show();
	}

	private void submitDelete(int id)
	{
		showRequestDialog();
		// 通过Socket验证信息
		Client client = application.getClient();
		ClientOutputThread out = client.getClientOutputThread();
		TranObject<User> o = new TranObject<User>(TranObjectType.FriendDelete);
		User u = new User();
		u.setId(id);
		o.setFromUser(Integer.parseInt(util.getId()));
		System.out.println(util.getId());
		o.setObject(u);
		out.setMsg(o);
		Toast.makeText(getApplicationContext(), "删除成功", 0).show();
		
		setResult(-1, getIntent());
		finish();
	}

	private void submitCheck(int id)
	{
		Client client = application.getClient();
		ClientOutputThread out = client.getClientOutputThread();
		TranObject<User> o = new TranObject<User>(TranObjectType.FriendCheck);
		User u = new User();
		u.setId(id);
		o.setObject(u);
		out.setMsg(o);
	}

	@Override
	public void getMessage(TranObject msg)
	{
		System.out.println("Result:" + msg.getType());
		switch (msg.getType())
		{
		case FriendCheck:// LoginActivity只处理登录的消息
			User friend = (User) msg.getObject();
			System.out.println(friend.getId());
			break;
		default:
			break;
		}
		finish();
	}
}
