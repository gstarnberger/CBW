package name.starnberger.guenther.android.cbw;

import java.io.InputStream;
import java.util.ArrayList;

import android.sax.Element;
import android.sax.EndElementListener;
import android.sax.EndTextElementListener;
import android.sax.RootElement;
import android.util.Xml;

public class CBWFeedParser {
	static ArrayList<Station> parse(InputStream istream) {
		final Station currentStation = new Station();
		RootElement root = new RootElement("stations");
		final ArrayList<Station> stations = new ArrayList<Station>();
		Element station = root.getChild("station");
		station.setEndElementListener(new EndElementListener() {
			public void end() {
				Station copiedStation = currentStation.copy();
				copiedStation.updateLocation();
				stations.add(copiedStation);
				currentStation.clear();
			}
		});
		station.getChild("name").setEndTextElementListener(
				new EndTextElementListener() {
					public void end(String body) {
						currentStation.setStationName(body);
					}
				});
		station.getChild("description").setEndTextElementListener(
				new EndTextElementListener() {
					public void end(String body) {
						currentStation.setStationDescription(body);
					}
				});
		station.getChild("free_bikes").setEndTextElementListener(
				new EndTextElementListener() {
					public void end(String body) {
						currentStation.setBikesAvailable(body);
					}
				});
		station.getChild("free_boxes").setEndTextElementListener(
				new EndTextElementListener() {
					public void end(String body) {
						currentStation.setBoxesAvailable(body);
					}
				});
		station.getChild("latitude").setEndTextElementListener(
				new EndTextElementListener() {
					public void end(String body) {
						currentStation.setLatitude(body);
					}
				});
		station.getChild("longitude").setEndTextElementListener(
				new EndTextElementListener() {
					public void end(String body) {
						currentStation.setLongitude(body);
					}
				});
		station.getChild("status").setEndTextElementListener(
				new EndTextElementListener() {
					public void end(String body) {
						if(body.equals("aktiv")) {
							currentStation.setActive(true);
						} else {
							currentStation.setActive(false);
						}
					}
				});
		try {
			// FIXME: Get encoding from HTTP content type
			Xml.parse(istream, Xml.Encoding.UTF_8, root.getContentHandler());
		} catch (Exception e) {
			// FIXME: maybe we should log this?
			return null;
		}
		return stations;
	}
}