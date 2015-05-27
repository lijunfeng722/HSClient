package com.way.chat.activity;


import com.way.chat.common.bean.User;
import com.way.chat.common.tran.bean.TranObject;
import com.way.chat.common.tran.bean.TranObjectType;
import com.way.client.Client;
import com.way.client.ClientOutputThread;
import com.way.util.Constants;
import com.way.util.DialogFactory;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;

public class PinTuActy extends MyActivity 
{

	private  GameView           game;
	private  boolean            isRunning;         ///< ��Ϸ�Ƿ�ʼ
	private MyApplication application;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.game_acty);
		
		application = (MyApplication) this.getApplicationContext();
		DisplayMetrics metric = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metric);
		int width = metric.widthPixels;//��Ļ��ȣ����أ�
		
		game   = new GameView(this,width);
		
		RelativeLayout gameLayout = (RelativeLayout)findViewById(R.id.gameView);
		gameLayout.addView(game);
		Button back_btn     = (Button) findViewById(R.id.back_btn);  
		Button start = (Button) findViewById(R.id.start_btn);
		
		back_btn.setOnClickListener(new Button.OnClickListener()
		{  
			public void onClick(View v)  
			{  
				onBackPressed();
			}  
		});
		
		start.setOnClickListener(new Button.OnClickListener()
		{  
			@Override
			public void onClick(View v)  
			{  
				game.initMap();
				game.invalidate();
			}  
		});
		
	}
	@Override
	public void  onBackPressed()                            //��׽���ؼ�
	{
		isRunning = false;
		   
		Intent intent = new Intent();                       // �½�һ��Intent����
		intent.setClass(PinTuActy.this, MapActy.class);// ָ��intentҪ��������   
		startActivity(intent);                              //����һ���µ�Activity   
		finish();                                           //�رյ�ǰ��Activity 
	}
	
	@SuppressLint("HandlerLeak")
	class GameView extends View implements Runnable
	{	
		private  static final int   MSG_UPDATE = 0x123;///< handler���ݵ���Ϣ����
		private  int                btmpWidth;     ///< �ָ��ÿ��ͼƬ�Ŀ��
		private  int                btmpHeight;    ///< �ָ��ÿ��ͼƬ�ĸ߶�
		private  int                dimension;     ///< ƴͼ��ά��
		private  int                steps;         ///< ��¼�ƶ��Ĳ���
		private  int                playSeconds;   ///< ��¼��Ϸ����ʱ��
		private  int[][]            map;           ///< ƴͼ�ĵ�ͼ
		private  float              scale;         ///< ������
		private  boolean            isSuccess;     ///< �Ƿ�ƴͼ�ɹ�
		private  PointF             point;         ///< ��¼���һ���ͼƬλ��
		private  Bitmap             bitmap;        ///< ԭʼͼƬ
		private  Bitmap             resizeBmp;     ///< ����ض���Ļ���ź��ͼƬ
		private  Paint              paint;         ///< ����
		private  Matrix             mat;           ///< �任����
		private  Bitmap[]           img;           ///< ͼ������
		private  Context            context;       ///< �����ģ������л�ʱҪ�õ�Ŷ
		private  RefreshHandler     mRefreshHandler;//< �Զ�����Ϣ���
		
		public GameView(Context context, int screenWidth)
		{
			super(context);
			this.context = context;
			bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.gg); //װ��ͼƬ
				
			steps       = 0;
			dimension   = 3;      //Ĭ��Ϊ3X3
			playSeconds = 0; 			
			isSuccess = false;
			isRunning = false;
			
			map    = new int[4][4];
			paint  = new Paint();
			point  = new PointF();
			mat    = new Matrix();
			img    = new Bitmap[16];
			mRefreshHandler = new RefreshHandler();
			
			new Thread(this).start();
			
			scale  =(float)screenWidth / bitmap.getWidth();		
			mat.postScale(scale, scale);
			
			resizeBmp = Bitmap.createBitmap(bitmap,0,0,bitmap.getWidth(),bitmap.getHeight(),mat,true);   
			
			btmpWidth  = resizeBmp.getWidth()  / dimension;  //ͼƬ�ָ��ÿһ��ͼƬ�Ŀ��
			btmpHeight = resizeBmp.getHeight() / dimension;  //ͼƬ�ָ��ÿһ��ͼƬ�ĸ߶�
			mat.postScale(0.2f, 0.2f);
			mat.postTranslate(btmpWidth*5/4,resizeBmp.getHeight()+5);//���þ����λ��
			int i,j;
			for (i=0; i<dimension; i++)
			{
				for (j=0; j<dimension; j++)
				{
					img[i*dimension+j] = Bitmap.createBitmap(resizeBmp, j*btmpWidth, i*btmpHeight, btmpWidth, btmpHeight);
				}
			}
			initMap();	
		}
	
		@Override
		public void onDraw(Canvas canvas) 
		{
			super.onDraw(canvas);
		    int row,col;
			int x,y;
			paint.setTextSize(60);			
	//		paint.setColor(Color.CYAN);   //������ɫΪ��ɫ
			
			if (isSuccess)
			{
				canvas.drawBitmap(resizeBmp, 0, 0, paint);
			}
			else
			{
				for (x = 0; x < dimension; x++)
				{
					for (y = 0; y < dimension; y++)
					{
						if(map[x][y] != dimension*dimension-1) 
						{
		    				row = map[x][y] / dimension;
		    				col = map[x][y] % dimension;
		    				canvas.save();
		    				canvas.drawBitmap(img[row*dimension+col],y*btmpWidth, x*btmpHeight, paint);
		    				canvas.restore();
						}
					}
				}
			}//else
	//		canvas.drawText(""+playSeconds, 50, resizeBmp.getHeight()+70, paint);
		    canvas.drawText(""+steps, btmpWidth*2+50, resizeBmp.getHeight()+70, paint);
		    canvas.drawBitmap(resizeBmp, mat, paint);
		 }
		@Override
		public boolean onTouchEvent(MotionEvent event) 
		{
			switch (event.getAction())
			{
			case MotionEvent.ACTION_DOWN:
				//ȡ�ð��µ�xy���
				
			    int y = (int)event.getX() / btmpWidth;
				int x = (int)event.getY() / btmpHeight;
				int finalx = (int)point.x;           //��ȱ���λ��
				int finaly = (int)point.y;
				int canMove = Math.abs(finalx - x) + Math.abs(finaly - y); 
				if (canMove!=1)
				{
					break;
				}
				steps++;
				isRunning = true;
				
				map[finalx][finaly] = map[x][y];   //���ͼƬ��� ����ھſ�ͼƬ
			    map[x][y] = dimension*dimension-1; //��ȱ��ͼƬ��� ���ͼƬ
				point.set(x, y);    
				int count=0;               //ƴͼƴ�Ե�ͼƬ����
			    for (x=0; x < dimension; x++)
			    {
			        for (y=0; y < dimension; y++)
			        {
			        	if (map[x][y]==x*dimension+y)
			        	{
			        		count++;   
			        	}
			        }
			     }
			    if (count==dimension*dimension)
			    {
			    	//ƴͼ�ɹ�������
			    	isSuccess = true;
			    	isRunning = false;
			    	SuccessDialog(context,"解锁成功!","移动了" +playSeconds+"步");
			    }
				this.invalidate();
				break;
			}
			return true;
		}
		
		public void initMap()
	    {
			steps       = 0;
			playSeconds = 0; 
	    	long time = System.nanoTime();        //�õ�ϵͳ��ǰʱ�� ��Ϊ���������
	        java.util.Random rnd = new java.util.Random(time);
	        int temp, x1, y1, x2, y2;
	        int i,j;
	        for (i=0; i<dimension; i++)
	        {
	        	for (j=0; j<dimension; j++)
	        	{
	        		map[i][j]=i*dimension+j;
	           	}
	        }
	        //������ͼƬ�໥����
	        //���ͼƬ
	        for (i = 0; i < 50; i++)
	        {
	        	x1 = rnd.nextInt(dimension);
	        	x2 = rnd.nextInt(dimension);
	        	y1 = rnd.nextInt(dimension);
	        	y2 = rnd.nextInt(dimension);
	        	temp = map[x1][y1];
	        	map[x1][y1] = map[x2][y2];
	       		map[x2][y2] = temp;
	       	}    
	     
	       	for (i = 0; i < dimension; i++)
	        {
	        	for (j = 0; j < dimension; j++)
	        	{
	        		if (map[i][j] == dimension*dimension-1) //ȡ���½�ͼƬΪ��ɫͼƬ
	        		{
	        			point.set(i, j);    //��¼��ɫͼƬ��λ��
	        		}
	        	}		
	        }//for
	    }
		public void run()
		{
			while (!Thread.currentThread().isInterrupted())
			{
				if (isRunning)
				{
					Message m = new Message();
					m.what = MSG_UPDATE;
					mRefreshHandler.sendMessage(m);//�����̷߳�����Ϣ
					try
					{
						Thread.sleep(1000);
						playSeconds++;
					}
					catch (InterruptedException e) 
					{
						e.printStackTrace();
					}
					
				}
			}
		}
		/*****************************
		 * ƴͼ�ɹ�ʱ����ʾ��
		 * 
		 * @param context
		 *            �����Ķ���
		 * @param title
		 *            ����
		 * @param msg
		 *            ����
		 *****************************/
		private void SuccessDialog(final Context context, String title, String msg) 
		{
			new AlertDialog.Builder(context).setTitle(title).setMessage(msg)
					.setPositiveButton("确定", new DialogInterface.OnClickListener() 
					{
						@Override
						public void onClick(DialogInterface dialog, int which)
						{
							Client client = application.getClient();
							ClientOutputThread out = client.getClientOutputThread();
							 // �½�һ��Intent����   
							String ID = PinTuActy.this.getIntent().getStringExtra("ID");
							System.out.println("ID:"+ID);
							TranObject<User> o = new TranObject<User>(TranObjectType.FriendCheck);
							User u = new User();
							u.setId((new Integer(ID)).intValue());
							o.setObject(u);
							out.setMsg(o);
			        /*        Intent intent = new Intent(); 
			                intent.putExtra("ID",ID);
			                // ָ��intentҪ��������   
			                intent.setClass(context, FriendMsg.class);  
			                //����һ���µ�Activity   
			                startActivity(intent);  
			                //�رյ�ǰ��Activity   
			                finish();  */
						}
					}).setNegativeButton("取消", null).create().show();
		}	
		class RefreshHandler extends Handler
		{
			
			public void handlerMessage(Message msg)
			{
				if (msg.what==MSG_UPDATE)
				{
					GameView.this.invalidate();
				}
				super.handleMessage(msg);
			}
		}
	}		
	public void getMessage(TranObject msg) {
		if (msg != null) {
			System.out.println("Result:" + msg);
			switch (msg.getType()) {
			case FriendCheck:// LoginActivity只处理登录的消息
				User friend = (User) msg.getObject();
				System.out.println(friend);
					Intent intent = new Intent();  
	                intent.setClass(PinTuActy.this, StrangerMsgActy.class);  
	                intent.putExtra(Constants.MSGKEY, msg);
	                startActivity(intent); 
	                finish();
					
				} 
			}
		}
}
