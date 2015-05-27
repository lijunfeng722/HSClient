package com.way.chat.common.bean;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;

/**
 * 文本消息
 * 
 * @author way
 */
public class TextMessage implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String message;
	private byte[] voiceByte;
	private byte[] ImageByte;
	private int messageType;

	public TextMessage() {

	}

	public TextMessage(String message) {
		this.message = message;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public byte[] getVoiceByte()
	{
		return voiceByte;
	}

	public void setVoiceByte(byte[] voiceByte)
	{
		this.voiceByte = voiceByte;
	}
	
	public Bitmap getImageByte()
	{
		return BitmapFactory.decodeByteArray(ImageByte, 0, ImageByte.length);
	}

	public void setImageByte(Bitmap imageByte)
	{
		if(imageByte==null){
			this.ImageByte=null;
		}else {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			imageByte.compress(Bitmap.CompressFormat.JPEG, 100, baos);
			System.out.println(baos);
			this.ImageByte = baos.toByteArray();
		}
	}

	public int getMessageType()
	{
		return messageType;
	}

	public void setMessageType(int messageType)
	{
		this.messageType = messageType;
	}
}
