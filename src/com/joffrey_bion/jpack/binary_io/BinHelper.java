package com.joffrey_bion.jpack.binary_io;

public class BinHelper {

    public static String addLeadingZeros(String str, int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length - str.length(); i++) {
            sb.append("0");
        }
        sb.append(str);
        return sb.toString();
    }
    
}
