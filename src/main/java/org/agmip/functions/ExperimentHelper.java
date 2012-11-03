package org.agmip.functions;


import java.math.BigDecimal;
import java.math.RoundingMode;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import org.agmip.common.Event;
import static org.agmip.common.Functions.*;
import static org.agmip.functions.SoilHelper.*;
import static org.agmip.util.MapUtil.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provide static functions for experiment data handling
 *
 * @author Meng Zhang
 * @version 0.1
 */
public class ExperimentHelper {

    private static final Logger LOG = LoggerFactory.getLogger(ExperimentHelper.class);

    /**
     * This function will calculate the planting date which is the first date
     * within the planting window<br/> that has an accumulated rainfall amount
     * (P) in the previous n days.
     *
     * @param eDate Earliest planting date (mm-dd or mmdd)
     * @param lDate Latest planting date (mm-dd or mmdd)
     * @param rain Threshold rainfall amount (mm)
     * @param days Number of days of accumulation
     * @param wthData The HashMap of experiment (including weather data)
     *
     * @return The calculated first planting date, if not valid based on the
     * input data, will return ""
     */
    public static void getAutoPlantingDate(String eDate, String lDate, String rain, String days, Map data) {

        Map wthData;
        Map expData;
        ArrayList<Map> dailyData;
        ArrayList<Map> eventData;
        Event event;
        Calendar eDateCal = Calendar.getInstance();
        Calendar lDateCal = Calendar.getInstance();
        int intDays;
        int duration;
        double accRainAmtTotal;
        double accRainAmt;
        int expDur;
        int startYear = 0;
        Window[] windows;

        // Validation for input parameters
        // Weather data check and try to get daily data
        if (data.isEmpty()) {
            LOG.error("NO ANY DATA.");
            return;
        } else {
            // Case for multiple data json structure
            if (data.containsKey("weathers")) {
                ArrayList<Map> wths = getObjectOr(data, "weathers", new ArrayList());
                if (wths.isEmpty()) {
                    LOG.error("NO WEATHER DATA.");
                    return;
                } else {
                    wthData = wths.get(0);
                    if (wthData.isEmpty()) {
                        LOG.error("NO WEATHER DATA.");
                        return;
                    } else {
                        dailyData = getObjectOr(wthData, "dailyWeather", new ArrayList());
                    }
                }
            } else {
                return;
            }

            if (dailyData.isEmpty()) {
                LOG.error("EMPTY DAILY WEATHER DATA.");
                return;
            }
        }

        // Check experiment data
        // Case for multiple data json structure
        if (data.containsKey("experiments")) {
            ArrayList<Map> exps = getObjectOr(data, "experiments", new ArrayList());
            if (exps.isEmpty()) {
                LOG.error("NO EXPERIMENT DATA.");
                return;
            } else {
                expData = exps.get(0);
                if (expData.isEmpty()) {
                    LOG.error("NO EXPERIMENT DATA.");
                    return;
                } else {
                    Map mgnData = getObjectOr(expData, "management", new HashMap());
                    eventData = getObjectOr(mgnData, "events", new ArrayList());
                }

                // Check EXP_DUR is avalaible
                try {
                    expDur = Integer.parseInt(getValueOr(expData, "exp_dur", "1"));
                } catch (Exception e) {
                    expDur = 1;
                }

                // The starting year for multiple year runs may be set with SC_YEAR.
                if (expDur > 1) {
                    try {
                        startYear = Integer.parseInt(getValueOr(expData, "sc_year", "").substring(0, 4));
                    } catch (Exception e) {
                        startYear = 0;
                    }
                }
                windows = new Window[expDur];
            }
        } else {
            LOG.error("NO EXPERIMENT DATA.");
            return;
        }

        // Check if there is eventData existing
        if (eventData.isEmpty()) {
            LOG.warn("EMPTY EVENT DATA.");
            event = new Event(new ArrayList(), "planting");
        } else {
            event = new Event(eventData, "planting");
            // If only one year is to be simulated, the recorded planting date year will be used (if available).
            if (expDur == 1) {
                if (event.isEventExist()) {
                    Map plEvent = event.getCurrentEvent();
                    try {
                        startYear = Integer.parseInt(getValueOr(plEvent, "date", "").substring(0, 4));
                    } catch (Exception e) {
                        startYear = 0;
                    }
                } else {
                    startYear = 0;
                }
            }
        }

        // If no starting year is provided, the multiple years will begin on the first available weather year.
        int startYearIndex;
        if (startYear == 0) {
            startYearIndex = 0;
        } else {
            startYearIndex = dailyData.size();
            for (int i = 0; i < dailyData.size(); i++) {
                String w_date = getValueOr(dailyData.get(i), "w_date", "");
                if (w_date.equals(startYear + "0101")) {
                    startYearIndex = i;
                    break;
                } else if (w_date.endsWith("0101")) {
                    i += 364;
                }
            }

            // If start year is out of weather data range
            if (startYearIndex == dailyData.size()) {
                // If one year duration, then use the first year
                if (expDur == 1) {
                    startYearIndex = 0;
                } // If multiple year duration, then report error and end function
                else {
                    LOG.error("THE START YEAR IS OUT OF DATA RANGE (SC_YEAR:[" + startYear + "]");
                    return;
                }
            }
        }

        // Check input dates
        if (!isValidDate(eDate, eDateCal, "-")) {
            LOG.error("INVALID EARLIST DATE:[" + eDate + "]");
            return;
        }
        if (!isValidDate(lDate, lDateCal, "-")) {
            LOG.error("INVALID LATEST DATE:[" + lDate + "]");
            return;
        }
        if (eDateCal.after(lDateCal)) {
            lDateCal.set(Calendar.YEAR, lDateCal.get(Calendar.YEAR) + 1);
        }
        duration = (int) ((lDateCal.getTimeInMillis() - eDateCal.getTimeInMillis()) / 86400000);

        // Check Number of days of accumulation
        try {
            intDays = Integer.parseInt(days);
        } catch (Exception e) {
            LOG.error("INVALID NUMBER FOR NUMBER OF DAYS OF ACCUMULATION");
            return;
        }
        if (intDays <= 0) {
            LOG.error("NON-POSITIVE NUMBER FOR NUMBER OF DAYS OF ACCUMULATION");
            return;
        }

        // Check Threshold rainfall amount
        try {
            accRainAmtTotal = Double.parseDouble(rain);
        } catch (Exception e) {
            LOG.error("INVALID NUMBER FOR THRESHOLD RAINFALL AMOUNT");
            return;
        }
        if (accRainAmtTotal <= 0) {
            LOG.error("NON-POSITIVE NUMBER FOR THRESHOLD RAINFALL AMOUNT");
            return;
        }

        // Find the first record which is the ealiest date for the window in each year
        int end;
        int start = getDailyRecIndex(dailyData, eDate, startYearIndex, 0);
        for (int i = 0; i < windows.length; i++) {
            end = getDailyRecIndex(dailyData, lDate, start, duration);
            windows[i] = new Window(start, end);
            if (i + 1 < windows.length) {
                start = getDailyRecIndex(dailyData, eDate, end, 365 - duration);
            }
        }

        if (windows[0].start == dailyData.size()) {
            LOG.error("NO VALID DAILY DATA FOR SEARCH WINDOW");
            return;
        }

        // Loop each window to try to find appropriate planting date
        for (int i = 0; i < windows.length; i++) {

            // Check first n days
            int last = Math.min(windows[i].start + intDays, windows[i].end);
            accRainAmt = 0;
            for (int j = windows[i].start; j < last; j++) {

                try {
                    accRainAmt += Double.parseDouble(getValueOr(dailyData.get(j), "rain", "0"));
                } catch (Exception e) {
                    continue;
                }
//                LOG.debug(getValueOr(dailyData.get(j), "w_date", "") + " : " + accRainAmt + ", " + (accRainAmt >= accRainAmtTotal));
                if (accRainAmt >= accRainAmtTotal) {
                    event.updateEvent("date", getValueOr(dailyData.get(j), "w_date", ""));
                    break;
                }
            }

            if (accRainAmt >= accRainAmtTotal) {
                continue;
            }

            // If the window size is smaller than n
            if (last > windows[i].end) {
                LOG.info("NO APPROPRIATE DATE WAS FOUND FOR NO." + (i + 1) + " PLANTING EVENT");
                // TODO remove one planting event
                event.removeEvent();
            }

            // Check following days
            for (int j = last; j < windows[i].end; j++) {

                try {
                    accRainAmt -= Double.parseDouble(getValueOr(dailyData.get(j - intDays), "rain", "0"));
                    accRainAmt += Double.parseDouble(getValueOr(dailyData.get(j), "rain", "0"));
                } catch (Exception e) {
                    continue;
                }
//                LOG.debug(getValueOr(dailyData.get(j), "w_date", "") + " : " + accRainAmt + ", " + (accRainAmt >= accRainAmtTotal));
                if (accRainAmt >= accRainAmtTotal) {
                    event.updateEvent("date", getValueOr(dailyData.get(j), "w_date", ""));
                    break;
                }
            }
        }
    }

    /**
     * Store a start index and end index of daily data array for a window
     */
    private static class Window {

        public int start;
        public int end;

        public Window(int start, int end) {
            this.start = start;
            this.end = end;
        }
    }

    /**
     * To check if the input date string is valid and match with the required
     * format
     *
     * @param date The input date string, which should comes with the format of
     * yyyy-mm-dd, the separator should be same with the third parameter
     * @param out The Calendar instance which will be assigned with input year,
     * month and day
     * @param separator The separator string used in date format
     * @return check result
     */
    private static boolean isValidDate(String date, Calendar out, String separator) {
        try {
            String[] dates = date.split(separator);
            out.set(Calendar.DATE, Integer.parseInt(dates[dates.length - 1]));
            out.set(Calendar.MONTH, Integer.parseInt(dates[dates.length - 2]));
            if (dates.length > 2) {
                out.set(Calendar.YEAR, Integer.parseInt(dates[dates.length - 3]));
            }
        } catch (Exception e) {
            try {
                out.set(Calendar.DATE, Integer.parseInt(date.substring(date.length() - 2, date.length())));
                out.set(Calendar.MONTH, Integer.parseInt(date.substring(date.length() - 4, date.length() - 2)) - 1);
                if (date.length() > 4) {
                    out.set(Calendar.YEAR, Integer.parseInt(date.substring(date.length() - 8, date.length() - 4)) - 1);
                }
            } catch (Exception e2) {
                return false;
            }
        }

        return true;
    }

    /**
     * To check if two input date string is same date with no matter about 2nd
     * input's separator
     *
     * @param date1 1st input date string with format yyyymmdd
     * @param date2 2nd input date string with format mmdd or mm-dd
     * @param separator The separator used in 2nd string
     * @return comparison result
     */
    private static boolean isSameDate(String date1, String date2, String separator) {

        date2 = date2.replace(separator, "");
        if (date2.equals("0229")) {
            try {
                int year1 = Integer.parseInt(date2.substring(2, 4));
                if (year1 % 4 != 0) {
                    return date1.endsWith("0228");
                }
            } catch (Exception e) {
                return false;
            }
        }

        return date1.endsWith(date2);
    }

    /**
     * Find the index of daily data array for the particular date
     *
     * @param dailyData The array of daily data
     * @param findDate The expected date
     * @param start The start index for searching
     * @param expectedDiff The default difference between start index and
     * expected index (will try this index first, if failed then start loop)
     * @return The index for the expected date, if no matching data, will return
     * the size of array
     */
    private static int getDailyRecIndex(ArrayList<Map> dailyData, String findDate, int start, int expectedDiff) {
        String date;
        if (start + expectedDiff < dailyData.size()) {
            date = getValueOr(dailyData.get(start + expectedDiff), "w_date", "");
            if (isSameDate(date, findDate, "-")) {
                return start + expectedDiff;
            } else {
                expectedDiff++;
                date = getValueOr(dailyData.get(start + expectedDiff), "w_date", "");
                if (isSameDate(date, findDate, "-")) {
                    return start + expectedDiff;
                }
            }
        }

        for (int j = start; j < dailyData.size(); j++) {
            date = getValueOr(dailyData.get(j), "w_date", "");
            if (isSameDate(date, findDate, "-")) {
                return j;
            }
        }
        return dailyData.size();
    }

    /**
     * Often the total amount of fertilizer in a growing season has been
     * recorded, but no details of application dates, types of fertilizer,etc.
     * This function allows a user to specify rules for fertilizer application
     * in a region. As a result, "N" fertilizer events are added to the JSON
     * object.
     *
     * @param num Number of fertilizer applications
     * @param fecd The code for type of fertilizer added
     * @param feacd The code for fertilizer application method
     * @param fedep The depth at which fertilizer is applied (cm)
     * @param offsets The array of date as offset from planting date (days)
     * (must be paired with ptps)
     * @param ptps The array of proportion of total N added (%) (must be paired
     * with offsets)
     * @param data The experiment data holder
     */
    public static void getFertDistribution(String num, String fecd, String feacd, String fedep, String[] offsets, String[] ptps, HashMap data) {
        int iNum;
        //Map expData;
        ArrayList<Map> eventData;
        double fen_tot;
        String[] fdates;
        double[] dPtps;
        Event events;
        String pdate;

        try {
            iNum = Integer.parseInt(num);
        } catch (Exception e) {
            LOG.error("INPUT NUMBER OF FERTILIZER APPLICATIONS IS NOT A NUMBERIC STRING [" + num + "]");
            return;
        }

        // Check if the two input array have "num" pairs of these data
        if (iNum != offsets.length || iNum != ptps.length) {
            LOG.error("THE SPECIFIC DATA TO EACH APPLICATION MUST HAVE " + num + " PAIRS OF THESE DATA");
            return;
        }

        // Check if experiment data is available
        //ArrayList<Map> exps = getObjectOr(data, "experiments", new ArrayList());
        //if (exps.isEmpty()) {
        //    LOG.error("NO EXPERIMENT DATA.");
        //    return;
        //} else {
        //    expData = exps.get(0);
        //    if (expData.isEmpty()) {
        //        LOG.error("NO EXPERIMENT DATA.");
        //       return;
        //    } else {
                Map mgnData = getObjectOr(data, "management", new HashMap());
                eventData = getObjectOr(mgnData, "events", new ArrayList());
        //    }

            // Check FEN_TOT is avalaible
            try {
                fen_tot = Double.parseDouble(getValueOr(data, "fen_tot", "")); // TODO will be replace by generic getting method
            } catch (Exception e) {
                LOG.error("FEN_TOT IS INVALID");
                return;
            }

            // Check planting date is avalaible
            events = new Event(eventData, "planting");
            if (events.isEventExist()) {
                pdate = getValueOr(events.getCurrentEvent(), "date", "");
                if (convertFromAgmipDateString(pdate) == null) {
                    LOG.error("PLANTING DATE IS MISSING");
                    return;
                }
            } else {
                LOG.error("PLANTING EVENT IS MISSING");
                return;
            }
        //}

        // Check input days and ptps
        try {
            fdates = new String[iNum];
            dPtps = new double[iNum];
            for (int i = 0; i < iNum; i++) {
                fdates[i] = dateOffset(pdate, offsets[i]);
                if (fdates[i] == null) {
                    LOG.error("INVALID OFFSET NUMBER OF DAYS [" + offsets[i] + "]");
                    return;
                }
                dPtps[i] = Double.parseDouble(ptps[i]);
            }
        } catch (Exception e) {
            LOG.error("PAIR DATA IS IN VALID [" + e.getMessage() + "]");
            return;
        }

        events.setEventType("fertilizer");
        for (int i = 0; i < iNum; i++) {
            // Create event map
            Map event = events.addEvent(fdates[i], true);
            event.put("fecd", fecd);
            event.put("feacd", feacd);
            event.put("fedep", fedep);
            event.put("feamn", String.format("%.0f", fen_tot * dPtps[i] / 100));
        }
    }

    /**
     * Organic matter applications include manure, crop residues, etc. As a
     * result, the organic matter application event is updated with missing
     * data.
     *
     * @param offset application date as days before (-) or after (+) planting
     * date (days)
     * @param omcd code for type of fertilizer added
     * @param omc2n C:N ratio for applied organic matter
     * @param omdep depth at which organic matter is incorporated (cm)
     * @param ominp percentage incorporation of organic matter (%)
     * @param data The experiment data holder
     */
    public static void getOMDistribution(String offset, String omcd, String omc2n, String omdep, String ominp, String dmr, HashMap expData) {

        String omamt;
        ArrayList<Map> eventData;
        Event events;
        String pdate;
        String odate;

        // Check if experiment data is available
        // ArrayList<Map> exps = getObjectOr(data, "experiments", new ArrayList());
        // if (exps.isEmpty()) {
        //     LOG.error("NO EXPERIMENT DATA.");
        //     return;
        // } else {
        //     Map expData = exps.get(0);
        //     if (expData.isEmpty()) {
        //         LOG.error("NO EXPERIMENT DATA.");
        //         return;
        //     } else {
                Map mgnData = getObjectOr(expData, "management", new HashMap());
                eventData = getObjectOr(mgnData, "events", new ArrayList());

        //    }
            // Get the omamt from the first? OM event
            Event omEvent = new Event(eventData, "organic_matter");
            omamt = (String) omEvent.getCurrentEvent().get("omamt");
            if (omamt == null || omamt.equals("")) {
                LOG.error("OMAMT IS NOT AVAILABLE");
                return;
            }
            //omamt = getValueOr(expData, "omamt", ""); // TODO will be replace by generic getting method

        //}

        // Get planting date and om_date
        events = new Event(eventData, "planting");
        pdate = (String) events.getCurrentEvent().get("date");
        if (pdate == null || pdate.equals("")) {
            LOG.error("PLANTING DATE IS NOT AVAILABLE");
            return;
        }
        odate = dateOffset(pdate, offset);
        if (odate == null) {
            LOG.error("INVALID OFFSET NUMBER OF DAYS [" + offset + "]");
            return;
        }

        BigDecimal decDmr;
        BigDecimal decOMC2N;
        BigDecimal start = new BigDecimal("100.0");
        try {
            decDmr = new BigDecimal(dmr);
            decOMC2N = new BigDecimal(omc2n);
        } catch (Exception ex) {
            LOG.error("INVALID VALUES FOR DMR and OMC2N");
            return;
        }
        
        BigDecimal omnpct = start.divide(decDmr, 2, RoundingMode.HALF_UP).divide(decOMC2N, 2, RoundingMode.HALF_UP);
        // Update organic material event
        events.setEventType("organic_matter");
        if (events.isEventExist()) {
            events.updateEvent("date", odate, false);
            events.updateEvent("omcd", omcd, false);
            events.updateEvent("omamt", omamt, false);
            events.updateEvent("omc2n", omc2n, false);
            events.updateEvent("omdep", omdep, false);
            events.updateEvent("ominp", ominp, false);
            events.updateEvent("omn%", omnpct.toString(), true);
        }
    }

    /**
     * Calculate Stable C (g[C]/100g[soil]) fraction distribution in soil layers
     * and save the result into initial condition layers
     *
     * @param som3_0 fraction of total soil organic C which is stable, at
     * surface (fraction)
     * @param pp depth of topsoil where maximum SOM3 fraction is relatively
     * constant (cm)
     * @param rd depth at which soil C is relatively stable (~98% stable C) (cm)
     * @param data The experiment data holder
     */
    public static void getStableCDistribution(String som3_0, String pp, String rd, HashMap data) {

        ArrayList<Map> icLayers;
        ArrayList<HashMap<String, Object>> soilLayers;
        double dSom3_0;
        double dPp;
        double dRd;
        double dK;
        double dSom2_0;
        double dF;
        double dSom3_fac;
        double[] dSllbs;
        double[] dSlocs;
        double mid;

        try {
            dSom3_0 = Double.parseDouble(som3_0);
            dPp = Double.parseDouble(pp);
            dRd = Double.parseDouble(rd);
            dK = Math.log(0.02) / (dRd - dPp);
            dSom2_0 = 0.95 * (1 - dSom3_0);
        } catch (Exception e) {
            LOG.error("INVALID INPUT FOR NUMBERIC VALUE");
            return;
        }

        soilLayers = getSoilLayer(data);
        if (soilLayers == null) {
            return;
        } else if (soilLayers.isEmpty()) {
            LOG.error("SOIL LAYER DATA IS EMPTY");
            return;
        } else {
            try {
                dSllbs = new double[soilLayers.size()];
                dSlocs = new double[soilLayers.size()];
                for (int i = 0; i < soilLayers.size(); i++) {
                    dSllbs[i] = Double.parseDouble(getObjectOr(soilLayers.get(i), "sllb", "").toString());
                    dSlocs[i] = Double.parseDouble(getObjectOr(soilLayers.get(i), "sloc", "").toString());
                }
            } catch (NumberFormatException e) {
                LOG.error("INVALID NUMBER FOR SLOC OR SLLB IN DATA [" + e.getMessage() + "]");
                return;
            }
        }

        // Check if initial condition layer data is available
        // ArrayList<Map> exps = getObjectOr(data, "experiments", new ArrayList());
        // if (exps.isEmpty()) {
        //     LOG.error("NO EXPERIMENT DATA.");
        //     return;
        // } else {
        //     Map expData = exps.get(0);
        //     if (expData.isEmpty()) {
        //         LOG.error("NO EXPERIMENT DATA.");
        //         return;
        //     } else {
                Map icData = getObjectOr(data, "initial_conditions", new HashMap());
                icLayers = getObjectOr(icData, "soilLayer", new ArrayList());
                if (icLayers.isEmpty()) {
                    LOG.error("NO INITIAL CONDITION DATA.");
                    return;
                } else if (icLayers.size() != soilLayers.size()) {
                    LOG.error("THE LAYER DATA IN THE INITIAL CONDITION SECTION IS NOT MATCHED WITH SOIL SECTION");
                    return;
                }
            //}
        //}

        double last = 0;
        for (int i = 0; i < icLayers.size(); i++) {
            mid = (dSllbs[i] + last) / 2;
            last = dSllbs[i];
            dF = getGrowthFactor(mid, dPp, dK, dSom2_0);
            dSom3_fac = 1 - Math.max(0.02, dF) / 0.95;
            icLayers.get(i).put("slsc", String.format("%.2f", dSlocs[i] * dSom3_fac));
//            LOG.debug((String)icLayers.get(i).get("icbl") + ", " + (String)icLayers.get(i).get("slsc"));
        }
    }
}