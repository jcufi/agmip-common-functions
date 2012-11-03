package org.agmip.functions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
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

    private static final Logger LOG = LoggerFactory.getLogger(SoilHelper.class);

    /**
     * Calculate root growth factor (0-1) for each soil layer
     *
     * @param sllbs The array of soil_layer_depth (cm)
     * @param pp depth of top of curve (pivot point) (cm)
     */
    public static void getRootDistribution(String m, String pp, String rd, HashMap data) {

        double[] dSllbs;
        double mid;
        double dPp;
        double dRd;
        double dM;
        double dK;
//        ArrayList<HashMap<String, Object>> soilLayers = traverseAndGetSiblings(data, "sllb");
        ArrayList<HashMap<String, Object>> soilLayers = getSoilLayer(data);
        // ArrayList<HashMap<String, String>> soilLayers = MapUtil.getBucket(data, "soils").getDataList();

        if (soilLayers == null) {
            return;
        } else if (soilLayers.isEmpty()) {
            LOG.error("----  SOIL LAYER DATA IS EMPTY");
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

        // First layer
        soilLayers.get(0).put("slrgf", getGrowthFactor(dSllbs[0] / 2, dPp, dK, dM, 3));
//        insertValue(data, "slrgf", getGrowthFactor(dSllbs[0] / 2, dPp, dK, dM, 3));

        // Other layers
        for (int i = 1; i < dSllbs.length; i++) {
            mid = (dSllbs[i] + dSllbs[i - 1]) / 2;
            String slrgf = getGrowthFactor(mid, dPp, dK, dM, 3);
            soilLayers.get(i).put("slrgf", slrgf);
//            insertValue(data, "slrgf", slrgf);
//            LOG.debug("Layer " + (i + 1) + " : sllb= " + dSllbs[i] + ", mid=" + mid + ", factor=" + slrgf);
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

    /**
     * Get soil layer data array from data holder. Only get the first soil site.
     *
     * @param data The experiment data holder
     * @return
     */
    protected static ArrayList getSoilLayer(Map data) {
        HashMap soils = (HashMap) getObjectOr(data, "soil", new HashMap());

        if (soils.isEmpty()) {
            LOG.error("SOIL DATA IS EMPTY");
            return null;
        } else {
            return getObjectOr(soils, "soilLayer", new ArrayList());
        }
    }
}
