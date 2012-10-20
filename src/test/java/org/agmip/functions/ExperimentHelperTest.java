package org.agmip.functions;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import org.agmip.util.JSONAdapter;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Meng Zhang
 */
public class ExperimentHelperTest {

    URL resource;
    URL resource2;

    @Before
    public void setUp() throws Exception {
        resource = this.getClass().getResource("/ufga8201_multi.json");
        resource2 = this.getClass().getResource("/machakos.json");
    }

    @Test
    public void testGetAutoPlantingDate() throws IOException, Exception {
        String line;
        String startDate = "01-15";
        String endDate = "02-28";
        String accRainAmt = "29.2";
        String dayNum = "4";
        String expected_1 = "19820204";
        int expected_2 = 5;
        String acctual_1 = "";
        int acctual_2 = 0;

        BufferedReader br = new BufferedReader(
                new InputStreamReader(
                new FileInputStream(resource.getPath())));

        if ((line = br.readLine()) != null) {
            Map<String, ArrayList<Map>> data = JSONAdapter.fromJSON(line);
            Map<String, Object> expData = data.get("experiments").get(0);
            expData.put("exp_dur", "2");
            ExperimentHelper.getAutoPlantingDate(startDate, endDate, accRainAmt, dayNum, data);
            Map<String, ArrayList> mgnData = (Map) expData.get("management");
            ArrayList<Map<String, String>> events = mgnData.get("events");
            acctual_1 = events.get(0).get("date");
            acctual_2 = events.size();
        }
        
        assertEquals("getAutoPlantingDate: normal case", expected_1, acctual_1);
        assertEquals("getAutoPlantingDate: no date find case", expected_2, acctual_2);
        
    }
    
    

    @Test
    public void testGetAutoPlantingDate_machakos() throws IOException, Exception {
        String line;
        String startDate = "01-15";
        String endDate = "02-28";
        String accRainAmt = "9";
        String dayNum = "6";
        String expected_1 = "19800124";
        String expected_2 = "19810218";
        int expected_3 = 3;
        String acctual_1 = "";
        String acctual_2 = "";
        int acctual_3 = 0;

        BufferedReader br = new BufferedReader(
                new InputStreamReader(
                new FileInputStream(resource2.getPath())));

        if ((line = br.readLine()) != null) {
            
            Map<String, ArrayList<Map>> data = new LinkedHashMap<String, ArrayList<Map>>();
            Map<String, Object> expData =  JSONAdapter.fromJSON(line);
            data.put("experiments", new ArrayList());
            data.put("weathers", new ArrayList());
            data.get("experiments").add(expData);
            data.get("weathers").add((Map) expData.get("weather"));
            expData.put("exp_dur", "3");
            ExperimentHelper.getAutoPlantingDate(startDate, endDate, accRainAmt, dayNum, data);
            Map<String, ArrayList> mgnData = (Map) expData.get("management");
            ArrayList<Map<String, String>> events = mgnData.get("events");
            acctual_1 = events.get(0).get("date");
            acctual_2 = events.get(2).get("date");
            acctual_3 = events.size();
        }
        
        assertEquals("getAutoPlantingDate: normal case", expected_1, acctual_1);
        assertEquals("getAutoPlantingDate: copy case", expected_2, acctual_2);
        assertEquals("getAutoPlantingDate: no date find case", expected_3, acctual_3);
        
    }
}
