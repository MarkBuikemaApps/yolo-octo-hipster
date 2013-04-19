package com.markbuikema.juliana32.tools;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.graphics.Bitmap;

public class Tools {

	private static Bitmap[] teletekst;

	public static void putTeletekst(Bitmap[] bmps) {
		teletekst = bmps;
	}

	public static Bitmap getTeletekst(int index) {
		if (index >= 0 && teletekst.length > index)
			return teletekst[index];
		else
			return null;
	}
	
	public static String getHttpContent(String url) {
		HttpClient client = new DefaultHttpClient();
		HttpGet get = new HttpGet(url);
		try {
			HttpResponse response = client.execute(get);
			return EntityUtils.toString(response.getEntity());
		} catch (ClientProtocolException e){} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

}
