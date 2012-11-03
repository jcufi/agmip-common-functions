package org.agmip.common;

import java.util.Date;

import static org.agmip.common.Functions.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.After;
import static org.junit.Assert.*;

public class FunctionsTest {

    @Test
    public void numericStringNonRoundedTest() {
        int test = 1234;
        assertEquals("Numeric conversion failed", test, numericStringToBigInteger("1234.56", false).intValue());
    }
    
    @Test
    public void numericStringRoundedTest() {
        int test = 1235;
        assertEquals("Numeric conversion failed", test, numericStringToBigInteger("1234.56").intValue());
    }

    @Test
    public void toDateTest() {
        Date test = new Date(2012-1900, 0, 1);
        Date d = convertFromAgmipDateString("20120101");
        assertEquals("Dates not the same", test, d);
    }

    @Test
    public void toStringTest() {
        String test = "20120101";
        String d = convertToAgmipDateString(new Date(2012-1900, 0, 1));
        assertEquals("Dates not the same", test, d);
    }

    @Test
    public void failToDateTest() {
        Date d = convertFromAgmipDateString("1");
        assertNull("Converted invalid date", d);
    }

    @Test
    public void dateOffsetTest() {
        String test = "20120219";
        String initial = "20120212";
        String offset="7";

        assertEquals("Date offset incorrect", test, dateOffset(initial, offset));
    }

    @Test
    public void dateOffsetMonthBoundary() {
        String test = "20120703";
        String initial = "20120628";
        String offset = "5";

        assertEquals("Date offset incorrect", test, dateOffset(initial, offset));
    }

    @Test
    public void dateOffsetYearBoundary() {
        String test = "20120101";
        String initial = "20111225";
        String offset = "7";

        assertEquals("Date offset incorrect", test, dateOffset(initial, offset));
    }

    @Test
    public void dateOffsetReverseOffset() {
        String test = "20120101";
        String initial = "20120105";
        String offset = "-4";

        assertEquals("Date offset incorrect", test, dateOffset(initial, offset));
    }

    @Test
    public void failDateOffsetBadDate() {
        assertNull("Offset invalid date", dateOffset("1232", "1"));
    }

    @Test
    public void failDateOffsetBadOffset() {
        assertNull("Offset invalid offset", dateOffset("20120101", "abc"));
    }

    @Test
    public void failDateOffsetBadNumericOffsetTest() {
        assertNull("Offset invalid offset", dateOffset("20120101", "1.0"));
    }

    @Test 
    public void failDateOffsetBadNumericOffset2Test() {
        assertNull("Offset invalid offset", dateOffset("20120101", "1.2"));
    }
    
    @Test
    public void numericOffsetTest() {
        String test = "12.34";
        String initial = "11.22";
        String offset  = "1.12";
        
        assertEquals("Numeric offset incorrect", test, numericOffset(initial, offset));
    }
    
    @Test
    public void integerOffsetTest() {
        String test = "12";
        String initial = "11";
        String offset = "1";
        
        assertEquals("Numeric offset incorrect", test, numericOffset(initial, offset));
    }
    
    @Test
    public void mixedNumericOffsetTest() {
        String test = "12.34";
        String initial = "11";
        String offset = "1.34";
        
        assertEquals("Numeric offset incorrect", test, numericOffset(initial, offset));
    }
    
    @Test
    public void failedNumericOffsetBadInitialTest() {
        assertNull("Offset invalid initial", numericOffset("abc", "12.34"));
    }
    
    @Test
    public void failedNumericOffsetBadOffsetTest() {
        assertNull("Offset invalid offset", numericOffset("12.34", "abc"));
    }
    
    @Test
    public void numericNegativeOffsetTest() {
        String test = "12.34";
        String initial = "23.45";
        String offset = "-11.11";
        
        assertEquals("Numeric offset incorrect", test, numericOffset(initial, offset));
    }

    @Test
    public void multiplySimple() {
        String test="12.34";
        String f1 = "1234";
        String f2 = ".01";

        assertEquals("Multiply incorrect", test, multiply(f1, f2));
    }

    @Test
    public void mutliplyIntentionalFailure() {
        String f1 = "Hi";
        String f2 = "12";

        assertNull("This shouldn't work", multiply(f1, f2));
    }
}
