package org.agmip.functions;

import java.util.ArrayList;
import java.util.HashMap;
import static org.agmip.util.MapUtil.*;
import static org.agmip.util.assume.Command.*;
import static org.agmip.ace.util.AcePathfinderUtil.insertValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provide static functions for soil data handling
 *
 * @author Meng Zhang
 */
public class SoilHelper {

    private static final Logger LOG = LoggerFactory.getLogger(ExperimentHelper.class);

//    /**
//     * Calculate root growth factor (0-1) for each soil layer
//     *
//     * @param data The data holder which contains array of soil_layer_depth (cm)
//     * @param pp depth of top of curve (pivot point) (cm)
//     * @return The array of root_growth_factor (0-1)
//     */
//    public static double[] getRootDistribution(Map data, String pp) {
//        ArrayList<Map> soils = getObjectOr(data, "soils", new ArrayList());
//        if (soils.isEmpty()) {
//            return null;
//        }
//        ArrayList<Map<String, String>> soilLarys = getObjectOr(soils.get(0), "soilLayer", new ArrayList());
//        if (soilLarys.isEmpty()) {
//            return null;
//        }
//        String[] sllbs = new String[soilLarys.size()];
//        for (int i = 0; i < soilLarys.size(); i++) {
//            sllbs[i] = soilLarys.get(i).get("sllb");
//        }
//        return getRootDistribution(sllbs, pp);
//    }
//
//    /**
//     * Calculate root growth factor (0-1) for each soil layer, return value with
//     * String type
//     *
//     * @param data The data holder which contains array of soil_layer_depth (cm)
//     * @param pp depth of top of curve (pivot point) (cm)
//     * @return The array of root_growth_factor (0-1) (String type)
//     */
//    public static String[] getRootDistributionStr(Map data, String pp) {
//        return tranDoubleToString(getRootDistribution(data, pp));
//    }
//
//    /**
//     * Update the data map with calculated soil growth factor (0-1)
//     *
//     * @param data The data holder which contains array of soil_layer_depth (cm)
//     * @param pp depth of top of curve (pivot point) (cm)
//     * @param multiplier
//     */
//    public static void updRootDistribution(Map data, String pp, String multiplier) {
//        String[] factors = getRootDistributionStr(data, pp);
//        double multi;
//        try {
//            multi = Double.parseDouble(multiplier);
//        } catch (Exception e) {
//            LOG.error("INVALID NUMBER FOR MULTIPLIER");
//            return;
//        }
//        // TODO update map
//    }
    /**
     * Calculate root growth factor (0-1) for each soil layer
     *
     * @param sllbs The array of soil_layer_depth (cm)
     * @param pp depth of top of curve (pivot point) (cm)
     */
    public static void getRootDistribution(String m, String pp, String rd, HashMap data) {

//        double[] ret = null;    // TODO set back to map
        double[] dSllbs;
        double mid;
        double dPp;
        double dRd;
        double dM;
        double dK;
//        ArrayList<HashMap<String, Object>> soilLayers = traverseAndGetSiblings(data, "sllb");
        ArrayList<HashMap> soils = getObjectOr(data, "soils", new ArrayList());
        if (soils.isEmpty()) {
            LOG.error("SOIL DATA IS EMPTY");
            return;
        }
        ArrayList<HashMap<String, Object>> soilLayers = getObjectOr(soils.get(0), "soilLayer", new ArrayList());

        if (soilLayers.isEmpty()) {
            LOG.error("SOIL LAYER DATA IS EMPTY");
            return;
        } else {
            try {
                dPp = Double.parseDouble(pp);
                dRd = Double.parseDouble(rd);
                dM = Double.parseDouble(m);
                dSllbs = new double[soilLayers.size()];
                dK = Math.log(0.02) / (dRd - dPp);
                for (int i = 0; i < soilLayers.size(); i++) {
                    dSllbs[i] = Double.parseDouble(getObjectOr(soilLayers.get(i), "sllb", "").toString());
                }
            } catch (NumberFormatException e) {
                LOG.error("INVALID INPUT NUMBER [" + e.getMessage() + "]");
                return;
            }
        }

//        ret = new double[dSllbs.length];
//        ret[0] = getGrowthFactor(dSllbs[0] / 2, dPp, dK, dM);
        insertValue(data, "slrgf", getGrowthFactor(dSllbs[0] / 2, dPp, dK, dM, 3));

        for (int i = 1; i < dSllbs.length; i++) {
            mid = (dSllbs[i] + dSllbs[i - 1]) / 2;
            String slrgf = getGrowthFactor(mid, dPp, dK, dM, 3);
            soilLayers.get(i).put("slrgf", slrgf);
//            insertValue(data, "slrgf", slrgf);
//            ret[i] = getGrowthFactor(mid, dPp, dK, dM);
            LOG.debug("Layer " + (i + 1) + " : sllb= " + dSllbs[i] + ", mid=" + mid + ", factor=" + slrgf);
        }

    }

    /**
     * soil factors which decline exponentially between PP and RD (units depend
     * on variable, same units as M (Maximum value, will use default value 1)
     *
     * @param mid The mid point value between two layers
     * @param pp depth of top soil, or pivot point of curve (cm)
     * @param k exponential decay rate
     * @return The growth factor (0-1)
     */
    protected static double getGrowthFactor(double mid, double pp, double k) {
        return getGrowthFactor(mid, pp, k, 1f);
    }

    /**
     * soil factors which decline exponentially between PP and RD (units depend
     * on variable, same units as M
     *
     * @param mid The mid point value between two layers
     * @param pp depth of top soil, or pivot point of curve (cm)
     * @param k exponential decay rate
     * @param m Maximum value in the top PP cm of soil (units depend on
     * variable)
     * @return The growth factor (0-M)
     */
    protected static double getGrowthFactor(double mid, double pp, double k, double m) {
        if (mid <= pp) {
            return m;
        } else {
            return m * Math.exp(k * (mid - pp));
        }
    }

    /**
     * soil factors which decline exponentially between PP and RD (units depend
     * on variable, same units as M, the output accuracy will depend on prec
     *
     * @param mid The mid point value between two layers
     * @param pp depth of top soil, or pivot point of curve (cm)
     * @param k exponential decay rate
     * @param m Maximum value in the top PP cm of soil (units depend on
     * variable)
     * @param prec The output decimal precision (0 for no decimal part)
     * @return The growth factor (0-M)
     */
    protected static String getGrowthFactor(double mid, double pp, double k, double m, int prec) {
        prec = prec < 0 ? 0 : prec;
        return String.format("%." + prec + "f", getGrowthFactor(mid, pp, k, m));
    }

    /**
     * soil factors which decline exponentially between PP and RD (units depend
     * on variable, same units as M, the output accuracy will depend on prec
     *
     * @param mid The mid point value between two layers
     * @param pp depth of top soil, or pivot point of curve (cm)
     * @param k exponential decay rate
     * @param prec The output decimal precision (0 for no decimal part)
     * @return The growth factor (0-1)
     */
    protected static String getGrowthFactor(double mid, double pp, double k, int prec) {
        return String.format("%." + prec + "f", getGrowthFactor(mid, pp, k, 1, prec));
    }

//    /**
//     * Calculate root growth factor (0-1) for each soil layer, return value with
//     * String type
//     *
//     * @param sllbs The array of soil_layer_depth (cm)
//     * @param pp depth of top of curve (pivot point) (cm)
//     * @return The array of root_growth_factor (0-1) (String type)
//     */
//    public static String[] getRootDistributionStr(String[] sllbs, String pp) {
//        return tranDoubleToString(getRootDistribution(sllbs, pp));
//    }
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
