package com.markbuikema.juliana32.server.tools;

import java.io.IOException;
import java.nio.charset.Charset;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;


public class Tools {

	public static String getContent(String theURL) {

		HttpClient client = new DefaultHttpClient();
		HttpGet get = new HttpGet(theURL);
			HttpResponse response;
			try {
				response = client.execute(get);
				return EntityUtils.toString(response.getEntity(), Charset.forName("UTF-8"));

			} catch (IOException e) {
				e.printStackTrace();
			}
		
		return null;

	}

}
