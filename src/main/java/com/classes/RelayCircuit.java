/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.classes;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.wiringpi.GpioUtil;

/**
 *
 * @author tiago
 */
public class RelayCircuit {
    
    
    public static void controlRelayCircuit(Pin pinCode ,String pinName, PinState pinState ){
		
                System.out.println("Starting Relay Circuit Example..."); 
		
               
                
		//This is required to enable Non Privileged Access to avoid applying sudo to run Pi4j programs
		GpioUtil.enableNonPrivilegedAccess();
		
                //LOW-->OFF,HIGH-->ON
                
		//Create gpio controller for LED listening on the pin GPIO_00 with default PinState as LOW                    
		final GpioController gpioRelay = GpioFactory.getInstance();
                
                GpioUtil.export(0, GpioUtil.DIRECTION_OUT);
                
                GpioPinDigitalOutput relayLED1 = gpioRelay.provisionDigitalOutputPin(pinCode, pinName, pinState);
	}
	
	/**
	 * Introduce Delay with parameter in milliseconds
	 * @param n Delay parameter in milliseconds
	 */
	public static void introduceDelay(int n){
		try {
			System.out.println("Wait for "+ (n/1000) +" seconds..");
			Thread.sleep(n);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
//	public static void main(String args[]){
//		RelayCircuit stepperMotor = new RelayCircuit();
//		stepperMotor.controlRelayCircuit();
//		
//	}

}
