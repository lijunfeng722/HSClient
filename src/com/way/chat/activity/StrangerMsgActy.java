package com.way.chat.activity;

import com.way.chat.common.bean.User;
import com.way.chat.common.tran.bean.TranObject;
import com.way.chat.common.tran.bean.TranObjectType;
import com.way.client.Client;
import com.way.client.ClientOutputThread;
import com.way.util.BitmapUtil;
import com.way.util.Constants;
import com.way.util.DialogFactory;
import com.way.util.SharePreferenceUtil;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class StrangerMsgActy extends MyActivity
{
	private Button BtnAdd;
	private Button BtnCancel;
	private TextView nameView;
	private TextView idView;
	private TextView emailView;
	private ImageView iconImgVw;
	private TranObject msg;
	private MyApplication application;
	private SharePreferenceUtil util;
	private User newFriend;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.stranger_msg);
		application = (MyApplication) this.getApplicationContext();
		iconImgVw = (ImageView)findViewById(R.id.ImgVwInSMSG);
		BtnAdd = (Button) findViewById(R.id.addBtnInSMSG);
		BtnCancel = (Button) findViewById(R.id.backBtnInSMSG);
		nameView = (TextView) findViewById(R.id.nameTvInSMSG);
		idView = (TextView) findViewById(R.id.idTvInSMSG);
		emailView = (TextView) findViewById(R.id.emailTvInSMSG);

		String ID = StrangerMsgActy.this.getIntent().getStringExtra("ID");// 获得陌生人ID

		msg = (TranObject) getIntent().getSerializableExtra(Constants.MSGKEY);
		newFriend = (User) msg.getObject();
		nameView.setText(newFriend.getName());
		idView.setText(newFriend.getId() + " ");
		emailView.setText(newFriend.getEmail());
		iconImgVw.setImageBitmap(BitmapUtil.toRoundCorner(newFriend.getImg(), 1));
		util = new SharePreferenceUtil(this, Constants.SAVE_USER);
		BtnAdd.setOnClickListener(new Button.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				submit();
				Intent intent = new Intent();
				// 指定intent要启动的类
				intent.setClass(StrangerMsgActy.this, FriendListActy.class);
				intent.putExtra("From","StrangerMsg");
				intent.putExtra("newFriend", newFriend);
				startActivity(intent);
			}

		});
		
		BtnCancel.setOnClickListener(new Button.OnClickListener()
		{
			
			@Override
			public void onClick(View v)
			{
				finish();
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
		mDialog = DialogFactory.creatRequestDialog(this, "正在添加...");
		mDialog.show();
	}

	private void submit()
	{
		showRequestDialog();
		// 通过Socket验证信息
		Client client = application.getClient();
		ClientOutputThread out = client.getClientOutputThread();
		// System.out.println("fdfgdfjh");
		TranObject<User> o = new TranObject<User>(TranObjectType.FriendAdd);
		User u = new User();
		User v = (User) msg.getObject();
		u.setId(v.getId());
		o.setFromUser(Integer.parseInt(util.getId()));
		o.setObject(u);
		out.setMsg(o);
	}

	@Override
	public void getMessage(TranObject msg)
	{
		Toast.makeText(getApplicationContext(), "添加成功", 0).show();
		finish();
	}
}
