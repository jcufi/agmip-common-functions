package org.agmip.translators.soil;

import java.util.HashMap;
import java.util.Map;
/**
 * Interface used to change the layer reducer algorithm behaviour, ie aggregation criteria and values put in the aggregated layer.
 * @author jucufi
 *
 */
public interface LayerReducerDecorator {
	/**
	 * Returns true if the both soil layers should be merge into a single one
	 * @param currentSoil The current soil layer (n)
	 * @param previousSoil The previous soil layer (n - 1)
	 * @return
	 */
	public boolean shouldAggregateSoils(HashMap<String, String> currentSoil, HashMap<String, String> previousSoil);

	/**
	 * Returns the new soil layer filled with parameters coming from layers set as input parameters
	 * @param currentSoil The current soil layer (n)
	 * @param previousSoil The previous soil layer (n - 1)
	 * @return
	 */
	public HashMap<String, String> computeSoil(Map<String, String> currentSoil, Map<String, String> previousSoil);
		
}
