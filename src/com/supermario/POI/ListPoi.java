package com.supermario.POI;

import java.util.Map;

import com.way.chat.activity.R;


import android.content.Context;
import android.os.Handler;


public class ListPoi {
	// optional field
	private static String[] opt_field = { "title", "tags", "bounds", "sn", };
	private static int iUrl = R.string.url_list_poi;

	public static void list(String geotable_id, Handler handler, Context context) {
		list(geotable_id, context.getResources().getString(R.string.ak),
				handler, 0, context);
	}

	public static void list(String geotable_id, Handler handler, int what,
			Context context) {
		list(geotable_id, context.getResources().getString(R.string.ak),
				handler, what, context);
	}

	public static void list(String geotable_id, String ak, Handler handler,
			int what, Context context) {
		StringBuilder sb = new StringBuilder();
		sb.append(context.getResources().getString(iUrl));
		sb.append("?");
		sb.append("ak=").append(ak);
		sb.append("&geotable_id=").append(geotable_id);
		HttpUtil.HttpGetRequest(sb.toString(), handler, what);
	}

	public static void list(String geotable_id, Map<String, String> map,
			Handler handler, Context context,int what) {
		list(geotable_id, context.getResources().getString(R.string.ak), map,
				handler, context,what);
	}

	public static void list(String geotable_id, String ak,
			Map<String, String> map, Handler handler, Context context,int what) {
		StringBuilder sb = new StringBuilder();
		sb.append(context.getResources().getString(iUrl));
		sb.append("?");
		sb.append("ak=").append(ak);
		sb.append("&geotable_id=").append(geotable_id);
		for (String field : opt_field) {
			if (map.containsKey(field))
				sb.append("&").append(field).append("=").append(map.get(field));
		}
		HttpUtil.HttpGetRequest(sb.toString(), handler,what);
	}
}
