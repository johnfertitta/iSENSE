package edu.uml.cs.isense;
/* Copyright (c) 2009, iSENSE Project. All rights reserved.
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
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Iterator;
import java.util.Vector;


/**
 * The pinpointInterface class gives an interface to the pinpoint API to keep
 * the API separate from the rest of the code. 
 * 
 * @author James Dalphond <jdalphon@cs.uml.edu>
 */
public class pinpointInterface {

    // Vector of datapoints.
    private Vector<String> dataPoints;
    // Comunications protocol for the Pinpoint.
    public PinComm pinpoint;
    // The number of entries in a page of flash memory.
    private static final int PAGE_SIZE = 22;
    // The number of pages of flash memory.
    private static final int FLASH_PAGES = 4096;

    public boolean streamEnabled = true;

    private PinPointConverter conv;

    
    /**
     * The constructor for the interface will try to initiate the
     * connection over usb to the pinpoint. This will use the pinpoint 
     * communications protocol and report back to the UI if there was a 
     * connection problem.
     * 
     */
    public pinpointInterface(BluetoothService bts) {
    	pinpoint = PinComm.instantiate(bts);
            
        // Initiate the vector in which the data will be stored. 
        dataPoints = new Vector<String>();

        conv = new PinPointConverter(pinpoint.getExternalType(), pinpoint.getSampleRate(), true);
    }

    /**
     * Requests pages one at a time appends the response from each page to the 
     * datapoints vector.
     * 
     * @return Vector<String>
     * @throws NoDataException
     * @throws IOException
     */
    public Vector<String> getRecords() {

        String[] records;

        int i, j = 0;

        boolean stop = false;

        try {
            //Test each of the pages
            for (i = 0; i < FLASH_PAGES; i++) {
                if (stop == true) {
                    break;
                }

                //Lets the user know what pages are being requested.
                System.out.println("Requesting page:" + i + "\thigh:" + ((byte) ((i >> 8) & 0xFF)) + "\tlow:" + ((byte) (i & 0xFF)));

                //Temporary copy of the current page of flash memory.
                records = pinpoint.getPages(i);

                //Test the page to see if there is any more information to be had.
                for (j = 0; j < PAGE_SIZE; j++) {

                    //There is no more information stored on the pinpoint so stop reading.
                    if (records[j].compareToIgnoreCase("EOF") == 0) {
                        stop = true;
                        break;

                        //Have not reached the end of records so add the datapoint to the vector.
                    } else {
                        dataPoints.add(records[j]);
                    }
                }

            }
        } catch (Exception e) {
            System.err.println("Exception caught " + e.getMessage());
        }

        // Test to see if any data was actually recorded.
        if (dataPoints.isEmpty()) {
            // Let the UI know that no data was found on the pinpoint.
        }

        // Return the datapoints up to the UI for display.
        return dataPoints;
    }

    public String requestDataStream() throws IOException {
        byte[] record = pinpoint.requestDataStream();

        String recordConverted = conv.convertAll(record);
        return recordConverted;
    }

    //Allows a single page to be read back. This page is unformatted and not converted. 
    public byte[][] getSinglePage(int page) throws IOException {
        byte[][] records = pinpoint.requestPage((byte) ((page >> 8) & 0xFF), (byte) (page & 0xFF));
        return records;
    }

    //Allows a single page to be read back in converted form.
    public String[] getSingleConvertedPage(int page) throws IOException {

        byte[][] records = pinpoint.requestPage((byte) ((page >> 8) & 0xFF), (byte) (page & 0xFF));

        PinPointConverter conv = new PinPointConverter(pinpoint.getExternalType(), pinpoint.getSampleRate(), false);

        String[] tempArray = new String[PAGE_SIZE];

        //If there is data convert it, else report end of file.
        for (int i = 0; i < PAGE_SIZE; i++) {
            if (pinpoint.lastRecord(records[i]) == false) {
                tempArray[i] = conv.convertAll(records[i]);
            } else {
                tempArray[i] = "EOF";
            }
        }

        //Data is converted or end of file is found. Return the array.
        return tempArray;

    }

    // Disconnect the pointpoint and shut down communications cleanly.
    public void disconnect() {
        try {
            pinpoint.close();
        } catch (IOException ex) {
            System.out.println("Error disconnecting pinpoint called from whithin pinpointInterface.java");
        }
    }

    // The following functions allow the programmer to interface with the pinpoints
    // individual parts without making them deal with low level serial protocols hidden
    // in the api. Each throws a NoConnectionException to allow the UI to know when a
    // the connection has been lost.
    /**
     * Returns the sample rate requested from the pinpoint.
     * This requires a connection to the pinpoint so the exception
     * is thrown to let the UI know if a problem occurs.
     *
     * @return int
     * @throws NoConnectionException
     */
    public int getSampleRate() {
        return pinpoint.getSampleRate();
    }

    /**
     * Returns the acceleration rate requested from the pinpoint.
     * This requires a connection to the pinpoint so the exception
     * is thrown to let the UI know if a problem occurs.
     *
     * @return int
     * @throws NoConnectionException
     */
    public int getAccelRate() {
        return pinpoint.getAccelRate();
    }

    /**
     * Returns the temperature rate requested from the pinpoint.
     * This requires a connection to the pinpoint so the exception
     * is thrown to let the UI know if a problem occurs.
     *
     * @return int
     * @throws NoConnectionException
     */
    public int getTempRate() {
        return pinpoint.getTempRate();
    }

    /**
     * Returns the light rate requested from the pinpoint.
     * This requires a connection to the pinpoint so the exception
     * is thrown to let the UI know if a problem occurs.
     *
     * @return int
     * @throws NoConnectionException
     */
    public int getLightRate() {
        return pinpoint.getLightRate();
    }

    /**
     * Returns the external rate requested from the pinpoint.
     * This requires a connection to the pinpoint so the exception
     * is thrown to let the UI know if a problem occurs.
     *
     * @return int
     * @throws NoConnectionException
     */
    public int getExternalRate() {
        return pinpoint.getExternalRate();
    }

    /**
     * Returns the sound rate requested from the pinpoint.
     * This requires a connection to the pinpoint so the exception
     * is thrown to let the UI know if a problem occurs.
     *
     * @return int
     * @throws NoConnectionException
     */
    public int getSoundRate() {
        return pinpoint.getSoundRate();
    }

    /**
     * Returns the external type requested from the pinpoint.
     * This requires a connection to the pinpoint so the exception
     * is thrown to let the UI know if a problem occurs.
     *
     * @return int
     * @throws NoConnectionException
     */
    /* public int getExternalType() throws NoConnectionException {
    return pinpoint.getExternalType();
    }*/
    private String getExternalType() {
        String externalSensorCSV;
        int ExternalType = 255;

        ExternalType = pinpoint.getExternalType();

        Vector<String[]> conversion = new Vector<String[]>();
        String line;
        BufferedReader in = new BufferedReader(new StringReader(PinPointConverter.conversionsText));
        try {
			while ((line = in.readLine()) != null) {
			    conversion.add(line.split(","));
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        Iterator<String[]> convIterator = conversion.iterator();

        while (convIterator.hasNext()) {
            String temp[] = (String[]) convIterator.next();
            if (ExternalType == Integer.parseInt(temp[0])) {
                //externalSensorUser = temp[1];
                externalSensorCSV = temp[2];
                return externalSensorCSV;
            }
        }
		
		return null;
    }

    public int getExternalTypeAsInt() {
        return pinpoint.getExternalType();
    }

    /**
     * Returns the GPS settings requested from the pinpoint.
     * This requires a connection to the pinpoint so the exception
     * is thrown to let the UI know if a problem occurs.
     *
     * @return int
     * @throws NoConnectionException
     */
    public int getGPSCount() {
        return pinpoint.getGPSCount();
    }

    /**
     * Return the headers associated with the current setup of the pinpoint.
     * 
     * @return
     */
    public String[] getHeaders() {
        String[] headers = new String[]{"Time", "Latitude", "Longitude", "Acceleration", "Light", "Temperature", "Sound", getExternalType(), "Altitude"};
     
        return headers;
    }

    /**
     * Sets the sample rate and writes it to the pinpoint. This requires
     * a connection to the pinpoint so the NoConnectionException is
     * thrown when there isnt one so that the UI knows when an error occured.
     *
     * @param millis
     * @throws NoConnectionException
     */
    public void setSampleRate(int millis) {
        pinpoint.setSampleRate(millis);
    }

    /**
     * Sets the acceleration rate and writes it to the pinpoint. This requires
     * a connection to the pinpoint so the NoConnectionException is
     * thrown when there isnt one so that the UI knows when an error occured.
     *
     * @param millis
     * @throws NoConnectionException
     */
    public void setAccelRate(int millis) {
        pinpoint.setAccelRate(millis);
    }

    /**
     * Sets the temperature rate and writes it to the pinpoint. This requires
     * a connection to the pinpoint so the NoConnectionException is
     * thrown when there isnt one so that the UI knows when an error occured.
     *
     * @param millis
     * @throws NoConnectionException
     */
    public void setTempRate(int millis) {
        pinpoint.setTempRate(millis);
    }

    /**
     * Sets the light rate and writes it to the pinpoint. This requires
     * a connection to the pinpoint so the NoConnectionException is
     * thrown when there isnt one so that the UI knows when an error occured.
     *
     * @param millis
     * @throws NoConnectionException
     */
    public void setLightRate(int millis) {
        pinpoint.setLightRate(millis);
    }

    /**
     * Sets the external rate and writes it to the pinpoint. This requires
     * a connection to the pinpoint so the NoConnectionException is
     * thrown when there isnt one so that the UI knows when an error occured.
     *
     * @param millis
     * @throws NoConnectionException
     */
    public void setExternalRate(int millis) {
        pinpoint.setExternRate(millis);
    }

    /**
     * Sets the sound rate and writes it to the pinpoint. This requires
     * a connection to the pinpoint so the NoConnectionException is
     * thrown when there isnt one so that the UI knows when an error occured.
     *
     * @param millis
     * @throws NoConnectionException
     */
    public void setSoundRate(int millis) {
        pinpoint.setSoundRate(millis);
    }

    /**
     * Sets the external type and writes it to the pinpoint. This requires
     * a connection to the pinpoint so the NoConnectionException is
     * thrown when there isnt one so that the UI knows when an error occured.
     *
     * @param millis
     * @throws NoConnectionException
     */
    public void setExternalType(int x) {
        pinpoint.setExternalType(x);
    }

    /**
     * Sets the GPS settings and writes it to the pinpoint. This requires
     * a connection to the pinpoint so the NoConnectionException is
     * thrown when there isnt one so that the UI knows when an error occured.
     *
     * @param millis
     * @throws NoConnectionException
     */
    public void setGPSCount(int x) {
        pinpoint.setGPSCount(x);
    }

    /**
     * Gets the firmware version from the pinpoint. This requires
     * a connection to the pinpoint so the NoConnectionException is
     * thrown when there isnt one so that the UI knows when an error occured.
     *
     * @param millis
     * @throws NoConnectionException
     */
    public String getFirmwareVersion() {
        return pinpoint.getFirmwareVersion() + "";
    }

    /**
     * Gets the Serial number of the pinpoint. This requires
     * a connection to the pinpoint so the NoConnectionException is
     * thrown when there isnt one so that the UI knows when an error occured.
     *
     * @param millis
     * @throws NoConnectionException
     */
    public int getSerialNumber() {
    	return pinpoint.getSerialNumber();
    }

    /**
     * Sets the serial number and writes it to the pinpoint. This requires
     * a connection to the pinpoint so the NoConnectionException is
     * thrown when there isnt one so that the UI knows when an error occured.
     *
     * @param millis
     * @throws NoConnectionException
     */
    public void setSerialNumber(int x) {
        pinpoint.setSerialNumber(x);
    }
}
