package org.agmip.translators.soil;

import static java.lang.Float.parseFloat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Some util functions for handling soil data
 * 
 * @author jucufi
 * 
 */
public class LayerReducerUtil {
	public static final Logger log = LoggerFactory.getLogger(LayerReducerUtil.class);
	private static final String UNKNOWN_DEFAULT_VALUE = "0.0";

	/**
	 * Compute soil layer thickness.
	 * 
	 * @param soilsData soil informations (with soil layer deep instead of soil layer thickness)
	 * @return the same map as input but with soil layer thickness
	 */
	public static ArrayList<HashMap<String, String>> computeSoilLayerSize(List<HashMap<String, String>> soilsData) {
		float deep = 0.0f;
		ArrayList<HashMap<String, String>> newSoilsData;
		newSoilsData = new ArrayList<HashMap<String, String>>();

		for (HashMap<String, String> currentSoil : soilsData) {
			// create a new soil with reference parameters
			HashMap<String, String> newCurrentSoil = new HashMap<String, String>(currentSoil);
			// Specific for stics soil data representation
			newCurrentSoil.put(LayerReducer.SLLB, new Float(parseFloat(currentSoil.get(LayerReducer.SLLB)) - deep).toString());
			deep = parseFloat(currentSoil.get(LayerReducer.SLLB));
			newSoilsData.add(newCurrentSoil);
		}
		return newSoilsData;
	}

	/**
	 * TODO replace this function by the DOME
	 * 
	 * @param key
	 * @return
	 */
	public static String defaultValue(String key) {
		String value;
		HashMap<String, String> defaultValues;
		defaultValues = new HashMap<String, String>();
		defaultValues.put("slcly", "12.6");
		defaultValues.put("salb", "0.25");
		defaultValues.put("slphw", "6.2");
		defaultValues.put("sksat", "0.0");
		defaultValues.put("caco3", "0.0");
		defaultValues.put("sloc", "0.1");
		defaultValues.put("slll", "0.0");
		defaultValues.put("icnh4", "0.0");
		defaultValues.put("icno3", "0.0");
		defaultValues.put("ich2o", "0.0");

		if (defaultValues.containsKey(key)) {
			value = defaultValues.get(key);
		} else {
			value = UNKNOWN_DEFAULT_VALUE;
		}
		return value;
	}

	/**
	 * Merge soil and initialization information in the same map, it's important to processing both at the same time
	 * during soils layers reducing in order to avoid having inconsistent data. At the end of the call the first map
	 * contains initialization data.
	 * 
	 * @param soilsData soil data map
	 * @param initData initialization data map
	 */
	public static void mergeSoilAndInitializationData(List<HashMap<String, String>> soilsData, List<HashMap<String, String>> initData) {
		int index = 0;
		log.debug("Init data size : " + initData.size());
		log.debug("Soil data size : " + soilsData.size());
		if (initData.size() == 0) {
			log.error("Unable to merge soil information, initial condition information unavailable");
			return;
		}
		for (HashMap<String, String> soilData : soilsData) {
			if (initData.get(index).get(SAReducerDecorator.ICBL).equals(soilData.get(LayerReducer.SLLB))) {
				soilData.putAll(initData.get(index));
			} else {
				log.error("Unable to merge soil information, inconsistent soil information");
			}
			index = index + 1;
		}
	}
}
