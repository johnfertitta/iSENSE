/*
 * Copyright (c) 2009, iSENSE Project. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer. Redistributions in binary
 * form must reproduce the above copyright notice, this list of conditions and
 * the following disclaimer in the documentation and/or other materials
 * provided with the distribution. Neither the name of the University of
 * Massachusetts Lowell nor the names of its contributors may be used to
 * endorse or promote products derived from this software without specific
 * prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH
 * DAMAGE.
 */
package edu.uml.cs.isense;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TimeZone;
import java.util.Vector;

import android.util.Log;
import net.java.dev.eval.Expression;


/**
 * The PINPoint converter takes a byte array (data record) and applies 
 * all the conversions necessary to get useful data from the PINPoint.
 *
 * The user only has access to the convert all function which will call
 * the others to get the correct values.
 *
 * @author James Dalphond <jdalphon@cs.uml.edu>
 */
public class PinPointConverter {

    private NumberFormat formatter = new DecimalFormat("#0.00");
    private byte[] record;
    private int rate;
    private boolean counter = false;
    private Expression externalConversion;

	public static String conversionsText = 
	"0,Counter Type,Counts,x\n" +
	"1,Voltage,Voltage,x*3.3/1023\n" +
	"2,PINPoint Temperature Probe,Temperature Probe,(1587.787/x) - (0.00008071358 * x * x) + 45.75087\n" +
	"3,Vernier Temperature Probe,Temperature Probe, (4548.5815/x) - (0.0001010072228 * x * x) + 19.55366\n" +
	"4,Vernier UV(A) Sensor,(mW/m^2),((3940*x*3)/(1024))\n" +
	"5,Vernier CO2 Gas Sensor - Low  Range (ppm),CO2 Low,((x*2500)/256)\n" +
	"6,Vernier CO2 Gas Sensor - High Range (ppm),CO2 High,((x*25000)/256)\n" +
	"7,Vernier pH Sensor,PH,-10.5275*((x*3.3)/1023)+29.279\n" +
	"8,Vernier Salinity Sensor,ppt,0.0224*((x*3.3)/1023)+1.51";

    
    public PinPointConverter(int conversionType, int sampleRate, boolean replaceTime) {
        setExternalConversion(conversionType);
        if(conversionType == 0){counter = true;}
        Log.d("conversion", "type = " + conversionType + " rate = " + sampleRate);
        rate = (sampleRate / 1000);
    }



    /**
     * The latitude Conversion will take the raw values from the message the pinpoint
     * passes to the interface, unpacks its part and converts to human readable form.
     *
     * @return string
     */
    private String latitudeConversion() {
        int lat = record[1];
        if (lat >= 0) {
            lat = lat & 255;
        }
        lat = (lat << 8) + (record[0] & 255);
        float flat = Float.parseFloat(("" + lat + "." + (((record[3] & 255) << 8) + (record[2] & 255))));
        int degs = (int) flat / 100;
        float min = flat - degs * 100;
        String retVal = "" + (degs + min / 60);

        if (retVal.compareTo("200.0") == 0) {
            return "";
        } else {
            return retVal;
        }
    }

    /**
     * The longitude Conversion will take the raw values from the message the pinpoint
     * passes to the interface, unpacks its part and converts to human readable form.
     *
     * @return string
     */
    private String longitudeConversion() {
        int lon = record[5];
        if (lon >= 0) {
            lon = lon & 255;
        }
        lon = (lon << 8) + (record[4] & 255);
        float flon = Float.parseFloat("" + lon + "." + (((record[7] & 255) << 8) + (record[6] & 255)));
        int degs = (int) flon / 100;
        float min = flon - degs * 100;
        String retVal = "" + (degs + min / 60);

        if (retVal.compareTo("200.0") == 0) {
            return "";
        } else {
            return retVal;
        }
    }

    /**
     * The altitude Conversion will take the raw values from the message the pinpoint
     * passes to the interface, unpacks its part and converts to human readable form.
     *
     * @return string
     */
    private String altitudeConversion() {
        int reading = ((record[9] & 255) << 8) + (record[8] & 255);
        if (reading == 60000) {
            return " ";
        } else {
            return reading + "";
        }
    }

    /**
     * The pedometer Conversion will take the raw values from the message the pinpoint
     * passes to the interface, unpacks its part and converts to human readable form.
     *
     * @return string
     */
    private String pedometerConversion() {
        return (record[20] & 255) + "";
    }

    /**
     * The external Conversion will take the raw values from the message the pinpoint
     * passes to the interface, unpacks its part and converts to human readable form.
     *
     * @return string
     */
    private String externalConversion() {
        int reading = ((record[19] & 255) + ((record[21] & 0x03) << 8));

        Map<String, BigDecimal> variables = new HashMap<String, BigDecimal>();
        variables.put("x", new BigDecimal(reading));
        BigDecimal result = externalConversion.eval(variables);

        String x = result + "";

        if(this.counter){
            System.out.println(rate);
            x = Integer.parseInt(x) * (60/rate) + "";
        }

        return x;
    }

    /**
     * The sound Conversion will take the raw values from the message the pinpoint
     * passes to the interface, unpacks its part and converts to human readable form.
     *
     * @return string
     */
    private String soundConversion() {
        int reading = (record[18] & 255) + ((record[21] & 0x0C) << 6);
        return (reading * 100) / 1024 + "";
    }

    /**
     * The temperature Conversion will take the raw values from the message the pinpoint
     * passes to the interface, unpacks its part and converts to human readable form.
     *
     * @return string
     */
    private String tempConversion() {
        int reading = ((record[17] & 255)  + ((record[21] & 0x30) << 4));

        double voltage = (reading * 3.3) / 1024;
        return formatter.format((voltage - .6) / .01);

    }

    /**
     * The light Conversion will take the raw values from the message the pinpoint
     * passes to the interface, unpacks its part and converts to human readable form.
     *
     * @return string
     */
    private String lightConversion() {
        int reading = ((record[16] & 255) + ((record[21] & 0xC0) << 2));
        //return reading + "";
        return formatter.format(((reading * reading) * (-.0009)) + (2.099 * reading));
    }

    /**
     * The acceleration Conversion will take the raw values from the message the pinpoint
     * passes to the interface, unpacks its part and converts to human readable form.
     *
     * @return string
     */
    private String accelConversion() {
        int reading = record[15] & 255;
        double voltage = (reading * 3.33) / 256;
        //return formatter.format(reading);
        return formatter.format((voltage / .290)*9.806);
    }

    /**
     * The time Conversion will take the raw values from the message the pinpoint
     * passes to the interface, unpacks its part and converts to human readable form.
     *
     * The conversion reads in the system time and converts to the correct time zone.
     *
     * @return string
     */
    private String timeConversion() {
        Calendar local = Calendar.getInstance();
        Calendar GMT = Calendar.getInstance(TimeZone.getTimeZone("GMT"));

        //Time from the PINPoint
        int hours, minutes, seconds, day, month, year;
        hours = (record[10] & 0xF8) >> 3;
        minutes = ((record[10] & 0x07) << 3) + ((record[11] & 0xE0) >> 5);
        seconds = ((record[11] & 0x1F) << 1) + ((record[12] & 0x80) >> 7);
        seconds += (record[12] & 0x7F) / 100;
        day = (record[13] & 0xF8) >> 3;
        month = ((record[13] & 0x07) << 1) + ((record[14] & 0x80) >> 7);
        year = (record[14] & 0x7F) + 2000;
        
        month--; //Months in java are 0-11, PINPoint = 1-12;

        //Set GMTs time to be the time from the PINPoint
        GMT.set(Calendar.DAY_OF_MONTH, day);
        GMT.set(Calendar.MONTH, month);
        GMT.set(Calendar.YEAR, year);
        GMT.set(Calendar.HOUR_OF_DAY, hours);
        GMT.set(Calendar.MINUTE, minutes);
        GMT.set(Calendar.SECOND, seconds);

        //Local is set to GMTs time but with the correct timezone
        local.setTimeInMillis(GMT.getTimeInMillis());

        //Set Local time to be the time converted from GMT
        int lHours, lMinutes, lSeconds, lDay, lMonth, lYear;
        lHours = local.get(Calendar.HOUR_OF_DAY);
        lMinutes = local.get(Calendar.MINUTE);
        lSeconds = local.get(Calendar.SECOND);
        lDay = local.get(Calendar.DAY_OF_MONTH);
        lMonth = local.get(Calendar.MONTH);

        lMonth++; //Months in java are 0-11, humans read 1-12

        lYear = local.get(Calendar.YEAR);

        return hR(lMonth) + "/" + hR(lDay) + "/" + lYear + " " + hR(lHours) + ":" + hR(lMinutes) + ":" + hR(lSeconds);
    }

    /**
     * Used to convert the time variables to be more user friendly. (Human readable)
     *
     * @param x
     * @return
     */
    private String hR(int x) {
        if (x < 10) {
            return "0" + x;
        }
        return "" + x;
    }

    /**
     * Called for each datapoint builds up a full record from the
     * converted input.
     *
     * @param input
     * @return
     */
    public String convertAll(byte[] input) {
        record = input;
        String result;
        result = timeConversion() + "," +
                latitudeConversion() + "," +
                longitudeConversion() + "," +
                accelConversion() + "," +
                lightConversion() + "," +
                tempConversion() + "," +
                soundConversion() + "," +
                externalConversion() + "," +
                pedometerConversion() + "," +
                altitudeConversion();

        return result;
    }


    /**
     * Since the external sensors can change we use an external conversion file that
     * can be edited. Because of this we must read in the conversions file, get the
     * correct one from the file, and use that conversion.
     *
     * @param conversionType
     * @throws MissingLogFileException
     */
    private void setExternalConversion(int conversionType) {

        //Vector of conversions.
        Vector<String[]> conversions = new Vector<String[]>();

        String line;

        //Create a buffered reader to read in the file.
        BufferedReader in = new BufferedReader(new StringReader(conversionsText));

        //Read in every line to memory.
        try {
			while ((line = in.readLine()) != null) {
			    conversions.add(line.split(","));
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        Iterator<String[]> iter = conversions.iterator();
        String[] temp = new String[4];

        //Iterate through the vector to find the correct conversion function.
        while (iter.hasNext()) {
            temp = (String[]) iter.next();

            if (Integer.parseInt(temp[0]) == conversionType) {
         
                break;
            }
        }

        //Create the expression from the conversion that was chosen from the file.
        Expression exp = new Expression(temp[3]);
        externalConversion = exp;

    }
}
