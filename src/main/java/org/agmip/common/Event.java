package org.agmip.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import static org.agmip.util.MapUtil.*;

/**
 * To handle the selected event in the event data array
 *
 * @author Meng Zhang
 */
public class Event {

    private int next = -1;
    private Map template;
    private ArrayList<Map> events;
    private String eventType;

    /**
     * Constructor
     *
     * @param events The event data array
     * @eventType The type of events which will be handled
     */
    public Event(ArrayList<Map> events, String eventType) {
        this.events = events;
        this.eventType = eventType;
        getNextEventIndex();
        setTemplate();
    }

    /**
     * Set event type and refresh the internal attributes
     * @param eventType The event type for handling
     */
    public void setEventType(String eventType) {
        this.eventType = eventType;
        next = -1;
        getNextEventIndex();
        setTemplate();
    }

    /**
     * Set template with selected event type
     */
    public void setTemplate() {
        template = new HashMap();
        if (next < events.size()) {
            template.putAll(events.get(next));
        }
        template.put("event", eventType);
    }

    /**
     * Remove the current planting event data if available
     */
    public void removeEvent() {
        if (next < events.size()) {
            events.remove(next);
            next--;
            getNextEventIndex();
        }
    }

    /**
     * Update the current event with given key and value, if current event not
     * available, add a new one into array
     *
     * @param key The variable's key for a event
     * @param value The input value for the key
     */
    public void updateEvent(String key, String value) {
        updateEvent(key, value, true, true);
    }

    /**
     * Update the current event with given key and value, if current event not
     * available, add a new one into array
     *
     * @param key The variable's key for a event
     * @param value The input value for the key
     * @toNext True for got to next event
     */
    public void updateEvent(String key, String value, boolean toNext) {
        updateEvent(key, value, true, toNext);
    }

    /**
     * Update the current event with given key and value, if current event not
     * available, add a new one into array
     *
     * @param key The variable's key for a event
     * @param value The input value for the key
     * @useTemp True for use template to create new event
     * @toNext True for got to next event
     */
    public void updateEvent(String key, String value, boolean useTemp, boolean toNext) {
        if (next < events.size()) {
            getCurrentEvent().put(key, value);
        } else {
            Map tmp;
            if ("date".equals(key)) {
                tmp = addEvent(value, useTemp);
            } else {
                tmp = addEvent(null, useTemp);
            }
            tmp.putAll(template);
            tmp.put(key, value);
        }
        if (toNext) {
            getNextEventIndex();
        }
    }

    /**
     * Add a new event into array with selected event type and input date.
     * 
     * @param date The event date
     * @param useTemp True for using template to create new data
     * @return The generated event data map
     */
    public Map addEvent(String date, boolean useTemp) {
        Map ret = new HashMap();
        if (useTemp) {
            ret.putAll(template);
        } else {
            ret.put("event", eventType);
        }
        ret.put("date", date);
        getInertIndex(ret);
        events.add(next, ret);
        return ret;
    }

    /**
     * Move index to the next planting event
     */
    private void getNextEventIndex() {
        for (int i = next + 1; i < events.size(); i++) {
            String evName = getValueOr(events.get(i), "event", "");
            if (evName.equals(eventType)) {
                next = i;
                return;
            }
        }
        next = events.size();
    }

    /**
     * Check if the selected event is existed in the array
     *
     * @return
     */
    public boolean isEventExist() {
        return next >= 0 && next < events.size();
    }

    /**
     * Get the current pointed event
     *
     * @return
     */
    public Map getCurrentEvent() {
        return events.get(next);
    }

    /**
     * Find out the insert position for the new event. If date is not available
     * for the new event, will return the last position of array
     *
     * @param event The new event data
     */
    private void getInertIndex(Map event) {
        int iDate;
        try {
            iDate = Integer.parseInt(getValueOr(event, "date", ""));
        } catch (Exception e) {
            next = events.size();
            return;
        }
        for (int i = isEventExist() ? next : 0; i < events.size(); i++) {
            try {
                if (iDate < Integer.parseInt(getValueOr(events.get(i), "date", "0"))) {
                    next = i;
                    return;
                }
            } catch (Exception e) {
            }
        }

        next = events.size();
        return;
    }
}
