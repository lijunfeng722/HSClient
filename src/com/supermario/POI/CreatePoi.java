package com.supermario.POI;

import android.content.Context;
import android.os.Handler;
import com.way.chat.activity.R;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;


public class CreatePoi {
	// optional field
	// private static String[] opt_field = { "title", "address", "tags", "sn"};
	private static int iUrl = R.string.url_create_poi;



	public static void create(String title,String latitude, String longitude,
			String coord_type, String geotable_id, String ak,
			Handler handler, int what, Context context) {
		List<NameValuePair> list = new ArrayList<NameValuePair>();
		list.add(new BasicNameValuePair("title", title));
		list.add(new BasicNameValuePair("latitude", latitude));
		list.add(new BasicNameValuePair("longitude", longitude));
		list.add(new BasicNameValuePair("geotable_id", geotable_id));
		list.add(new BasicNameValuePair("coord_type", coord_type));
		list.add(new BasicNameValuePair("ak", ak));
		HttpUtil.HttpPostRequest(context.getResources().getString(iUrl),
				handler, what, list);
	}
}
