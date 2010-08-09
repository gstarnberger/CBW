package name.starnberger.guenther.android.cbw;

import java.util.Comparator;

import android.location.Location;

public class LocationComparator implements Comparator<Station> {
	private Location curLocation;
	
	public LocationComparator(Location curLocation) {
		this.curLocation = curLocation;
	}
	
	@Override
	public int compare(Station x, Station y) {
		float xDist = x.getDistance(curLocation);
		float yDist = y.getDistance(curLocation);

		if (xDist < yDist) {
			return -1;
		} else if (xDist > yDist) {
			return 1;
		} else {
			return 0;
		}
	}
}
