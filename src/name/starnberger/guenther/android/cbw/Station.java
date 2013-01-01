package name.starnberger.guenther.android.cbw;

import android.location.Location;

public class Station {
	final static private String LOCATION_PROVIDER = "sysfrog";
	private String stationName;
	private String stationDescription;
	private String bikesAvailable;
	private String boxesAvailable;
	private String latitude;
	private String longitude;
	private Location location;
	private boolean active;

	public String getStationName() {
		return stationName;
	}

	public void setStationName(String stationName) {
		this.stationName = stationName;
	}

	public String getStationDescription() {
		return stationDescription;
	}

	public void setStationDescription(String stationDescription) {
		this.stationDescription = stationDescription;
	}

	public String getBikesAvailable() {
		return bikesAvailable;
	}

	public void setBikesAvailable(String bikesAvailable) {
		this.bikesAvailable = bikesAvailable;
	}

	public String getBoxesAvailable() {
		return boxesAvailable;
	}

	public void setBoxesAvailable(String boxesAvailable) {
		this.boxesAvailable = boxesAvailable;
	}

	public Location getLocation() {
		return location;
	}

	public void setLocation(Location location) {
		this.location = location;
	}

	public float getDistance(Location curLocation) {
		return curLocation.distanceTo(getLocation());
	}

	public void setLatitude(String latitude) {
		this.latitude = latitude;
	}

	public String getLatitude() {
		return latitude;
	}

	public void setLongitude(String longitude) {
		this.longitude = longitude;
	}

	public String getLongitude() {
		return longitude;
	}
	
	public void setActive(boolean active) {
		this.active = active;
	}

	public boolean isActive() {
		return active;
	}

	public Station copy() {
		Station station = new Station();
		station.setStationName(stationName);
		station.setStationDescription(stationDescription);
		station.setBikesAvailable(bikesAvailable);
		station.setBoxesAvailable(boxesAvailable);
		station.setLocation(location);
		station.setLatitude(latitude);
		station.setLongitude(longitude);
		station.setActive(active);
		return station;
	}

	public void clear() {
		this.setStationName(null);
		this.setStationDescription(null);
		this.setBikesAvailable(null);
		this.setBoxesAvailable(null);
		this.setLocation(null);
		this.setActive(false);
		this.setLatitude(null);
		this.setLongitude(null);
	}

	public void updateLocation() {
		Location location = new Location(LOCATION_PROVIDER);

		try {
			location.setLatitude(Double.parseDouble(latitude));
			location.setLongitude(Double.parseDouble(longitude));
		} catch (NumberFormatException e) {
		}

		this.location = location;
	}
}
