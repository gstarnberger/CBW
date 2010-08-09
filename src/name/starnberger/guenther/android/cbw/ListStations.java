// TODO: Use custom map instead of Google maps that approximates Pin location
// TODO: Fetch data directly from cbw.at instead of Yahoo Pipe (Pipe sometimes
// outdated)

package name.starnberger.guenther.android.cbw;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import name.starnberger.guenther.android.cbw.R;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

public class ListStations extends ListActivity {
	private ProgressDialog m_ProgressDialog = null;
	private ArrayList<Station> m_stations = null;
	private StationAdapter m_adapter;
	private Thread viewStationsThread;
	private Location curLocation;
	private LocationHelper locationHelper;
	private boolean splash = true;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// this.requestWindowFeature(Window.FEATURE_NO_TITLE);

		// this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
		// WindowManager.LayoutParams.FLAG_FULLSCREEN );

		// setContentView(R.layout.splash);

		// Temporary set full screen (will be removed when splash is removed)
		// setFullScreen(true);
		
		setContentView(R.layout.main);

		locationHelper = new LocationHelper(
				(LocationManager) getSystemService(LOCATION_SERVICE), Integer
						.parseInt(getString(R.string.max_age)), Integer
						.parseInt(getString(R.string.max_timeout)));

		m_stations = new ArrayList<Station>();
		this.m_adapter = new StationAdapter(this, R.layout.row, m_stations);
		setListAdapter(this.m_adapter);
		
		updateStations();
	}

	public Location getCurLocation() {
		return curLocation;
	}

	public LocationHelper getLocationHelper() {
		return locationHelper;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.resync:
			updateStations();
			return true;
		case R.id.about:
			Intent i = new Intent(this, About.class);
			startActivity(i);
			return true;
		}
		return false;
	}

	private void updateStations() {
		viewStationsThread = new Thread(null, new Runnable() {
			@Override
			public void run() {
				getStations();
			}
		});

		viewStationsThread.start();

		Thread getLocationThread = new Thread(null, new Runnable() {
			@Override
			public void run() {
				Location loc = getLocation();

				try {
					if (loc == null) {
						deleteEntries();
						UIshowErr(getString(R.string.error_no_gps));
					}
					viewStationsThread.join();
					if (loc != null) {
						runOnUiThread(returnRes);
					}
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			}
		});

		getLocationThread.start();

		m_ProgressDialog = ProgressDialog.show(ListStations.this,
				getString(R.string.progress_title),
				getString(R.string.progress_body), true);
	}

	// The station updater is a test class required to display the splash screen
	// private class StationUpdater extends AsyncTask<String, Void, Object> {
	// protected Void doInBackground(String... args) {
	//
	// // This is where you would do all the work of downloading your data
	//
	// updateStations();
	//
	// return "replace this with your data object";
	// }
	//
	// protected void onPostExecute(Object result) {
	// // Pass the result data back to the main activity
	// ListStations.this.data = result;
	//
	// if (MyActivity.this.pd != null) {
	// MyActivity.this.pd.dismiss();
	// }
	// }
	// }

	private void getStations() {
		HttpClient httpclient;
		HttpGet httpget;
		HttpResponse response;
		m_stations = new ArrayList<Station>();

		try {
			HttpParams httpParams = new BasicHttpParams();
			HttpConnectionParams.setConnectionTimeout(httpParams, Integer
					.parseInt(getString(R.string.max_timeout)));
			HttpConnectionParams.setSoTimeout(httpParams, Integer
					.parseInt(getString(R.string.max_timeout)));
			httpclient = new DefaultHttpClient(httpParams);

			httpget = new HttpGet(getString(R.string.json_url));
			response = httpclient.execute(httpget);
			HttpEntity entity = response.getEntity();

			if (entity != null) {
				ArrayList<Station> tmp_stations = CBWFeedParser.parse(entity
						.getContent());
				if (tmp_stations != null) {
					m_stations = tmp_stations;
				} else {
					deleteEntries();
					UIshowMsg(getString(R.string.exception_parser));
				}
			} else {
				deleteEntries();
				UIshowMsg(getString(R.string.exception_content));
			}

			httpclient.getConnectionManager().shutdown();
		} catch (UnknownHostException e) {
			deleteEntries();
			UIshowErr(getString(R.string.exception_hostnotfound));
		} catch (ClientProtocolException e) {
			deleteEntries();
			UIshowErr(getString(R.string.exception_protocol));
		} catch (IOException e) {
			deleteEntries();
			UIshowErr(getString(R.string.exception_io));
		}
	}

	private Location getLocation() {
		this.curLocation = locationHelper.getLocation();
		return curLocation;
	}

	private void UIshowErr(final String errMsg) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				m_ProgressDialog.dismiss();
				hideSplash();
				showMsg(errMsg);
			}
		});
	}

	private void UIshowMsg(final String text) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				showMsg(text);
			}
		});
	}

	private void showMsg(final String text) {
		Context context = getApplicationContext();
		int duration = Toast.LENGTH_LONG;

		Toast toast = Toast.makeText(context, text, duration);
		toast.show();
	}

	private Runnable returnRes = new Runnable() {
		@Override
		public void run() {
			if (m_stations != null && m_stations.size() > 0) {

				Collections.sort(m_stations,
						new LocationComparator(curLocation));

				m_adapter.clear();
				for (int i = 0; i < m_stations.size(); i++)
					m_adapter.add(m_stations.get(i));
			}
			m_ProgressDialog.dismiss();
			hideSplash();
			m_adapter.notifyDataSetChanged();
		}
	};

	private void deleteEntries() {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				m_adapter.clear();
				m_adapter.notifyDataSetChanged();
			}
		});
	}

	private void showMap(Location loc) {
		String uri = getString(R.string.map_uri);
		uri = uri.replaceFirst("\\$lat", Double.toString(loc.getLatitude()));
		uri = uri.replaceFirst("\\$long", Double.toString(loc.getLongitude()));
		startActivity(new Intent(android.content.Intent.ACTION_VIEW, Uri
				.parse(uri)));
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		if (l == findViewById(android.R.id.list)) {
			Station o = m_stations.get(position);
			showMap(o.getLocation());
		}
	}

	@Override
	protected void onPause() {
		super.onPause();

		locationHelper.onPause();
		if (m_ProgressDialog != null) {
			m_ProgressDialog.dismiss();
			hideSplash();
		}
	}

	private void hideSplash() {
		if (splash) {
			ImageView splashScreen = (ImageView) findViewById(R.id.splashscreen);
			splashScreen.setVisibility(View.GONE);
			splashScreen.setImageDrawable(null);
			splash = false;
		}
	}

//	private void setFullScreen(boolean full) {
//		if (full) {
//			WindowManager.LayoutParams attrs = this.getWindow().getAttributes();
//			attrs.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
//			this.getWindow().setAttributes(attrs);
//		} else {
//			WindowManager.LayoutParams attrs = this.getWindow().getAttributes();
//			attrs.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
//			this.getWindow().setAttributes(attrs);
//		}
//	}
}
