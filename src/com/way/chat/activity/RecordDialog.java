package com.way.chat.activity;

import java.io.File;

import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import com.way.util.Constants;
import com.way.util.RecordUtlis;

public class RecordDialog extends Dialog
{

	private String fileName = null;
	private String savePath = null;
	private static int default_width = 160; // 默认宽度
	private static int default_height = 160;// 默认高度
	private Button stopBtn = null;
	private OnCustomDialogListener customDialogListener = null;
	private RecordUtlis recordUtlis = null;

	public RecordDialog(Context context)
	{
		super(context);
	}

	public RecordDialog(Context context, int layout, int style,String savePath,String fileName)
	{
		this(context, default_width, default_height, layout, style,savePath,fileName);
	}

	public RecordDialog(Context context, int width, int height, int layout,
			int style,final String savePath,final String fileName)
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
		this.fileName= fileName;
		this.savePath= savePath;

		stopBtn = (Button) findViewById(R.id.record_stop_btn);
		stopBtn.setOnClickListener(new View.OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				recordUtlis.stopRecord();
				System.out.println("录音完毕");
				customDialogListener.back(savePath,fileName);
				RecordDialog.this.dismiss();
			}
		});

		recordUtlis = new RecordUtlis(savePath,fileName);
		recordUtlis.startRecord();
	}

	private float getDensity(Context context)
	{
		Resources resources = context.getResources();
		DisplayMetrics dm = resources.getDisplayMetrics();
		return dm.density;
	}

	public interface OnCustomDialogListener
	{
		public void back(String savePath,String recodeFileName);
	}
	

	public void setCustomDialogListener(
			OnCustomDialogListener customDialogListener)
	{
		this.customDialogListener = customDialogListener;
	}
}
