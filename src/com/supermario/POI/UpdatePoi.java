package com.supermario.POI;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import com.way.chat.activity.R;


import android.content.Context;
import android.os.Handler;


public class UpdatePoi {
	// optional field
	// private static String[] opt_field = { "title", "tags", "address", "sn" };
	private static int iUrl = R.string.url_update_poi;

	public static void update(String id, String geotable_id, String coord_type,
			Map<String, String> map, Handler handler, int what, Context context) {
		update(id, geotable_id, coord_type,
				context.getResources().getString(R.string.ak), map, handler,
				what, context);
	}

	public static void update(String id, String geotable_id, String coord_type,
			String ak, Map<String, String> map, Handler handler, int what,
			Context context) {
		List<NameValuePair> list = new ArrayList<NameValuePair>();
		list.add(new BasicNameValuePair("id", id));
		list.add(new BasicNameValuePair("geotable_id", geotable_id));
		list.add(new BasicNameValuePair("coord_type", coord_type));
		list.add(new BasicNameValuePair("ak", ak));
		Iterator<Entry<String, String>> it = map.entrySet().iterator();
		while (it.hasNext()) {
			Entry<String, String> entry = it.next();
			String key = entry.getKey();
			String value = entry.getValue();
			list.add(new BasicNameValuePair(key, value));
		}
		HttpUtil.HttpPostRequest(context.getResources().getString(iUrl),
				handler, what, list);
	}
}
