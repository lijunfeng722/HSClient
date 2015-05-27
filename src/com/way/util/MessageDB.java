package com.way.util;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import com.way.chat.activity.ChatMsgEntity;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class MessageDB {
	private SQLiteDatabase db;

	public MessageDB(Context context) {
		db = context.openOrCreateDatabase(Constants.DBNAME,
				Context.MODE_PRIVATE, null);
	}

	public void saveMsg(int id, ChatMsgEntity entity, int idFrom) {
		
		
		db.execSQL("CREATE table IF NOT EXISTS _"
				
				+ id
				+ " (_id INTEGER PRIMARY KEY AUTOINCREMENT,name TEXT, img MEDIUMBLOB,date TEXT,isCome TEXT,"
				+ "message TEXT, idFrom TEXT,messageType INTEGER,path TEXT)");
		int isCome = 0;
		if (entity.isComMsg()) {//如果是收到的消息，保存在数据库的值为1
			isCome = 1;
		}
		Bitmap bitmap=entity.getImg();
		ByteArrayOutputStream oss = new ByteArrayOutputStream();  
		// 将Bitmap压缩成PNG编码，质量为100%存储  
		if(bitmap!=null)
		bitmap.compress(Bitmap.CompressFormat.PNG, 100, oss);//除了PNG还有很多常见格式，如jpeg等
		db.execSQL(
				"insert into _" + id
						+ " (name,img,date,isCome,message,idFrom,messageType,path) values(?,?,?,?,?,?,?,?)",
				new Object[] { entity.getName(), oss.toByteArray(),
						entity.getDate(), isCome, entity.getMessage(),idFrom,entity.getMsgType(),entity.getPath()});
	}

	public List<ChatMsgEntity> getMsg(int id) {
	/*	db.execSQL("drop table _2038 ");
		db.execSQL("drop table _2039");
		db.execSQL("drop table _2018 ");
		db.execSQL("drop table _2019 ");
		db.execSQL("drop table _2029 ");
		db.execSQL("drop table _2030 ");
		db.execSQL("drop table _2031 ");
		db.execSQL("drop table _2032 ");*/
		List<ChatMsgEntity> list = new ArrayList<ChatMsgEntity>();
		db.execSQL("CREATE table IF NOT EXISTS _"
				+ id
				+ " (_id INTEGER PRIMARY KEY AUTOINCREMENT,name TEXT, img MEDIUMBLOB,date TEXT,isCome TEXT,"
				+ "message TEXT, idFrom TEXT,messageType INTEGER,path TEXT)");
		Cursor c = db.rawQuery("SELECT * from _" + id + " ORDER BY _id DESC LIMIT 5", null);
		while (c.moveToNext()) {

			Bitmap img=null;
			String name = c.getString(c.getColumnIndex("name"));
			byte[] temp =  c.getBlob(c.getColumnIndex("img"));
			if(temp.length!=0)
				img = BitmapFactory.decodeByteArray(temp,0,temp.length);
			String date = c.getString(c.getColumnIndex("date"));
			int isCome = c.getInt(c.getColumnIndex("isCome"));
			String message = c.getString(c.getColumnIndex("message"));
			boolean isComMsg = false;
			if (isCome == 1) {
				isComMsg = true;
			}
			int idFrom = c.getInt(c.getColumnIndex("idFrom"));
			int messageType = c.getInt(c.getColumnIndex("messageType"));
			System.out.println("yeyeye");
			String path = c.getString(c.getColumnIndex("path"));
			System.out.println("yeyeye");
			ChatMsgEntity entity = new ChatMsgEntity(name, date, message, img,
					isComMsg,idFrom,messageType,path);
			list.add(entity);
		}
		c.close();
		System.out.println("yeyeye");
		return list;
	}

	public void close() {
		if (db != null)
			db.close();
	}
}
