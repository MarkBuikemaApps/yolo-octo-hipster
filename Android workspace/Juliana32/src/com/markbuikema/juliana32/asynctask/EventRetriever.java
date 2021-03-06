package com.markbuikema.juliana32.asynctask;

import org.json.JSONArray;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.os.Bundle;

import com.facebook.Request;
import com.facebook.Request.Callback;
import com.facebook.Response;
import com.markbuikema.juliana32.model.Event;
import com.markbuikema.juliana32.model.NormalEvent;
import com.markbuikema.juliana32.util.FacebookHelper;

public class EventRetriever extends AsyncTask<Void, Event, Void> {

	@Override
	protected Void doInBackground(Void... arg0) {
		Request request = Request.newGraphPathRequest(null, FacebookHelper.JULIANA_ID + "/events", new Callback() {

			@Override
			public void onCompleted(Response response) {
				try {
					JSONObject obj = response.getGraphObject().getInnerJSONObject();
					JSONArray data = obj.getJSONArray("data");
					for (int i = 0; i < data.length(); i++) {
						JSONObject eventJSON = data.getJSONObject(i);
						String name = eventJSON.getString("name");
						String startDate = eventJSON.getString("start_time");
						String endDate = eventJSON.getString("end_time");
						String location = eventJSON.getString("location");
						int id = Integer.valueOf(eventJSON.getString("id"));

						publishProgress(new NormalEvent(id, startDate, endDate, name, location));
					}
				} catch (Exception e) {
				}
			}
		});
		Bundle params = new Bundle();
		params.putString("access_token", FacebookHelper.ACCESS_TOKEN);
		request.setParameters(params);

		request.executeAndWait();

		return null;
	}

}
