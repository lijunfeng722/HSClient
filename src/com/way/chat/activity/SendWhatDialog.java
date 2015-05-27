package com.way.chat.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

public class SendWhatDialog extends Dialog implements
		android.view.View.OnClickListener
{

	private static int default_width = 240; // 默认宽度
	private static int default_height = 120;// 默认高度
	private ImageView voiceIV = null;
	private ImageView picIV = null;
	private OnCustomDialogListener customDialogListener = null;

	public SendWhatDialog(Context context, int layout, int style)
	{
		this(context, default_width, default_height, layout, style);
	}

	public SendWhatDialog(Context context, int width, int height, int layout,
			int style)
	{
		super(context, style);
		// set content
		setContentView(layout);
		// set window params
		Window window = getWindow();
		WindowManager.LayoutParams params = window.getAttributes();
		// set width,height by density and gravity
		float density = getDensity(context);
		params.width = (int) (width * density);
		params.height = (int) (height * density);
		params.gravity = Gravity.CENTER;
		window.setAttributes(params);

		voiceIV = (ImageView) findViewById(R.id.sendvoice);
		voiceIV.setOnClickListener(this);

		picIV = (ImageView) findViewById(R.id.sendpic);
		picIV.setOnClickListener(this);

	}

	private float getDensity(Context context)
	{
		Resources resources = context.getResources();
		DisplayMetrics dm = resources.getDisplayMetrics();
		return dm.density;
	}

	public interface OnCustomDialogListener
	{
		public void back(String str);
	}

	public void setCustomDialogListener(
			OnCustomDialogListener customDialogListener)
	{
		this.customDialogListener = customDialogListener;
	}

	@Override
	public void onClick(View v)
	{
		switch (v.getId())
		{
		case R.id.sendvoice:
			customDialogListener.back("选择发送录音");
			SendWhatDialog.this.dismiss();
			break;
		case R.id.sendpic:
			customDialogListener.back("选择发送图片");
			SendWhatDialog.this.dismiss();
			break;
		default:
			System.out.println("未选择");
			break;
		}
	}

}
