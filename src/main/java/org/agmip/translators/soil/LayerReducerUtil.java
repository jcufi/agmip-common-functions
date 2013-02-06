package org.agmip.translators.soil;

import static java.lang.Float.parseFloat;

import java.util.ArrayList;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LayerReducerUtil {
	public static final Logger log = LoggerFactory.getLogger(LayerReducerUtil.class);
	private static String UNKNOWN_DEFAULT_VALUE = "0.0";

	/**
	 * Compute soil layer thickness
	 * 
	 * @param soilsData
	 * @return
	 */
	public static ArrayList<HashMap<String, String>> computeSoilLayerSize(ArrayList<HashMap<String, String>> soilsData) {
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

	public static void mergeSoilAndInitializationData(ArrayList<HashMap<String, String>> soilsData, ArrayList<HashMap<String, String>> initData) {
		int index = 0;
		log.info("Init data size : " + initData.size());
		log.info("Soil data size : " + soilsData.size());
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
