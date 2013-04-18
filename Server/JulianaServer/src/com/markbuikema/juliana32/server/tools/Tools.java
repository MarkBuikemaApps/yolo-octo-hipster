package com.markbuikema.juliana32.server.tools;

import java.io.IOException;

import org.jsoup.Connection;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;

public class Tools {

	public static String getContent(String theURL) {

//		HttpClient client = new DefaultHttpClient();
//		HttpGet get = new HttpGet(theURL);
//		try {
//			HttpResponse response = client.execute(get);
//			return EntityUtils.toString(response.getEntity());
//		} catch (ClientProtocolException e) {
//		} catch (IOException e) {
//		}
//		return null;
		
		Connection c = Jsoup.connect(theURL);
		try {
			Response r = c.execute();
			String charset = r.charset();
			String response = new String(r.body().getBytes(charset), charset);
//			response = response.replaceAll("â€™", "'");
//			response = response.replaceAll("â€œ", "\"");
//			response = response.replaceAll("â€�", "\"");
			return response;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;

	}

}
