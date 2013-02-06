package org.agmip.translators.soil;

import static java.lang.Float.parseFloat;
import static org.agmip.translators.soil.LayerReducer.SLBDM;
import static org.agmip.translators.soil.LayerReducer.SLDUL;
import static org.agmip.translators.soil.LayerReducer.SLLL;

import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Criteria used to know if two layers should be aggregated based on water reserve and bulk density.
 * @author jucufi
 *
 */
public class WaterReserveCriteria {
	
	private static final Logger log = LoggerFactory.getLogger(WaterReserveCriteria.class);
	
	public WaterReserveCriteria() {
		firstThreshold = FIRST_THRESHOLD_DEFAULT;
		secondThreshold = SECOND_THRESHOLD_DEFAULT;
	}

	/**
	 * threshold based on ru in mm/m
	 */
	public float firstThreshold;

	/**
	 * threshold based on bulk density in gm/cm3
	 */
	public float secondThreshold;

	// Thresholds used to merge soil layers
	// threshold in mm/m
	public static float FIRST_THRESHOLD_DEFAULT = 10f;
	// threshold in g/cm3
	public static float SECOND_THRESHOLD_DEFAULT = 0.08f;

	/**
	 * Round float
	 * 
	 * @param r
	 * @return
	 */
	public Float round(Float r) {
		return Math.round(r * 100.0) / 100f;
	}

	/**
	 * Criteria for merging soils ru is the maximum available water reserve
	 * (reserve utile) sdul is the field capacity slll is the wilting point
	 * (point de fletrissement permanent) slbdm is the bulk density
	 * 
	 * @param currentSoil
	 * @param previousSoil
	 * @return
	 */
	public boolean shouldAggregateSoils(HashMap<String, String> currentSoil, HashMap<String, String> previousSoil) {
		float ruCurrent;
		float ruPrevious;
		float resultFirstRule;
		float resultSecRule;
		boolean firstRule;
		boolean secRule;

		// ru in mm/m
		ruCurrent = (parseFloat(currentSoil.get(SLDUL)) - parseFloat(currentSoil.get(SLLL))) * 1000.0f;
		ruPrevious = (parseFloat(previousSoil.get(SLDUL)) - parseFloat(previousSoil.get(SLLL))) * 1000f;
		resultFirstRule = round(Math.abs(ruCurrent - ruPrevious));
		firstRule = resultFirstRule <= FIRST_THRESHOLD_DEFAULT;

		/**
		 * First rule : (currentRu - previousRu) <= 5 mm/m Second rule :
		 * (currentBdm - previousBdm) <= 0.05 g/cm3 Soil layers are aggregated
		 * if the rules below are both true
		 * */
		resultSecRule = round(Math.abs(parseFloat(currentSoil.get(SLBDM)) - parseFloat(previousSoil.get(SLBDM))));
		secRule = (round(resultSecRule) <= SECOND_THRESHOLD_DEFAULT);

		log.debug("*********************");
		log.debug("Ru current : "+ruCurrent);
		log.debug("Ru previous : "+ruPrevious);
		log.debug("First rule : " + resultFirstRule + " <= " + FIRST_THRESHOLD_DEFAULT + " ? " + firstRule);
		log.debug("Sec rule : " + resultSecRule + " <= " + SECOND_THRESHOLD_DEFAULT + " ? " + secRule);
		log.debug("*********************");

		return firstRule && secRule;
	}

	/**
	 * Threshold corresponding to the water reserve difference between layers in mm/m
	 * @return
	 */
	public float getFirstThreshold() {
		return firstThreshold;
	}

	public void setFirstThreshold(float firstThreshold) {
		this.firstThreshold = firstThreshold;
	}

	/**
	 * Threshold corresponding to the water reserve difference between layers in mm/m
	 * @return
	 */
	public float getSecondThreshold() {
		return secondThreshold;
	}

	public void setSecondThreshold(float secondThreshold) {
		this.secondThreshold = secondThreshold;
	}
}
