// FIXME: move to activity so that onPause can be implemented

package name.starnberger.guenther.android.cbw;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;

public class LocationHelper implements LocationListener {
	String provider;
	volatile Location cachedLocation;
	LocationManager mgr;
	int maxAge;
	int timeout;
	volatile Location callBackLocation;
	Semaphore waitReq;

	public LocationHelper(LocationManager mgr, int maxage, int timeout) {
		this.mgr = mgr;
		this.maxAge = maxage;
		this.timeout = timeout;
		updateProvider();
	}

	public synchronized Location getLocation() {
		updateProvider();
		
		if(this.provider == null) {
			return null;
		}
		
		Location lastLocation = mgr.getLastKnownLocation(provider);

		if ((lastLocation != null) && (lastLocation.getTime() + maxAge > System.currentTimeMillis())) {
			return lastLocation;
		}

		mgr
				.requestLocationUpdates(provider, 0, 0, this, Looper
						.getMainLooper());

		callBackLocation = null;
		waitReq = new Semaphore(0);

		try {
			waitReq.tryAcquire(timeout, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			return lastLocation;
		} finally {
			mgr.removeUpdates(this);
		}

		if (callBackLocation != null) {
			cachedLocation = callBackLocation;
			return callBackLocation;
		} else {
			return lastLocation;
		}
	}

	public Location getCachedLocation() {
		// FIXME: Should we call updateProvider here?
		
		if(this.provider == null) {
			return null;
		} else if (cachedLocation == null) {
			cachedLocation = mgr.getLastKnownLocation(provider);
		}
		return cachedLocation;
	}

	@Override
	public void onLocationChanged(Location location) {
		callBackLocation = location;
		if (waitReq != null) {
			waitReq.release();
		}
	}

	@Override
	public void onProviderDisabled(String provider) {
		if (waitReq != null) {
			waitReq.release();
		}
	}

	@Override
	public void onProviderEnabled(String provider) {
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		if (waitReq != null) {
			waitReq.release();
		}
	}

	// Must be called by containing class
	public void onPause() {
		mgr.removeUpdates(this);
		if (waitReq != null) {
			waitReq.release();
		}
	}
	
	private void updateProvider() {
		this.provider = mgr.getBestProvider(new Criteria(), true);
	}
}
