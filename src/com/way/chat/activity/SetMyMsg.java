package com.way.chat.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class SetMyMsg extends Activity{
	private Button btn;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.set_mymsg);
		btn = (Button) findViewById(R.id.button1);
		btn.setOnClickListener(new Button.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
			}

		});
	}
}
