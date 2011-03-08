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
package edu.uml.cs.isense.pincushion;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import android.bluetooth.BluetoothAdapter;
import android.util.Log;

/**
 * The PinComm class is used to communicate with a PINPoint via a serial
 * port interface.
 * 
 * @author James Dalphond <jdalphon@cs.uml.edu>
 */
public class PinComm {

    //Serial Constants
    public static final int BAUD_RATE = 38400;
    //public static final int BAUD_RATE = 500000;
    public static final boolean FLOW_CONTROL = false;
    //Pinpoint Commands
    private static final byte HELLO_BYTE = (byte) (0x01);
    private static final byte HELLO_BACK_BYTE = (byte) (0x02);
    private static final byte REQUEST_SAVED_BYTE = (byte) (0x02);
    private static final byte REQUEST_LIVE_BYTE = (byte) (0x06);
    private static final byte READ_EEPROM_BYTE = (byte) (0x04);
    private static final byte WRITE_EEPROM_BYTE = (byte) (0x05);
    //Pinpoint EEPROM Locations
    private static final byte EEPROM_SAMPLE_HIGH = (byte) (0x00);
    private static final byte EEPROM_SAMPLE_LOW = (byte) (0x01);
    private static final byte EEPROM_ACCEL_HIGH = (byte) (0x02);
    private static final byte EEPROM_ACCEL_LOW = (byte) (0x03);
    private static final byte EEPROM_LIGHT_HIGH = (byte) (0x04);
    private static final byte EEPROM_LIGHT_LOW = (byte) (0x05);
    private static final byte EEPROM_TEMP_HIGH = (byte) (0x06);
    private static final byte EEPROM_TEMP_LOW = (byte) (0x07);
    private static final byte EEPROM_SOUND_HIGH = (byte) (0x08);
    private static final byte EEPROM_SOUND_LOW = (byte) (0x09);
    private static final byte EEPROM_EXTERN_HIGH = (byte) (0x0A);
    private static final byte EEPROM_EXTERN_LOW = (byte) (0x0B);
    private static final byte EEPROM_EXTERN_TYPE = (byte) (0x0C);
    private static final byte EEPROM_GPS_VALIDITY = (byte) (0x0D);
    private static final byte EEPROM_SERIAL_HIGH = (byte) (0x1FE);
    private static final byte EEPROM_SERIAL_LOW = (byte) (0x1FF);
    //Pinpoint record information
    private static final int PAGE_SIZE = 22;
    private static final int RECORD_SIZE = 24;
    private Double firmwareVersion = 0.0;
    private boolean printOn = true;
    private BluetoothService spi;
    
    private PinComm(BluetoothService spi) {
        this.spi = spi;
    }

    /**
     * The instantiate method will use the Serial channel to open up available 
     * ports and check them to see if there is a pinpoint device listening on the other 
     * side.
     */
    public static PinComm instantiate(BluetoothService bts) {
        PinComm pinPoint = new PinComm(bts);
        return pinPoint;
    }

    /**
     * Calls requestPage which does the communication with the pinpoint.
     *
     * This takes care of all conversions to human readable form by calling
     * the pinpoint converter class.
     *
     * @param page
     * @return
     * @throws IOException
     * @throws MissingLogFileException
     */
    public String[] getPages(int page) throws IOException {
        try {

            //Create an instance of the converter so it can be used.
            PinPointConverter conv = new PinPointConverter(getExternalType(), getSampleRate(),false);

            //Create a temporary array the size of a page for the data to be stored in.
            String[] tempArray = new String[PAGE_SIZE];

            //Request a single page from the pinpoint.
            byte[][] records = requestPage((byte) ((page >> 8) & 0xFF), (byte) (page & 0xFF));

            //If there is data convert it, else report end of file.
            for (int i = 0; i < PAGE_SIZE; i++) {
                if (lastRecord(records[i]) == false) {
                    tempArray[i] = conv.convertAll(records[i]).toString().replace("[", "").replace("]", "");
                } else {
                    tempArray[i] = "EOF";
                }
            }

            //Data is converted or end of file is found. Return the array.
            return tempArray;

        } catch (IOException ex) {
            Logger.getLogger(PinComm.class.getName()).log(Level.SEVERE, null, ex);
        }

        //Failed return null.
        return null;
    }

    /**
     * Communicates with the pinpoint to get back a raw copy of the requested
     * page of flash memory of the pinpoint.
     *
     * @param highByte
     * @param lowByte
     * @return
     * @throws IOException
     */
    public byte[][] requestPage(byte highByte, byte lowByte) throws IOException {

        // If the serial line is open.
        if (spi.isOpen()) {

            //Raw storage for the response.
            byte[][] records = new byte[PAGE_SIZE][RECORD_SIZE];

            //Clear the line
            spi.clear();

            //Tell the pinpoint we will be requesting a page.
            spi.writeByte(REQUEST_SAVED_BYTE);

            //Tell the pinpoint which page we are  requesting.
            spi.writeByte((byte) highByte);
            spi.writeByte((byte) lowByte);

            //Read each byte back from the serial line and place it into the raw storage.
            try {
                for (int i = 0; i < PAGE_SIZE; i++) {
                    for (int j = 0; j < RECORD_SIZE; j++) {
                        records[i][j] = spi.readByte();
                    }
                }
            } catch (ArrayIndexOutOfBoundsException e) {
                //System.out.println("Found it");
            } catch (Exception e) {
                e.printStackTrace();
            }

            //Return the raw values.
            return records;
        }

        //Faied return null.
        return null;
    }

    public byte[] requestDataStream() throws IOException {
        byte[] x  = null;
        // If the serial line is open.
        if (spi.isOpen()) {
            spi.clear();
            //System.out.println("\nSending request for live data...");
            spi.writeByte(REQUEST_LIVE_BYTE);
            // System.out.println("Request Sent waiting for response...");
            try {
                x = getData();
            } catch (IOException e) {
                Log.d("PinComm", "Timed out trying again...");
                x = getData();
            }
        }
        return x;
    }

    private byte[] getData() throws IOException {
        int i ;
        byte[] ret = new byte[24];
        for (i = 0; i < 24; i++) {
            Byte b = spi.readByte();
            ret[i] = b;
        }
        return ret;
    }

    /**
     * Allows us to keep track of whether or not we have reached the end of the data.
     *
     * @param record
     * @return
     */
    public boolean lastRecord(byte[] record) {
        for (int i = 0; i < record.length; i++) {
            if ((record[i] & 255) != 255) {
                return false;
            }
        }
        return true;
    }


    /**
     * Returns the description of the connected device.
     *
     * @return String
     */
    public String getDescription() {
        return "pinpoint";
    }

    /**
     * Cleanly close the connection.
     *
     * @throws IOException
     */
    public void close() throws IOException {
        spi.close();
    }

    /**
     * Returns the firmware version of the current PINPoint
     *
     * @return Double
     */
    public Double getFirmwareVersion() {
        return firmwareVersion;
    }

    /**
     * Returns the value stored in the pinpoint for the requested setting
     * 
     * @param hByte (High Byte)
     * @param lByte (Low Byte)
     * @return int
     * @throws NoConnectionException
     */
    private int getSetting(byte hByte, byte lByte)  {
        //System.out.println("Requesting Sample Rate");
        short high, low;
        if (spi.isOpen()) {
            spi.clear();
			spi.writeByte(READ_EEPROM_BYTE);
			spi.writeByte((byte) 0x00);
			spi.writeByte(hByte);
			high = (short) (spi.readByte() & 255);
			spi.clear();
			spi.writeByte(READ_EEPROM_BYTE);
			spi.writeByte((byte) 0x00);
			spi.writeByte(lByte);
			low = (short) (spi.readByte() & 255);
			int result = ((high << 8) + low);
			return result;
        } else {
        	return -1;
        }
    }

    /**
     * Returns the value stored in the pinpoint for the requested setting
     *
     * @param sByte (Setting Byte)
     * @return
     * @throws NoConnectionException
     */
    private int getSetting(byte sByte) {
        short high;
        if (spi.isOpen()) {
            spi.clear();
			spi.writeByte(READ_EEPROM_BYTE);
			spi.writeByte((byte) 0x00);
			spi.writeByte(sByte);
			spi.clear();
			high = (short) (spi.readByte() & 255);
			return (high & 255);
        } else {
        	return -1;
        }
    }

    /**
     * Sets the value stored in the pinpoint for the requested setting
     *
     * @param hByte (High Byte)
     * @param lByte (Low Byte)
     * @param value
     * @throws NoConnectionException
     */
    private void setSetting(byte hByte, byte lByte, int value) {
        if (spi.isOpen()) {
            spi.clear();
			spi.writeByte(WRITE_EEPROM_BYTE);
			spi.writeByte((byte) 0x00);
			spi.writeByte(hByte);
			spi.writeByte((byte) ((value >> 8) & 0xFF));
			spi.clear();
			spi.writeByte(WRITE_EEPROM_BYTE);
			spi.writeByte((byte) 0x00);
			spi.writeByte(lByte);
			spi.writeByte((byte) (value & 0xFF));
			spi.readByte();
        }
    }

    /**
     * Sets the value stored in the pinpoint for the requested setting
     *
     * @param sByte (Setting Byte)
     * @param value
     * @throws NoConnectionException
     */
    private void setSetting(byte sByte, int value) {
        if (spi.isOpen()) {
            spi.clear();
			spi.writeByte(WRITE_EEPROM_BYTE);
			spi.writeByte((byte) 0x00);
			spi.writeByte(sByte);
			spi.writeByte((byte) (value & 0xFF));
			spi.clear();
			spi.readByte();
        }
    }

    /**
     * Returns the sample rate of the connected PINPoint.
     * If no connection is found, throws NoConnectionException
     *
     * @return int
     * @throws NoConnectionException
     */
    public int getSampleRate() {
        return getSetting(EEPROM_SAMPLE_HIGH, EEPROM_SAMPLE_LOW);
    }

    /**
     * Returns the sub sample rate of the accelerometer of the connected PINPoint.
     * If no connection is found, throws NoConnectionException
     *
     * @return int
     * @throws NoConnectionException
     */
    public int getAccelRate() {
        return getSetting(EEPROM_ACCEL_HIGH, EEPROM_ACCEL_LOW);
    }

    /**
     * Returns the sub sample rate of the light sensor of the connected PINPoint.
     * If no connection is found, throws NoConnectionException
     *
     * @return int
     * @throws NoConnectionException
     */
    public int getLightRate() {
        return getSetting(EEPROM_LIGHT_HIGH, EEPROM_LIGHT_LOW);
    }

    /**
     * Returns the sub sample rate of the temperature sensor of the connected PINPoint.
     * If no connection is found, throws NoConnectionException
     *
     * @return int
     * @throws NoConnectionException
     */
    public int getTempRate() {
        return getSetting(EEPROM_TEMP_HIGH, EEPROM_TEMP_LOW);
    }

    /**
     * Returns the sub sample rate of the sound sensor of the connected PINPoint.
     * If no connection is found, throws NoConnectionException
     *
     * @return int
     * @throws NoConnectionException
     */
    public int getSoundRate() {
        return getSetting(EEPROM_SOUND_HIGH, EEPROM_SOUND_LOW);
    }

    /**
     * Returns the sub sample rate of the external sensor of the connected PINPoint.
     * If no connection is found, throws NoConnectionException
     *
     * @return int
     * @throws NoConnectionException
     */
    public int getExternalRate() {
        return getSetting(EEPROM_EXTERN_HIGH, EEPROM_EXTERN_LOW);
    }

    /**
     * Returns the serial number of the connected PINPoint.
     * If no connection is found, throws NoConnectionException
     *
     * @return int
     * @throws NoConnectionException
     */
    public int getSerialNumber() {
        return getSetting(EEPROM_SERIAL_HIGH, EEPROM_SERIAL_LOW);
    }

    /**
     * Returns the index value of the external sensor attached to the connected PINPoint.
     * If no connection is found, throws NoConnectionException
     *
     * @return int
     * @throws NoConnectionException
     */
    public int getExternalType() {
        return getSetting(EEPROM_EXTERN_TYPE);
    }

    /**
     * Returns number of satellites the connected PINPoint is searching for.
     * If no connection is found, throws NoConnectionException
     *
     * @return int
     * @throws NoConnectionException
     */
    public int getGPSCount() {
        return getSetting(EEPROM_GPS_VALIDITY);
    }

    /**
     * Sets the sample rate of the connected PINPoint. If No
     * connection is found, throws NoConnectionException.
     * 
     * @param millis (Milliseconds)
     * @throws NoConnectionException
     */
    public void setSampleRate(int millis) {
        setSetting(EEPROM_SAMPLE_HIGH, EEPROM_SAMPLE_LOW, millis);
    }

    /**
     * Sets the sub sample rate for the accelerometer sensor of the
     * connected PINPoint. If no connection is found, throws
     * NoConnectionException
     *
     * @param samples (sub samples per sample)
     * @throws NoConnectionException
     */
    public void setAccelRate(int samples) {
        setSetting(EEPROM_ACCEL_HIGH, EEPROM_ACCEL_LOW, samples);
    }

    /**
     * Sets the sub sample rate for the light sensor of the
     * connected PINPoint. If no connection is found, throws
     * NoConnectionException
     *
     * @param samples (sub samples per sample)
     * @throws NoConnectionException
     */
    public void setLightRate(int samples) {
        setSetting(EEPROM_LIGHT_HIGH, EEPROM_LIGHT_LOW, samples);
    }

    /**
     * Sets the sub sample rate for the temperature sensor of the 
     * connected PINPoint. If no connection is found, throws 
     * NoConnectionException
     * 
     * @param samples (sub samples per sample)
     * @throws NoConnectionException
     */
    public void setTempRate(int samples) {
        setSetting(EEPROM_TEMP_HIGH, EEPROM_TEMP_LOW, samples);
    }

    /**
     * Sets the sub sample rate for the sound sensor of the
     * connected PINPoint. If no connection is found, throws
     * NoConnectionException
     *
     * @param samples (sub samples per sample)
     * @throws NoConnectionException
     */
    public void setSoundRate(int samples) {
        setSetting(EEPROM_SOUND_HIGH, EEPROM_SOUND_LOW, samples);
    }

    /**
     * Sets the sub sample rate for the external sensor attached to the
     * connected PINPoint. If no connection is found, throws
     * NoConnectionException
     *
     * @param samples (sub samples per sample)
     * @throws NoConnectionException
     */
    public void setExternRate(int samples) {
        setSetting(EEPROM_EXTERN_HIGH, EEPROM_EXTERN_LOW, samples);
    }

    /**
     * Sets the serial number of the
     * connected PINPoint. If no connection is found, throws
     * NoConnectionException
     *
     * @param samples (sub samples per sample)
     * @throws NoConnectionException
     */
    public void setSerialNumber(int x) {
        setSetting(EEPROM_SERIAL_HIGH, EEPROM_SERIAL_LOW, x);
    }

    /**
     * Sets the index value for the type of external sensor attached to the 
     * connected PINPoint. If no connection is found, throws 
     * NoConnectionException
     * 
     * @param samples (sub samples per sample)
     * @throws NoConnectionException
     */
    public void setExternalType(int x) {
        setSetting(EEPROM_EXTERN_TYPE, x);
    }

    /**
     * Sets number of satellites the connected PINPoint is looking for.
     * If no connection is found, throws NoConnectionException
     *
     * @param samples (sub samples per sample)
     * @throws NoConnectionException
     */
    public void setGPSCount(int x) {
        setSetting(EEPROM_GPS_VALIDITY, x);
    }

    public String getComType() {
        return "pptv3";
    }

    private void myPrint(String x){
        if(printOn){
            Log.d("PinComm", x);
        }
    }
}
