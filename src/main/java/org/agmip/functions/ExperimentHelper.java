package org.agmip.functions;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.Map;
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
     * @param eDate Earliest planting date (yyyy-mm-dd)
     * @param lDate Latest planting date (yyyy-mm-dd)
     * @param rain Threshold rainfall amount (mm)
     * @param days Number of days of accumulation
     * @param wthData The HashMap of experiment (including weather data)
     *
     * @return The calculated first planting date, if not valid based on the
     * input data, will return ""
     */
    public static String getAutoPlantingDate(String eDate, String lDate, String rain, String days, Map expData) {

        String defRet = "";
        Map wthData;
        ArrayList<Map> dailyData;
        Calendar eDateCal = Calendar.getInstance();
        Calendar lDateCal = Calendar.getInstance();
        int intDays;
        int duration;
        double accRainAmtTotal;
        double accRainAmt = 0;

        // Validation for input parameters
        // Weather data check and try to get daily data
        if (expData.isEmpty()) {
            return defRet;
        } else {
            // Case for multiple data json structure
            if (expData.containsKey("weathers")) {
                ArrayList<Map> wths = getObjectOr(expData, "weathers", new ArrayList());
                if (wths.isEmpty()) {
                    LOG.error("NO WEATHER DATA.");
                    return defRet;
                } else {
                    wthData = wths.get(0);
                    if (wthData.isEmpty()) {
                        LOG.error("NO WEATHER DATA.");
                        return defRet;
                    } else {
                        dailyData = getObjectOr(wthData, "dailyWeather", new ArrayList());
                    }
                }
            } else {
                // Case for whole experiment data structure
                wthData = getObjectOr(expData, "weather", new LinkedHashMap());
                if (wthData.isEmpty()) {
                    dailyData = getObjectOr(expData, "dailyWeather", new ArrayList());
                } else {
                    // Case for weather only structure
                    dailyData = getObjectOr(wthData, "dailyWeather", new ArrayList());
                }
            }

            if (dailyData.isEmpty()) {
                LOG.error("EMPTY DAILY WEATHER DATA.");
                return defRet;
            }
        }

        // Check input dates
        if (!isValidDate(eDate, eDateCal, "-")) {
            LOG.error("INVALID EARLIST DATE:[" + eDate + "]");
            return defRet;
        }
        if (!isValidDate(lDate, lDateCal, "-")) {
            LOG.error("INVALID LATEST DATE:[" + lDate + "]");
            return defRet;
        }
        if (eDateCal.after(lDateCal)) {
            LOG.error("LASTEST DATE [" + lDate + "] IS BEFORE EARLIST DATE:[" + eDate + "]");
            return defRet;
        } else {
            duration = (int) ((lDateCal.getTimeInMillis() - eDateCal.getTimeInMillis()) / 86400000);
        }

        // Check Number of days of accumulation
        try {
            intDays = Integer.parseInt(days);
        } catch (Exception e) {
            LOG.error("INVALID NUMBER FOR NUMBER OF DAYS OF ACCUMULATION");
            return defRet;
        }
        if (intDays <= 0) {
            LOG.error("NON-POSITIVE NUMBER FOR NUMBER OF DAYS OF ACCUMULATION");
            return defRet;
        }

        // Check Threshold rainfall amount
        try {
            accRainAmtTotal = Double.parseDouble(rain);
        } catch (Exception e) {
            LOG.error("INVALID NUMBER FOR THRESHOLD RAINFALL AMOUNT");
            return defRet;
        }
        if (accRainAmtTotal <= 0) {
            LOG.error("NON-POSITIVE NUMBER FOR THRESHOLD RAINFALL AMOUNT");
            return defRet;
        }

        // Find the first record which is the ealiest date
        int first = -1;
        int last = dailyData.size();
        for (int i = 0; i < last; i++) {
            String date = getValueOr(dailyData.get(i), "w_date", "");
            if (isSameDate(date, eDate, "-")) {
                first = i;
                break;
            }
        }
        if (first < 0) {
            LOG.error("NO VALID DAILY DATA FOR SEARCH WINDOW");
            return defRet;
        }
        
        // Check first n days
        last = Math.min(first + intDays + 1, dailyData.size());
        for (int i = first; i < last; i++) {

            try {
                accRainAmt += Double.parseDouble(getValueOr(dailyData.get(i), "rain", "0"));
            } catch (Exception e) {
                continue;
            }
            if (accRainAmt == accRainAmtTotal) {
                return getValueOr(dailyData.get(i), "w_date", "");
            }
        }
        
        // If the window size is no larger than n
        if (first + intDays > dailyData.size()) {
            LOG.warn("NO APPROPRIATE DATE WAS FOUND.");
            return defRet;
        }

        // Check following days
        last = Math.min(first + duration, dailyData.size());
        for (int i = first + intDays; i < last; i++) {

            try {
                accRainAmt -= Double.parseDouble(getValueOr(dailyData.get(i - intDays), "rain", "0"));
                accRainAmt += Double.parseDouble(getValueOr(dailyData.get(i), "rain", "0"));
            } catch (Exception e) {
                continue;
            }
            if (accRainAmt == accRainAmtTotal) {
                return getValueOr(dailyData.get(i), "w_date", "");
            }
        }
        
        LOG.warn("NO APPROPRIATE DATE WAS FOUND.");
        return defRet;
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
            out.set(Calendar.YEAR, Integer.parseInt(dates[0]));
            out.set(Calendar.MONTH, Integer.parseInt(dates[1]) - 1);
            out.set(Calendar.DATE, Integer.parseInt(dates[2]));
        } catch (Exception e) {
            return false;
        }

        return true;
    }

    /**
     * To check if two input date string is same date with no matter about 2nd input's separator
     * 
     * @param date1 1st input date string with format yyyymmdd
     * @param date2 2nd input date string with format yyyymmdd or yyyy-mm-dd
     * @param separator The separator used in 2nd string
     * @return comparison result
     */
    private static boolean isSameDate(String date1, String date2, String separator) {
        return date1.equals(date2) || date1.equals(date2.replaceAll(separator, ""));
    }
}
