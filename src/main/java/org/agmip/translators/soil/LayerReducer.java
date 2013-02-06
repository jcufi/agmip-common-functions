package org.agmip.translators.soil;

import java.util.ArrayList;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class used to reduce number of soil layers, it reduces the number of soil
 * layers according the criteria decorator. If at the end of the process the
 * soil layers number is greater than the max, we will aggregate all the deepest
 * layers into a single one without taking into account the criteria.
 * 
 * @author jucufi
 * @author dripoche
 * 
 */
public class LayerReducer {
	private static final Logger log = LoggerFactory.getLogger(LayerReducer.class);
	private LayerReducerDecorator decorator;
	private int maxSoilLayers;
	private int MAX_SOIL_LAYERS = 5;

	// Soil information under soil section
	public static String SLLL = "slll";
	public static String SLDUL = "sldul";
	public static String SLBDM = "slbdm";
	public static String SKSAT = "sksat";
	public static String SLLB = "sllb";
	public static String SLOC = "sloc";

	public LayerReducer(LayerReducerDecorator decorator) {
		this.decorator = decorator;
		this.maxSoilLayers = MAX_SOIL_LAYERS;
	}

	/**
	 * Process all the soil layers and return a new structure with aggregated
	 * data.
	 * 
	 * @param soilsData
	 * @return
	 */
	public ArrayList<HashMap<String, String>> process(ArrayList<HashMap<String, String>> soilsData) {
		HashMap<String, String> previousSoil;
		ArrayList<HashMap<String, String>> aggregatedSoilsData;
		HashMap<String, String> aggregatedSoil;
		boolean aggregate;
		boolean enforceAggregation;
		previousSoil = null;
		aggregate = true;
		enforceAggregation = false;
		aggregatedSoilsData = new ArrayList<HashMap<String, String>>();
		ArrayList<HashMap<String, String>> normalizedSoilsData = normalizeSoilLayers(soilsData);
		log.debug("Formated soil data : " + normalizedSoilsData);
		int i = 0;
		for (HashMap<String, String> currentSoil : normalizedSoilsData) {
			i++;
			if (previousSoil != null) {
				if (aggregatedSoilsData.size() == getMaxSoilLayers()) {
					// ok max is reached up to now we enforce aggregation
					enforceAggregation = true;
				}
				aggregate = decorator.shouldAggregateSoils(currentSoil, previousSoil) || enforceAggregation;
				if (aggregate) {
					log.debug("Aggregating soil layers... " + i + " and " + (i - 1));
					log.debug("soil " + i + " " + currentSoil);
					log.debug("soil " + (i - 1) + " " + previousSoil);
					// Compute the new map
					aggregatedSoil = decorator.computeSoil(currentSoil, previousSoil);
					if (aggregatedSoilsData.contains(previousSoil)) {
						aggregatedSoilsData.remove(previousSoil);
					}
					// Set as previous soil treated
					previousSoil = aggregatedSoil;
					aggregatedSoilsData.add(aggregatedSoil);
				} else {
					previousSoil = currentSoil;
					log.debug("Adding soil layer ...");
					log.debug("soil " + i + " " + currentSoil);
					aggregatedSoilsData.add(currentSoil);
				}
			} else {
				previousSoil = currentSoil;
				aggregatedSoilsData.add(currentSoil);
			}
		}
		log.info("Information about soil aggregation");
		log.info("Soil layers before : " + soilsData.size());
		log.info("Soil layers after  : " + aggregatedSoilsData.size());
		return aggregatedSoilsData;
	}

	public int getMaxSoilLayers() {
		return maxSoilLayers;
	}

	/**
	 * Set the max of soil layers at the end on reduction
	 * 
	 * @param maxSoilLayers
	 */
	public void setMaxSoilLayers(int maxSoilLayers) {
		this.maxSoilLayers = maxSoilLayers;
	}

	/**
	 * Fill each layer with all parameters. In the json structure when a
	 * parameter value is missing we must take the value of the previous layer.
	 * 
	 * @param soilsData
	 * @return
	 */
	public ArrayList<HashMap<String, String>> normalizeSoilLayers(ArrayList<HashMap<String, String>> soilsData) {
		HashMap<String, String> referenceSoil;
		ArrayList<HashMap<String, String>> newSoilsData;
		referenceSoil = soilsData.get(0);
		newSoilsData = new ArrayList<HashMap<String, String>>();
		if (referenceSoil != null) {
			for (HashMap<String, String> currentSoil : soilsData) {
				// create a new soil with reference parameters
				HashMap<String, String> fullCurrentSoil = new HashMap<String, String>(referenceSoil);
				for (String key : referenceSoil.keySet()) {
					if (currentSoil.containsKey(key)) {
						fullCurrentSoil.put(key, currentSoil.get(key));
					}
				}
				newSoilsData.add(fullCurrentSoil);
			}
		}
		return newSoilsData;
	}

}
