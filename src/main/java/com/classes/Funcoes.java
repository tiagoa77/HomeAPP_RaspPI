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

public class Funcoes {
    
    public static final String EMPTY = "";
    public static final char DEFAULT_PAD_CHAR = ' ';
    
    
    public static String padLeftZeros(String inputString, int length, char charappend) {
        if (inputString.length() >= length) {
            return inputString;
        }
        StringBuilder sb = new StringBuilder();
        while (sb.length() < length - inputString.length()) {
            sb.append(charappend);
        }
        sb.append(inputString);

        return sb.toString();
    }

}
