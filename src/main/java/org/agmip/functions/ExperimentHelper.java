package org.agmip.functions;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
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
        Window[] windows;

        // Validation for input parameters
        // Weather data check and try to get daily data
        if (data.isEmpty()) {
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
                try {
                    expDur = Integer.parseInt(getValueOr(expData, "exp_dur", "1"));
                } catch (Exception e) {
                    expDur = 1;
                }
                windows = new Window[expDur];
            }
        } else {
            return;
        }

        if (eventData.isEmpty()) {
            LOG.warn("EMPTY EVENT DATA.");
            event = new Event(new ArrayList());
        } else {
            event = new Event(eventData);
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
        int start = getDailyRecIndex(dailyData, eDate, 0, 0);
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
                    event.updatePlEvent(getValueOr(dailyData.get(j), "w_date", ""));
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
                event.removePlEvent();
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
                    event.updatePlEvent(getValueOr(dailyData.get(j), "w_date", ""));
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
     * To handle the planting event in the event data array
     */
    private static class Event {

        public int next = -1;
        public Map template;
        public ArrayList<Map> events;

        /**
         * Constructor
         *
         * @param events The event data array
         */
        public Event(ArrayList<Map> events) {
            this.events = events;
            getNextPlEventIndex();
            template = new HashMap();
            if (next < events.size()) {
                template.putAll(events.get(next));
            }
        }

        /**
         * Remove the current planting event data if available
         */
        public void removePlEvent() {
            if (next < events.size()) {
                events.remove(next);
                next--;
                getNextPlEventIndex();
            }
        }

        /**
         * Update the current planting event with given pdate, if current event
         * not available, add a new one into array
         *
         * @param pdate The planting date
         */
        public void updatePlEvent(String pdate) {
            if (next < events.size()) {
                events.get(next).put("date", pdate);
            } else {
                Map tmp = new HashMap();
                tmp.putAll(template);
                tmp.put("date", pdate);
                events.add(tmp);
            }
            getNextPlEventIndex();
        }

        /**
         * Move index to the next planting event
         */
        private void getNextPlEventIndex() {
            for (int i = next + 1; i < events.size(); i++) {
                String evName = getValueOr(events.get(i), "event", "");
                if (evName.equals("planting")) {
                    next = i;
                    return;
                }
            }
            next = events.size();
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
            out.set(Calendar.MONTH, Integer.parseInt(dates[0]) - 1);
            out.set(Calendar.DATE, Integer.parseInt(dates[1]));
        } catch (Exception e) {
            try {
                out.set(Calendar.MONTH, Integer.parseInt(date.substring(0, 2)) - 1);
                out.set(Calendar.DATE, Integer.parseInt(date.substring(2, 4)));
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
     * Offset a value by a constant.
     *
     * @param initial Initial value to offset (either a static number OR date OR
     * variable)
     * @param offset The amount to offset the <code>initial</code>
     */
}