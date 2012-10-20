package org.agmip.functions;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Map;
import org.agmip.util.JSONAdapter;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Meng Zhang
 */
public class SoilHelperTest {

    URL resource;

    @Before
    public void setUp() throws Exception {
        resource = this.getClass().getResource("/ufga8201_multi.json");
    }

    @Test
    public void testGetRootDistribution() throws IOException, Exception {
        String line;
        String pp = "8";
        String[] expected = {"1.0000", "1.0000", "1.0000", "0.8694", "0.8694", "0.8694", "0.8694", "0.8694"};
        String[] acctual = null;

        BufferedReader br = new BufferedReader(
                new InputStreamReader(
                new FileInputStream(resource.getPath())));

        if ((line = br.readLine()) != null) {
            Map<String, ArrayList<Map>> data = JSONAdapter.fromJSON(line);
            acctual = SoilHelper.getRootDistributionStr(data, pp);
        }

        for (int i = 0; i < expected.length; i++) {
            assertEquals("getRootDistribution: normal case", expected[i], acctual[i]);
        }
    }
}
