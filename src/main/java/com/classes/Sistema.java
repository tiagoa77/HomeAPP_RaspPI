/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.classes;

import com.pi4j.system.SystemInfo;
import java.io.IOException;
import java.text.ParseException;

/**
 *
 * @author tiago
 */
public class Sistema {

    public static String getSystemTemp() throws InterruptedException, IOException, ParseException {

        Float systemTemp = 0.0f;

        try {
            systemTemp = SystemInfo.getCpuTemperature();

        } catch (UnsupportedOperationException ex) {
        }
        return Float.toString(systemTemp);
    }

    public static void shutdownRaspi() {
        try {
            Process p = Runtime.getRuntime().exec("sudo shutdown -h now");
            p.waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
