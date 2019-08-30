/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.classes;

/**
 *
 * @author tiago
 */
import com.pi4j.wiringpi.Gpio;
import com.pi4j.wiringpi.GpioUtil;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author tfaugusto
 */
public class DHT11 {
    private static final int    MAXTIMINGS  = 85;
    private final int[]         dht11_dat   = { 0, 0, 0, 0, 0 };
    private Float oldTemp = 0.0f;
    private Float oldHum = 0.0f;
    private List<String> result;

    public DHT11() {

        // setup wiringPi
        if (Gpio.wiringPiSetup() == -1) {
            System.out.println(" ==>> GPIO SETUP FAILED");
            return;
        }

        GpioUtil.export(3, GpioUtil.DIRECTION_OUT);
    }

    public HashMap<String,Float> getTemperature(final int pin) {
        int laststate = Gpio.HIGH;
        int j = 0;
        dht11_dat[0] = dht11_dat[1] = dht11_dat[2] = dht11_dat[3] = dht11_dat[4] = 0;
        
        HashMap<String,Float> output = new HashMap<>();

        Gpio.pinMode(pin, Gpio.OUTPUT);
        Gpio.digitalWrite(pin, Gpio.LOW);
        Gpio.delay(18);

        Gpio.digitalWrite(pin, Gpio.HIGH);
        Gpio.pinMode(pin, Gpio.INPUT);

        for (int i = 0; i < MAXTIMINGS; i++) {
            int counter = 0;
            while (Gpio.digitalRead(pin) == laststate) {
                counter++;
                Gpio.delayMicroseconds(1);
                if (counter == 255) {
                    break;
                }
            }

            laststate = Gpio.digitalRead(pin);

            if (counter == 255) {
                break;
            }

            /* ignore first 3 transitions */
            if (i >= 4 && i % 2 == 0) {
                /* shove each bit into the storage bytes */
                dht11_dat[j / 8] <<= 1;
                if (counter > 16) {
                    dht11_dat[j / 8] |= 1;
                }
                j++;
            }
        }
        // check we read 40 bits (8bit x 5 ) + verify checksum in the last
        // byte
        if (j >= 40 && checkParity()) {
            float h = (float) ((dht11_dat[0] << 8) + dht11_dat[1]) / 10;
            if (h > 100) {
                h = dht11_dat[0]; // for DHT11
            }
            float c = (float) (((dht11_dat[2] & 0x7F) << 8) + dht11_dat[3]) / 10;
            if (c > 125) {
                c = dht11_dat[2]; // for DHT11
            }
            if ((dht11_dat[2] & 0x80) != 0) {
                c = -c;
            }
            final float f = c * 1.8f + 32;
            
            oldTemp=c;
            oldHum=h;
            
            output.put("Temperatura",c);
            output.put("Humidade",h);
            
            System.out.println("Humidity = " + output.get(0) + " Temperature = " + output.get(1) + "(" + f + "f)");
            
            
            
        } else {
            //Em caso de nao encontrar
            output.put("Temperatura",oldTemp);
            output.put("Humidade",oldHum);
            
            System.out.println("Humidity = " + oldHum + " Temperature = " +oldTemp);
        }
        return output;
    }

    private boolean checkParity() {
        return dht11_dat[4] == (dht11_dat[0] + dht11_dat[1] + dht11_dat[2] + dht11_dat[3] & 0xFF);
    }

}

