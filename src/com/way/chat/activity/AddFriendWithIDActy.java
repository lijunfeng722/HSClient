package com.way.chat.activity;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.way.chat.common.bean.User;
import com.way.chat.common.tran.bean.TranObject;
import com.way.chat.common.tran.bean.TranObjectType;
import com.way.client.Client;
import com.way.client.ClientOutputThread;
import com.way.util.Constants;
import com.way.util.DialogFactory;

public class AddFriendWithIDActy extends MyActivity {
	private EditText mAccounts;
	private Button addBtn;
	private MyApplication application;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.addfriend_acty);
		application = (MyApplication) this.getApplicationContext();
		mAccounts = (EditText) findViewById(R.id.addIdTvInFADD);
		addBtn = (Button) findViewById(R.id.addBtnInFADD);
		addBtn.setOnClickListener(new Button.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				submit();
			}

		});
		
	}
	
	private Dialog mDialog = null;
	
	private void showRequestDialog() {
		if (mDialog != null) {
			mDialog.dismiss();
			mDialog = null;
		}
		mDialog = DialogFactory.creatRequestDialog(this, "正在查询...");
		mDialog.show();
	}
	
	private void submit() {
		String accounts = mAccounts.getText().toString();
		if (accounts.length() == 0) {
			DialogFactory.ToastDialog(this, "添加好友", "亲！帐号不能为空哦");
		} else {
			showRequestDialog();
			// 通过Socket验证信息
				Client client = application.getClient();
				ClientOutputThread out = client.getClientOutputThread();
				TranObject<User> o = new TranObject<User>(TranObjectType.FriendCheck);
				User u = new User();
				u.setId(Integer.parseInt(accounts));
				o.setObject(u);
				out.setMsg(o);
		}
	}
	
	@Override
	public void getMessage(TranObject msg) {
		if (msg != null) {
			System.out.println("Result:" + msg);
			switch (msg.getType()) {
			case FriendCheck:// LoginActivity只处理登录的消息
				User friend = (User) msg.getObject();
				System.out.println(friend);
				if(friend.getId()==0) {
					DialogFactory.ToastDialog(AddFriendWithIDActy.this, "查询结果",
							"亲！您输入的账号不存在哦");
					if (mDialog.isShowing())
						mDialog.dismiss();
				}
				else {
					Intent intent = new Intent();  
	                intent.setClass(AddFriendWithIDActy.this, StrangerMsgActy.class);  
	                intent.putExtra(Constants.MSGKEY, msg);
	                startActivity(intent); 
	                finish();
					
				} 
				break;
			default:
				break;
			}
		}
	}
}
