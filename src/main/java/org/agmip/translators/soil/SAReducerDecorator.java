package org.agmip.translators.soil;

import static java.lang.Float.parseFloat;
import static org.agmip.translators.soil.LayerReducer.SKSAT;
import static org.agmip.translators.soil.LayerReducer.SLBDM;
import static org.agmip.translators.soil.LayerReducer.SLDUL;
import static org.agmip.translators.soil.LayerReducer.SLLB;
import static org.agmip.translators.soil.LayerReducer.SLLL;
import static org.agmip.translators.soil.LayerReducer.SLOC;

import java.util.HashMap;
import java.util.Map;
/**
 * Stics and Aquacrop decorator.
 * Stics and Aquacrop use the water reserve and bulk density criteria for reducing soil layers.
 *
 * @author jucufi
 *
 */
public class SAReducerDecorator implements LayerReducerDecorator {
	// Soil information under init section
	public static String ICBL = "icbl";
	public static String ICH2O = "ich2o";
	public static String ICNO3 = "icno3";
	public static String ICNH4 = "icnh4";
	private WaterReserveCriteria criteria;
	private String[] allParams = new String[] { SLLL, SLDUL, SLBDM, SKSAT, SLOC, ICH2O, SLLB, ICNO3, ICNH4 };

	public SAReducerDecorator() {
		criteria = new WaterReserveCriteria();
	}

	public WaterReserveCriteria getCriteria() {
		return criteria;
	}

	/**
	 * Perform initial condition convertion, for icnh4 and icno3 it's important to take into account the deep of the layer
	 * for computing the aggregated value.
	 * @param key
	 * @param fullCurrentSoil
	 * @param previousSoil
	 * @return
	 */
	public Float computeInitialConditions(String key, Map<String, String> fullCurrentSoil, Map<String, String> previousSoil) {
		Float newValue = (parseFloat(fullCurrentSoil.get(key)) * parseFloat(fullCurrentSoil.get(SLLB)) + parseFloat(previousSoil.get(key)) * parseFloat(previousSoil.get(SLLB)));
		newValue = newValue / (parseFloat(fullCurrentSoil.get(SLLB)) + parseFloat(previousSoil.get(SLLB)));
		return newValue;
	}

	/**
	 * Create a new soil filled with aggregated data coming from soils set as
	 * input parameters.
	 * 
	 * @param fullCurrentSoil
	 * @param previousSoil
	 * @return
	 */
	public HashMap<String, String> computeSoil(Map<String, String> fullCurrentSoil, Map<String, String> previousSoil) {
		HashMap<String, String> aggregatedSoil;
		String fullCurrentValue;
		String previousValue;
		Float newValue;
		newValue = 0f;
		aggregatedSoil = new HashMap<String, String>();
		for (String p : allParams) {
			if (SLLB.equals(p)) {
				newValue = (parseFloat(fullCurrentSoil.get(p)) + parseFloat(previousSoil.get(p)));
			} else if ((ICNH4.equals(p) && fullCurrentSoil.containsKey(ICNH4) && previousSoil.containsKey(ICNH4)) || ICNO3.equals(p) && fullCurrentSoil.containsKey(ICNO3)
					&& previousSoil.containsKey(ICNO3)) {
				newValue = computeInitialConditions(p, fullCurrentSoil, previousSoil);
			} else {
				fullCurrentValue = fullCurrentSoil.get(p) == null ? LayerReducerUtil.defaultValue(p) : fullCurrentSoil.get(p);
				previousValue = previousSoil.get(p) == null ? LayerReducerUtil.defaultValue(p) : previousSoil.get(p);
				newValue = (parseFloat(fullCurrentValue) + parseFloat(previousValue)) / 2f;
			}
			aggregatedSoil.put(p, newValue.toString());
		}
		return aggregatedSoil;
	}

	/**
	 * @see org.agmip.translators.soil.LayerReducerDecorator
	 */
	public boolean shouldAggregateSoils(HashMap<String, String> currentSoil, HashMap<String, String> previousSoil) {
		return criteria.shouldAggregateSoils(currentSoil, previousSoil);
	}
}
