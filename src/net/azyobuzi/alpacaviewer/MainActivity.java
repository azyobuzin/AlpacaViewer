package net.azyobuzi.alpacaviewer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class MainActivity extends ListActivity {
	private ArrayList<RankingItem> items;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.activity_main);
		new RankingAsyncTask().execute();
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		
		if (items != null) {
			RankingItem item = items.get(position);
			startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://twitter.com/" + item.name)));
		}
	}
	
	class RankingAsyncTask extends AsyncTask<Void, Void, JSONArray> {
		@Override
		protected void onPreExecute() {
			setProgressBarIndeterminateVisibility(true);
		};
		
		@Override
		protected JSONArray doInBackground(Void... params) {
			try {
				DefaultHttpClient client = new DefaultHttpClient();
				HttpResponse res = client.execute(new HttpGet("https://alpacabokujodata.azure-mobile.net/api/alpacaapi"));
				return new JSONArray(EntityUtils.toString(res.getEntity()));
			} catch(Exception e) {
				e.printStackTrace();
				return null;
			}
		}
		
		@Override
		protected void onPostExecute(JSONArray result) {
			if (result == null) {
				new AlertDialog.Builder(MainActivity.this)
					.setMessage("Azure 落ちてんじゃねーの？")
					.show();
			} else {
				items = new ArrayList<RankingItem>();
				for(int i = 0; i < result.length(); i++) {
					try {
						JSONObject obj = result.getJSONObject(i);
						RankingItem item = new RankingItem();
						item.name = obj.getString("name");
						item.level = obj.getInt("value");
						items.add(item);
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
				Collections.sort(items, new Comparator<RankingItem>() {
					@Override
					public int compare(RankingItem arg0, RankingItem arg1) {
						return -arg0.level.compareTo(arg1.level);
					}
				});
				setListAdapter(new RankingAdapter(items));
			}
			
			setProgressBarIndeterminateVisibility(false);
		}
	}
	
	class RankingAdapter extends ArrayAdapter<RankingItem> {
		public RankingAdapter(List<RankingItem> objects) {
			super(MainActivity.this, android.R.layout.simple_list_item_2, android.R.id.text1, objects);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = super.getView(position, convertView, parent);
			RankingItem obj = getItem(position);
			((TextView)view.findViewById(android.R.id.text1)).setText("@" + obj.name);
			((TextView)view.findViewById(android.R.id.text2)).setText("レベル " + obj.level);
			return view;
		}
	}
}
