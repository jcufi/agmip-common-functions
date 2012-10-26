package org.agmip.functions;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import org.agmip.util.JSONAdapter;
import static org.agmip.util.MapUtil.*;
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
        String m = "1";
        String pp = "20";
        String rd = "180";
        String[] expected = {"1.000", "1.000", "0.941", "0.543", "0.261", "0.125", "0.060", "0.029"};
        ArrayList<HashMap> acctual = null;

        BufferedReader br = new BufferedReader(
                new InputStreamReader(
                new FileInputStream(resource.getPath())));

        if ((line = br.readLine()) != null) {
            HashMap data = JSONAdapter.fromJSON(line);
            SoilHelper.getRootDistribution(m, pp, rd, data);
            acctual = getObjectOr((HashMap) getObjectOr(data, "soils", new ArrayList()).get(0), "soilLayer", new ArrayList());
//            File f = new File("RootDistJson.txt");
//            BufferedOutputStream bo = new BufferedOutputStream(new FileOutputStream(f));
//            bo.write(JSONAdapter.toJSON(data).getBytes());
//            bo.close();
//            f.delete();
        }

        for (int i = 0; i < expected.length; i++) {
            assertEquals("getRootDistribution: normal case", expected[i], (String) acctual.get(i).get("slrgf"));
        }
    }
}
