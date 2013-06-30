package com.conchord.android.network.rest;

import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.content.Context;
import android.util.Log;

import com.conchord.android.util.SafeAsyncTask;
import com.conchord.android.util.Utils;

public class BasePutRequestAsyncTask extends SafeAsyncTask {

	static final String BASE_URL = "http://conchordapp.appspot.com";

	protected String responseString = "";

	protected List<NameValuePair> parameters = null;

	private String uri = null;

	protected Context context;

	protected BasePutRequestAsyncTask(Context context, String uriSuffix,
			List<NameValuePair> parameters) {
		super();
		this.uri = BASE_URL + uriSuffix;
		this.parameters = parameters;
		this.context = context;
	}

	@Override
	protected void onPreExecute() {
		// Verify there is an Internet connection
		if (!Utils.isNetworkAvailable(context)) {
			// If there is no Internet connection, then don't run the AsyncTask.
			cancel(true);
		}
	}

	@Override
	public Object call() throws Exception {

		HttpClient client = new DefaultHttpClient();
		HttpPut putRequest = new HttpPut(uri);

		// Add the parameters in.
		if (parameters != null) {
			putRequest.setEntity(new UrlEncodedFormEntity(parameters));
		}

		Log.v("BasePutRequestAsyncTask.call", "Sending PUT request with URI: "
				+ uri);
		String responseString = EntityUtils.toString(client.execute(putRequest)
				.getEntity());

		if (responseString != null) {
			Log.v("BasePutRequestAsyncTask.call", "Got HTTP result: "
					+ responseString);
		} else {
			throw new Exception("PUT request receieved null response string.");
		}

		/*
		 * Save the responseString internally, for inheriting classes to use (e.g.
		 * most classes will parse this string).
		 */
		this.responseString = responseString;

		return null;
	}
	
	@Override
	protected void onException(Exception e) throws RuntimeException {
		super.onException(e);
	}

}
