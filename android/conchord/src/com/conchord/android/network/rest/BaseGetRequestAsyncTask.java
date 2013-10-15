package com.conchord.android.network.rest;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.content.Context;
import android.util.Log;

import com.conchord.android.util.Utils;

public class BaseGetRequestAsyncTask<ResultT> extends SafeAsyncTask<ResultT> {

	// TODO: Add base_url from app engine
	static final String BASE_URL = "http://conchordapp.appspot.com";

	protected String responseString = "";
	private String uri = null;
	private Context context;

	protected BaseGetRequestAsyncTask(Context context, String uriSuffix) {
		super();
		this.uri = BASE_URL + uriSuffix;
		this.context = context;
	}
	
	@Override
	protected void onPreExecute() {
		// Verify Internet connectivity
		if (!Utils.isNetworkAvailable(context)) {
			// If there is no Internet connection, then don't run the AsyncTask.
			cancel(true);
		}
	}

	@Override
	public ResultT call() throws Exception {

		HttpClient client = new DefaultHttpClient();
		HttpUriRequest getRequest = new HttpGet(uri);

		Log.v("BaseGetRequestAsyncTask.call", "Sending GET request with URI: "
				+ uri);

		// The actual network call
		String responseString = EntityUtils.toString(client.execute(getRequest)
				.getEntity());

		if (responseString != null) {
			Log.v("BaseGetRequestAsyncTask.call", "Got HTTP result: "
					+ responseString);
		} else {
			throw new Exception("GET request receieved null response string.");
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
