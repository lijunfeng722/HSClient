package com.supermario.POI;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

public class HttpUtil {
	public static boolean isNetworkAvailable(Context context) {
		ConnectivityManager connectivity = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (connectivity == null) {
			return false;
		} else {
			NetworkInfo info = connectivity.getActiveNetworkInfo();
			if (info == null) {
				return false;
			} else {
				if (info.isAvailable()) {
					return true;
				}
			}
		}
		return false;
	}

	public static void HttpGetRequest(final String url,
			final Handler handler_net) {
		HttpGetRequest(url, handler_net, 0);
	}

	public static void HttpGetRequest(final String url,
			final Handler handler_net, final int what) {
		new Thread() {
			public void run() {
				try {
					// �õ�HttpClient����
					HttpClient getClient = new DefaultHttpClient();

					// �õ�HttpGet����
					getClient.getParams().setParameter(
							CoreConnectionPNames.CONNECTION_TIMEOUT, 20000);// ����ʱ��
					getClient.getParams().setParameter(
							CoreConnectionPNames.SO_TIMEOUT, 20000);// ��ݴ�
					HttpGet request = new HttpGet(url);
					// �ͻ���ʹ��GET��ʽִ����̣���÷������˵Ļ�Ӧresponse
					HttpResponse response = getClient.execute(request);
					// �ж������Ƿ�ɹ�
					if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
						String result = EntityUtils.toString(response
								.getEntity());
//						System.out.print(result);
						// �ر�������
						// inStrem.close();
						Bundle bundle = new Bundle();
						bundle.putString("result", result.trim()); // ���trim(),ȥ��ĩβ���з�
						Message msg = new Message();
						msg.what = what;
						msg.setData(bundle);
						handler_net.sendMessage(msg);
					} else {
						handler_net.sendEmptyMessage(-1);
					}
				} catch (Exception e) {
					e.printStackTrace();
					handler_net.sendEmptyMessage(-1);
				}
			}
		}.start();
	}

	public static void HttpPostRequest(final String url,
			final Handler handler_net, final List<NameValuePair> postParameters) {
		HttpPostRequest(url, handler_net, 0, postParameters);
	}

	public static void HttpPostRequest(final String url,
			final Handler handler_net, final int what,
			final List<NameValuePair> postParameters) {
		new Thread() {
			public void run() {
				BufferedReader in = null;
				try {
					HttpClient client = new DefaultHttpClient();
					client.getParams().setParameter(
							CoreConnectionPNames.CONNECTION_TIMEOUT, 20000);// ����ʱ��
					client.getParams().setParameter(
							CoreConnectionPNames.SO_TIMEOUT, 20000);// ��ݴ�
					HttpPost request = new HttpPost(url);
					// �޸ı����ʽ,�����������
					request.addHeader("charset", HTTP.UTF_8);
					// ʵ��UrlEncodedFormEntity����
					// �޸ı����ʽ,�����������
					UrlEncodedFormEntity formEntity = new UrlEncodedFormEntity(
							postParameters, HTTP.UTF_8);
					// ʹ��HttpPost����������UrlEncodedFormEntity��Entity
					request.setEntity(formEntity);
					HttpResponse response = client.execute(request);
					in = new BufferedReader(new InputStreamReader(response
							.getEntity().getContent()));

					StringBuffer string = new StringBuffer("");
					String lineStr = "";
					while ((lineStr = in.readLine()) != null) {
						string.append(lineStr + "\n");
					}
					in.close();
					String resultStr = string.toString();
//					System.out.println(resultStr);

					Bundle bundle = new Bundle();
					bundle.putString("result", resultStr.trim()); // ���trim()ȥ��ĩβ��Ϣ
					Message msg = new Message();
					msg.what = what;
					msg.setData(bundle);
					handler_net.sendMessage(msg);
				} catch (Exception e) {
					handler_net.sendEmptyMessage(-1);
					// Do something about exceptions
				} finally {
					if (in != null) {
						try {
							in.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}.start();
	}

}