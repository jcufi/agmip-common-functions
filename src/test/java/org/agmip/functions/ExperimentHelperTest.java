package org.agmip.functions;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Map;
import org.agmip.util.JSONAdapter;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.ComparisonFailure;

/**
 *
 * @author Meng Zhang
 */
public class ExperimentHelperTest {

    URL resource;

    @Before
    public void setUp() throws Exception {
        resource = this.getClass().getResource("/ufga8201_multi.json");
    }

    @Test
    public void testGetAutoPlantingDate() throws IOException, Exception {
        String line;
        String startDate = "1982-01-15";
        String endDate = "1982-02-28";
        String accRainAmt = "29.2";
        String dayNum = "4";
        String expected = "19820204";
        String acctual = "";

        BufferedReader br = new BufferedReader(
                new InputStreamReader(
                new FileInputStream(resource.getPath())));

        if ((line = br.readLine()) != null) {
            Map data = JSONAdapter.fromJSON(line);
            acctual = ExperimentHelper.getAutoPlantingDate(startDate, endDate, accRainAmt, dayNum, data);
        }
        
        assertEquals("getAutoPlantingDate: normal case", expected, acctual);
        
    }
}
