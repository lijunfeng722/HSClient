package com.way.chat.activity;

import java.io.FileNotFoundException;
import java.io.IOException;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.way.chat.common.bean.User;
import com.way.chat.common.tran.bean.TranObject;
import com.way.chat.common.tran.bean.TranObjectType;
import com.way.client.Client;
import com.way.client.ClientOutputThread;
import com.way.util.DialogFactory;
import com.way.util.Encode;

public class RegisterActy extends MyActivity implements OnClickListener {

	private Button mBtnRegister;
	private Button mRegBack;
	private ImageView imageview;
	private Bitmap bitmap;
	private EditText mEmailEt, mNameEt, mPasswdEt, mPasswdEt2;
	
	private MyApplication application;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.register);
		imageview=(ImageView)findViewById(R.id.imageView1);
        Intent intent=getIntent();
        if(intent!=null)
        {
        	ContentResolver resolver = getContentResolver();
        	Uri fileUri =intent.getParcelableExtra("bitmap");
        	Bitmap photo =null;
        	try
			{
        		photo = MediaStore.Images.Media.getBitmap(resolver,fileUri);
				
        		this.bitmap = zoomBitmap(photo, photo.getWidth()/3, photo.getHeight()/3);
        		
				// 释放原始图片占用的内存，防止out of memory异常发生
        		photo.recycle();
			} catch (FileNotFoundException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            imageview.setImageBitmap(this.bitmap);
        }
		application = (MyApplication) this.getApplicationContext();
		initView();

	}

	public void initView() {
		mBtnRegister = (Button) findViewById(R.id.register_btn);
		mRegBack = (Button) findViewById(R.id.reg_back_btn);
		mBtnRegister.setOnClickListener(this);
		mRegBack.setOnClickListener(this);

		mEmailEt = (EditText) findViewById(R.id.reg_email);
		mNameEt = (EditText) findViewById(R.id.reg_name);
		mPasswdEt = (EditText) findViewById(R.id.reg_password);
		mPasswdEt2 = (EditText) findViewById(R.id.reg_password2);

	}

	private Dialog mDialog = null;

	private void showRequestDialog() {
		if (mDialog != null) {
			mDialog.dismiss();
			mDialog = null;
		}
		mDialog = DialogFactory.creatRequestDialog(this, "正在注册中...");
		mDialog.show();
	}

	@Override
	public void onBackPressed() {// 捕获返回键
		toast(RegisterActy.this);
	}

	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.register_btn:
			estimate();
			break;
		case R.id.reg_back_btn:
			toast(RegisterActy.this);
			break;
		default:
			break;
		}
	}

	private void toast(Context context) {
		new AlertDialog.Builder(context).setTitle("HS注册")
				.setMessage("亲！您真的不注册了吗？")
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int which) {
						finish();
					}
				}).setNegativeButton("取消", null).create().show();
	}

	private void estimate() {
		String email = mEmailEt.getText().toString();
		String name = mNameEt.getText().toString();
		String passwd = mPasswdEt.getText().toString();
		String passwd2 = mPasswdEt2.getText().toString();
		if (email.equals("") || name.equals("") || passwd.equals("")
				|| passwd2.equals("")) {
			DialogFactory.ToastDialog(RegisterActy.this, "HS注册",
					"亲！带*项是不能为空的哦");
		} else {
			if (passwd.equals(passwd2)) {
				showRequestDialog();
				// 提交注册信息
				if (application.isClientStart()) {// 如果已连接上服务器
					Resources res=getResources();
					Client client = application.getClient();
					ClientOutputThread out = client.getClientOutputThread();
					TranObject<User> o = new TranObject<User>(
							TranObjectType.REGISTER);
					User u = new User();
					u.setEmail(email);
					u.setName(name);
					u.setImg(bitmap);
					u.setPassword(Encode.getEncode("MD5", passwd));
					o.setObject(u);
					out.setMsg(o);
				} else {
					if (mDialog.isShowing())
						mDialog.dismiss();
					DialogFactory.ToastDialog(this, "HS注册", "亲！服务器暂未开放哦");
				}

			} else {
				DialogFactory.ToastDialog(RegisterActy.this, "HS注册",
						"亲！您两次输入的密码不同哦");
			}
		}
	}

	@Override
	public void getMessage(TranObject msg) {
		// TODO Auto-generated method stub
		switch (msg.getType()) {
		case REGISTER:
			User u = (User) msg.getObject();
			int id = u.getId();
			if (id > 0) {
				if (mDialog != null) {
					mDialog.dismiss();
					mDialog = null;
				}
				addDialog(RegisterActy.this, "注册成功", "亲！请牢记您的登录帐号哦：" + id);
			} else {
				if (mDialog != null) {
					mDialog.dismiss();
					mDialog = null;
				}
				DialogFactory.ToastDialog(RegisterActy.this, "HS注册",
						"亲！很抱歉！HS号暂时缺货哦");
			}
			break;

		default:
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
	private void addDialog(Context context, String title, String msg)
	{
		new AlertDialog.Builder(context)
				.setTitle(title)
				.setMessage(msg)
				.setPositiveButton("确定",
						new DialogInterface.OnClickListener()
						{

							@Override
							public void onClick(DialogInterface dialog,
									int which)
							{
								Intent intent = new Intent();
								intent.setClass(RegisterActy.this,
										LoginActy.class);
								startActivity(intent);
							}
						}).create().show();
	}
}
