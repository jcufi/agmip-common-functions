package org.agmip.common;

import java.math.BigInteger;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

public class Functions {
    private static final SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyyMMdd");
    
    /**
     * Cannot instantiate this class.
     */
    private Functions() {}
    
    /**
     * Converts a numeric string to a {@code BigInteger}.
     *
     * This function first converts to a {@code BigDecimal} to make sure the base
     * number being used is accurate. By default, this method uses the {@code ROUND_HALF_UP} 
     * rounding method from BigDecimal. If the string cannot be converted, this method 
     * returns {@code null}
     *
     * @param numeric A numeric string (with or without decimals).
     *
     * @return {@code BigInteger} representation of the string or {@code null}.
     * 
     * @see BigDecimal
     */
    public static BigInteger numericStringToBigInteger(String numeric) {
        return numericStringToBigInteger(numeric, true);
    }

    /**
     * Converts a numeric string to a {@code BigInteger}.
     *
     * This function first converts to a {@code BigDecimal} to make sure the base
     * number being used is accurate. If {@code round} is set to <strong>true</strong>
     * this method uses the {@code ROUND_HALF_UP} rounding method from {@code BigDecimal}. 
     * Otherwise the decimal part is dropped. If the string cannot be converted, this method
     * returns {@code null}
     *
     * @param numeric A numeric string (with or without decimals).
     * @param round Use {@link BigDecimal#ROUND_HALF_UP} method.
     *
     * @return {@code BigInteger} representation of the string or {@code null}
     *
     * @see BigDecimal
     */
    public static BigInteger numericStringToBigInteger(String numeric, boolean round) {
        BigDecimal decimal;

        try {
            decimal = new BigDecimal(numeric);
        } catch (Exception ex) {
            return null;
        }
        
        if (round) {
            decimal = decimal.setScale(0, BigDecimal.ROUND_HALF_UP);
        }
        BigInteger integer = decimal.toBigInteger();
        return integer;
    }

    /**
     * Convert from AgMIP standard date string (YYYYMMDD) to a {@code Date}
     *
     * @param agmipDate AgMIP standard date string
     *
     * @return {@code Date} represented by the AgMIP date string or {@code null}
     */
    public static Date convertFromAgmipDateString(String agmipDate) {
        try {
            return dateFormatter.parse(agmipDate);
        } catch (ParseException ex) {
            return null;
        }
    }

    /**
     * Convert from {@code Date} to AgMIP standard date string (YYYYMMDD)
     *
     * @param date {@link Date} object
     *
     * @return an AgMIP standard date string representation of {@code date}.
     */
    public static String convertToAgmipDateString(Date date) {
        if (date != null) {
            return dateFormatter.format(date);
        } else {
            return null;
        }
    }
    
    /**
     * Offset an AgMIP standard date string (YYYYMMDD) by a set number of days.
     *
     * @param initial AgMIP standard date string
     * @param offset number of days to offset (can be positive or negative integer)
     *
     * @return AgMIP standard date string of <code>initial + offset</code>
     */
    public static String dateOffset(String initial, String offset) {
        Date date = convertFromAgmipDateString(initial);
        BigInteger iOffset;
        if (date == null) {
            // Invalid date
            return null;
        }
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(date);

        try {
            iOffset = new BigInteger(offset);
            cal.add(GregorianCalendar.DAY_OF_MONTH, iOffset.intValue());
        } catch (Exception ex) {
            return null;
        }
        return convertToAgmipDateString(cal.getTime());
    }

    /**
     * Offset a numeric string by another numeric string.
     * 
     * Any numeric string recognized by {@code BigDecimal} is supported.
     *
     * @param initial A valid number string
     * @param offset A valid number string
     *
     * @return a number string with the precision matching the highest precision argument.
     *
     * @see BigDecimal
     */
    public static String numericOffset(String initial, String offset) {
        BigDecimal number;
        BigDecimal dOffset;

        try {
            number = new BigDecimal(initial);
            dOffset = new BigDecimal(offset);
        } catch (Exception ex) {
            return null;
        }
        return number.add(dOffset).toString();
    }

    /**
     * Multiply two numbers together
     * 
     * Any numeric string recognized by {@code BigDecimal} is supported.
     * 
     * @param f1 A valid number string
     * @param f2 A valid number string
     *
     * @return <code>f1*f2</code>
     *
     * @see BigDecimal
     */
    public static String multiply(String f1, String f2) {
        BigDecimal factor1;
        BigDecimal factor2;

        try {
            factor1 = new BigDecimal(f1);
            factor2 = new BigDecimal(f2);
        } catch (Exception ex) {
            return null;
        }

        return factor1.multiply(factor2).toString();
    }
}
