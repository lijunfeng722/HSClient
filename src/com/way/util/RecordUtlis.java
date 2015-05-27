package com.way.util;

import java.io.File;
import java.io.IOException;

import android.media.MediaRecorder;

public class RecordUtlis
{
	private MediaRecorder mRecorder = null;
	private String fileName = null;
	private String savePath = null;

	public RecordUtlis(String savePath, String fileName)
	{
		this.fileName = fileName;
		this.savePath = savePath;
		File saveDir = new File(savePath);
		System.out.println("savePath in RecordUtlis:"
				+ saveDir.getAbsolutePath());
		if (!saveDir.exists())
		{
			saveDir.mkdirs();
			System.out.println("make new dir");
		}

	}

	public void startRecord()
	{
		mRecorder = new MediaRecorder();
		mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
		mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
		mRecorder.setOutputFile(savePath + "/" + fileName);
		mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_WB);
		try
		{
			mRecorder.prepare();
			mRecorder.start();
		} catch (IOException e)
		{
			System.out.println(e);
		}
	}

	public void stopRecord()
	{
		mRecorder.stop();
		mRecorder.release();
		mRecorder = null;
	}
}
