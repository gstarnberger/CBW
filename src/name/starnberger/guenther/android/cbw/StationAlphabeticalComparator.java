package name.starnberger.guenther.android.cbw;

import java.text.Collator;
import java.util.Comparator;
import java.util.Locale;

public class StationAlphabeticalComparator implements Comparator<Station> {
	Collator collator;

	// FIXME: Performance can be increased here by doing the outer comparison
	// using a CollationKeys interface (instead of using multiple Collator
	// comparisons).

	public StationAlphabeticalComparator() {
		// FIXME: If CBW is adapted to other countries this should be changed
		// accordingly.
		collator = Collator.getInstance(Locale.GERMAN);
		collator.setStrength(Collator.PRIMARY);
	}

	@Override
	public int compare(Station x, Station y) {
		return collator.compare(x.getStationName(), y.getStationName());
	}
}
