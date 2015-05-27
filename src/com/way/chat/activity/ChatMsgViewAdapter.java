package com.way.chat.activity;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

/**
 * 消息ListView的Adapter
 * 
 * @author way
 */
public class ChatMsgViewAdapter extends BaseAdapter
{
	private Bitmap myImg;
	private Bitmap friendImg;
	private static final float scale = 0.5f;
	public static interface IMsgViewType
	{
		int IMVT_COM_MSG = 0;// 收到对方的消息
		int IMVT_TO_MSG = 1;// 自己发送出去的消息
	}

	private static final int ITEMCOUNT = 2;// 消息类型的总数
	private List<ChatMsgEntity> coll;// 消息对象数组
	private LayoutInflater mInflater;
	private Context context;

	public ChatMsgViewAdapter(Context context, List<ChatMsgEntity> coll)
	{
		this.coll = coll;
		mInflater = LayoutInflater.from(context);
		this.context = context;
	}

	public int getCount()
	{
		return coll.size();
	}

	public Object getItem(int position)
	{
		return coll.get(position);
	}

	public long getItemId(int position)
	{
		return position;
	}

	/**
	 * 得到Item的类型，是对方发过来的消息，还是自己发送出去的
	 */
	public int getItemViewType(int position)
	{
		ChatMsgEntity entity = coll.get(position);

		if (entity.isComMsg())
		{// 收到的消息
			return IMsgViewType.IMVT_COM_MSG;
		} else
		{// 自己发送的消息
			return IMsgViewType.IMVT_TO_MSG;
		}
	}

	/**
	 * Item类型的总数
	 */
	public int getViewTypeCount()
	{
		return ITEMCOUNT;
	}

	public View getView(int position, View convertView, ViewGroup parent)
	{

		final ChatMsgEntity entity = coll.get(position);
		boolean isComMsg = entity.isComMsg();

		ViewHolder viewHolder = null;
		if (convertView == null)
		{
			viewHolder = new ViewHolder();
			if (isComMsg)
			{
				convertView = mInflater.inflate(
						R.layout.chatting_item_msg_text_left, null);
				viewHolder.icon = (ImageView) convertView
						.findViewById(R.id.userImgVwInL);
				Drawable drawable = new BitmapDrawable(friendImg);
				viewHolder.icon.setImageDrawable(drawable);

			} else
			{
				convertView = mInflater.inflate(
						R.layout.chatting_item_msg_text_right, null);
				viewHolder.icon = (ImageView) convertView
						.findViewById(R.id.userImgVwInR);
				Drawable drawable = new BitmapDrawable(myImg);
				viewHolder.icon.setImageDrawable(drawable);
			}

			
			viewHolder.tvUserName = (TextView) convertView
					.findViewById(R.id.tv_username);
			viewHolder.tvContent = (TextView) convertView
					.findViewById(R.id.tv_chatcontent);

			viewHolder.isComMsg = isComMsg;

			convertView.setTag(viewHolder);
		} else
		{
			viewHolder = (ViewHolder) convertView.getTag();
		}
		viewHolder.tvContent.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				if (entity.getMsgType() == ChatMsgEntity.MSG_TYPE_VIOCE)
				{
					MediaPlayer mPlayer = MediaPlayer.create(context,
							Uri.parse(entity.getPath()));
					System.out.println("tvContent onclick getPath="
							+ entity.getPath());
					mPlayer.start();
				} else if (entity.getMsgType() == ChatMsgEntity.MSG_TYPE_IMAGE)
				{
					Bitmap bmp = BitmapFactory.decodeFile(entity.getPath());
					bmp = zoomBitmap(bmp,scale);
					System.out.println("tvContent onclick getPath="
							+ entity.getPath());
					MyDialog dialog = new MyDialog(context, bmp);
					dialog.show();

				} else
					Toast.makeText(context, entity.getMessage(),
							Toast.LENGTH_SHORT).show();
			}
		});
		viewHolder.tvUserName.setText(entity.getName());
		int type = entity.getMsgType();
		if (type == 0)
			viewHolder.tvContent.setText(entity.getMessage());
		else if (type == 1)
		{
			MediaPlayer mp = MediaPlayer.create(context,
					Uri.parse(entity.getPath()));
			System.out.println("entity.getPath=" + entity.getPath());
			int duration = mp.getDuration();
			viewHolder.tvContent.setText(")))" + duration);
		} else
		{
			Drawable drawable = new BitmapDrawable(entity.getImageByte());
			viewHolder.tvContent.setText("点击查看图像");
		}
		return convertView;
	}
	public Bitmap zoomBitmap(Bitmap bitmap ,float scale) 
	{
		Matrix matrix = new Matrix();
		matrix.postScale(scale, scale);// 利用矩阵进行缩放不会造成内存溢出

		Bitmap newbmp = Bitmap.createBitmap(bitmap, 0, 0
				, bitmap.getWidth(), bitmap.getHeight(), matrix, true);

		return newbmp;
	}  
	public Bitmap getFriendImg()
	{
		return friendImg;
	}

	public void setFriendImg(Bitmap friendImg)
	{
		this.friendImg = friendImg;
	}

	public Bitmap getMyImg()
	{
		return myImg;
	}

	public void setMyImg(Bitmap myImg)
	{
		this.myImg = myImg;
	}

	static class ViewHolder
	{
		public TextView tvUserName;
		public TextView tvContent;
		public ImageView image;
		public ImageView icon;
		public boolean isComMsg = true;
	}

}
