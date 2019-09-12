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
import com.pi4j.component.temperature.TemperatureSensor;
import com.pi4j.io.w1.W1Master;
import com.pi4j.temperature.TemperatureScale;
import org.apache.commons.math3.util.Precision;

/**
 *
 * @author tfaugusto
 */
public class DS18B20 {
    
    public static double getSensorsTemperature(String deviceName){
        W1Master w1Master = new W1Master();
       // System.out.println(w1Master);
        for (TemperatureSensor device : w1Master.getDevices(TemperatureSensor.class)) {
          //  System.out.printf("%-20s %3.1f°C %3.1f°F\n", device.getName(), device.getTemperature(),
                   // device.getTemperature(TemperatureScale.CELSIUS));
            if (device.getName().contains(deviceName)) {
                return device.getTemperature(TemperatureScale.CELSIUS);
            } 
       }
    return 0;
  }
    
  public static double getSensorsTemperatureAdjust(String deviceName, Double valor){
        
      Double sensorTemp, newSensorTemp;
      
      sensorTemp = getSensorsTemperature(deviceName);
      if(!sensorTemp.equals(0.0))
        newSensorTemp = sensorTemp+valor;
      else
          newSensorTemp=0.0;
    return Precision.round(newSensorTemp,1);
  }  
}