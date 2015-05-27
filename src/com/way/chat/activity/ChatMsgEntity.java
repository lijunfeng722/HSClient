package com.way.chat.activity;

import java.io.ByteArrayOutputStream;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

/**
 * 一个聊天消息的JavaBean
 * 
 * @author way
 * 
 */
public class ChatMsgEntity
{
	public static final int MSG_TYPE_TEXT = 0;
	public static final int MSG_TYPE_VIOCE = 1;
	public static final int MSG_TYPE_IMAGE = 2;
	private String name;// 消息来自
	private String date;// 消息日期
	private String message;// 消息内容
	private String path;
	private Bitmap img;
	private boolean isComMeg = true;// 是否为收到的消息
	private int idFrom; // 谁发送的消息
	private int msgType ;
	private byte[] voiceByte;
	private byte[] imageByte;

	public ChatMsgEntity()
	{

	}

	public ChatMsgEntity(String name, String date, String text, Bitmap img,
			boolean isComMsg, int idFrom)
	{
		super();
		this.name = name;
		this.date = date;
		this.message = text;
		this.img = img;
		this.isComMeg = isComMsg;
		this.idFrom = idFrom;
	}

	public ChatMsgEntity(String name, String date, String text, Bitmap img,
			boolean isComMsg, int idFrom, int msgType, String path)
	{
		this(name, date, text, img, isComMsg, idFrom);
		this.msgType = msgType;
		this.path=path;
		
	}

	public int getIdFrom()
	{
		return idFrom;
	}

	public void setIdFrom(int idFrom)
	{
		this.idFrom = idFrom;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getDate()
	{
		return date;
	}

	public void setDate(String date)
	{
		this.date = date;
	}

	public String getMessage()
	{
		return message;
	}

	public void setMessage(String message)
	{
		this.message = message;
	}

	public boolean isComMsg()
	{
		return isComMeg;
	}
	
	public int getMsgType() {
		return msgType;
	}
	
	public String getPath(){
		return path;
	}

	public void setIsComMsg(boolean isComMsg)
	{
		isComMeg = isComMsg;
	}

	public void setPath(String path){
		this.path=path;
	}
	
	public Bitmap getImg() {
		return img;
	}

	public void setImg(Bitmap img) {
		this.img = img;
	}

	public Bitmap getImageByte()
	{
		if(imageByte != null)
			return BitmapFactory.decodeByteArray(imageByte, 0, imageByte.length);
		else
			return null;
	}

	public void setImageByte(Bitmap image)
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();    
		image.compress(Bitmap.CompressFormat.JPEG, 100, baos);
		System.out.println(baos);
		this.imageByte = baos.toByteArray();
	}
	public void setMsgType(int msgType)
	{
		this.msgType = msgType;
		switch (this.msgType)
		{
		case MSG_TYPE_TEXT:
			break;
		case MSG_TYPE_VIOCE:
			break;
		case MSG_TYPE_IMAGE:
			break;
		}
	}

	public byte[] getVoiceByte()
	{
		return voiceByte;
	}

	public void setVoiceByte(byte[] voiceByte)
	{
		this.voiceByte = voiceByte;
	}

}
