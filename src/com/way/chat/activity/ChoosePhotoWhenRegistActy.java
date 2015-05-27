package com.way.chat.activity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.way.util.Constants;

public class ChoosePhotoWhenRegistActy extends Activity
{
	/** Called when the activity is first created. */
	private static String TAG = "HelloStranger";

	private Button cameraBtn = null;
	private Button mAlbum = null;
	private Button yesBtn = null;
	private File   mPhotoFile = null;
	private Bitmap myBitmap   = null;
	private ImageView cameraImgv = null;
	private static final int ALBUM = 0;
	private static final int SCALE = 3;
	private ContentResolver resolver;
	private Uri originalUri;
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.choosephoto_when_regist_acty);
		Log.e(TAG, "onCreate");
		mAlbum = (Button) findViewById(R.id.mypicture_btn);
		cameraBtn = (Button) findViewById(R.id.system_camera_btn);
		cameraImgv = (ImageView) findViewById(R.id.imgVwInPhoto);
		yesBtn=(Button) findViewById(R.id.yes_btn);
		// 监听button的事件信息
		mAlbum.setOnClickListener(click);
		cameraBtn.setOnClickListener(click);
		yesBtn.setOnClickListener(click);
	}

	private View.OnClickListener click = new View.OnClickListener()
	{
		@Override
		public void onClick(View v)
		{
			Intent intent = null;
			switch (v.getId())
			{
			case R.id.mypicture_btn:
				intent = new Intent(Intent.ACTION_GET_CONTENT);
				intent.addCategory(Intent.CATEGORY_OPENABLE);
				intent.setType("image/jpeg");
				startActivityForResult(intent, ALBUM);
				break;
			case R.id.system_camera_btn:
				
				intent = new Intent("android.media.action.IMAGE_CAPTURE");
				File file = new File(Constants.SAVEPATH);
				file.mkdirs();// 创建文件夹
				mPhotoFile = new File(Constants.SAVEPATH,getPhotoFileName());
				intent.addCategory(Intent.CATEGORY_DEFAULT);
				// 把文件地址转换成Uri格式
				Uri uri = Uri.fromFile(mPhotoFile);
				// 设置系统相机拍摄照片完成后图片文件的存放地址
				intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
				startActivityForResult(intent, 1);
				break;
			case R.id.yes_btn:
				// 新建一个Intent对象   
                intent = new Intent();  
                // 指定intent要启动的类  
                intent.setClass(ChoosePhotoWhenRegistActy.this, RegisterActy.class);  
                
                intent.putExtra("bitmap", originalUri);
                //启动一个新的Activity  
                startActivity(intent);  
                // 关闭当前的Activity   
                ChoosePhotoWhenRegistActy.this.finish();  
			}
		}
	};
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		resolver = getContentResolver();
		
		switch (requestCode)
		{
		case 1://拍照
			if (Activity.RESULT_OK == resultCode)
			{
				originalUri = Uri.fromFile(mPhotoFile);
				cameraImgv.setImageURI(originalUri);
				/*-----------即时刷新相册——>向相册插入图片---------*/
		    	ContentValues localContentValues = new ContentValues(); 
		    	localContentValues.put("_data", mPhotoFile.toString()); 
		    	localContentValues.put("description", "save image ---"); 
		    	localContentValues.put("mime_type", "image/jpeg");
		    	Uri localUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI; 
		    	resolver.insert(localUri, localContentValues);
				/*----------------------------------------------*/
			}
			break;
		case ALBUM:
			
			if (resultCode==Activity.RESULT_OK)
			{
				originalUri = data.getData();
				try {
					// 使用ContentProvider通过URI获取原始图片
					Bitmap photo = MediaStore.Images.Media.getBitmap(resolver,originalUri);
					if (photo != null) {
						// 为防止原始图片过大导致内存溢出，这里先缩小原图显示，然后释放原始Bitmap占用的内存
						myBitmap = zoomBitmap(photo, photo.getWidth()/SCALE, photo.getHeight()/SCALE);
						// 释放原始图片占用的内存，防止out of memory异常发生
						photo.recycle();
						cameraImgv.setImageBitmap(myBitmap);
					}
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
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
	private String getPhotoFileName()
	{
		Date date = new Date(System.currentTimeMillis());
		SimpleDateFormat dateFormat = new SimpleDateFormat(
				"'IMG'_yyyyMMdd_HHmmss");
		return dateFormat.format(date) + ".jpg";
	}
	@Override
	public void onBackPressed() // 捕捉返回键
	{
		Intent intent = new Intent(); // 新建一个Intent对象
		intent.setClass(ChoosePhotoWhenRegistActy.this, LoginActy.class);// 指定intent要启动的类
		startActivity(intent); // 启动一个新的Activity
		finish(); // 关闭当前的Activity
	}
	

}