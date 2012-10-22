package org.agmip.functions;

import java.util.ArrayList;
import java.util.Map;
import static org.agmip.util.MapUtil.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provide static functions for soil data handling
 *
 * @author Meng Zhang
 */
public class SoilHelper {

    private static final Logger LOG = LoggerFactory.getLogger(ExperimentHelper.class);

    /**
     * Calculate root growth factor (0-1) for each soil layer
     *
     * @param data The data holder which contains array of soil_layer_depth (cm)
     * @param pp depth of top of curve (pivot point) (cm)
     * @return The array of root_growth_factor (0-1)
     */
    public static double[] getRootDistribution(Map data, String pp) {
        ArrayList<Map> soils = getObjectOr(data, "soils", new ArrayList());
        if (soils.isEmpty()) {
            return null;
        }
        ArrayList<Map<String, String>> soilLarys = getObjectOr(soils.get(0), "soilLayer", new ArrayList());
        if (soilLarys.isEmpty()) {
            return null;
        }
        String[] sllbs = new String[soilLarys.size()];
        for (int i = 0; i < soilLarys.size(); i++) {
            sllbs[i] = soilLarys.get(i).get("sllb");
        }
        return getRootDistribution(sllbs, pp);
    }

    /**
     * Calculate root growth factor (0-1) for each soil layer, return value with
     * String type
     *
     * @param data The data holder which contains array of soil_layer_depth (cm)
     * @param pp depth of top of curve (pivot point) (cm)
     * @return The array of root_growth_factor (0-1) (String type)
     */
    public static String[] getRootDistributionStr(Map data, String pp) {
        return tranDoubleToString(getRootDistribution(data, pp));
    }
    
    /**
     * Update the data map with calculated soil growth factor (0-1)
     * 
     * @param data The data holder which contains array of soil_layer_depth (cm)
     * @param pp depth of top of curve (pivot point) (cm)
     * @param multiplier
     */
    public static void updRootDistribution(Map data, String pp, String multiplier) {
        String[] factors = getRootDistributionStr(data, pp);
        double multi;
        try {
            multi = Double.parseDouble(multiplier);
        } catch (Exception e) {
            LOG.error("INVALID NUMBER FOR MULTIPLIER");
            return;
        }
        // TODO update map
    }

    /**
     * Calculate root growth factor (0-1) for each soil layer
     *
     * @param sllbs The array of soil_layer_depth (cm)
     * @param pp depth of top of curve (pivot point) (cm)
     * @return The array of root_growth_factor (0-1)
     */
    public static double[] getRootDistribution(String[] sllbs, String pp) {

        double[] ret = null;
        double[] dbSllbs = new double[sllbs.length];
        double mid;
        double dbPp;

        if (sllbs.length < 1) {
            return ret;
        } else {
            try {
                dbPp = Double.parseDouble(pp);
                for (int i = 0; i < dbSllbs.length; i++) {
                    dbSllbs[i] = Double.parseDouble(sllbs[i]);
                }
            } catch (NumberFormatException e) {
                LOG.error("INVALID INPUT NUMBER [" + e.getMessage() + "]");
                return ret;
            }
        }

        ret = new double[sllbs.length];
        ret[0] = 1;

        for (int i = 1; i < sllbs.length; i++) {
            mid = (dbSllbs[i] + dbSllbs[i - 1]) / 2;
            if (mid > dbPp) {
                ret[i] = Math.exp(-0.02 * (mid - dbPp));
            } else {
                ret[i] = 1;
            }
//            LOG.debug("Layer " + (i + 1) + " : sllb= " + dbSllbs[i] + ", mid=" + mid + ", factor=" + ret[i]);
        }

        return ret;
    }

    /**
     * Calculate root growth factor (0-1) for each soil layer, return value with
     * String type
     *
     * @param sllbs The array of soil_layer_depth (cm)
     * @param pp depth of top of curve (pivot point) (cm)
     * @return The array of root_growth_factor (0-1) (String type)
     */
    public static String[] getRootDistributionStr(String[] sllbs, String pp) {
        return tranDoubleToString(getRootDistribution(sllbs, pp));
    }

    /**
     * Translate a double array to String array
     *
     * @param in Input double array
     * @return String array
     */
    private static String[] tranDoubleToString(double[] in) {
        String[] ret = new String[in.length];
        for (int i = 0; i < ret.length; i++) {
            ret[i] = String.format("%.3f", in[i]);
        }
        return ret;
    }
}
