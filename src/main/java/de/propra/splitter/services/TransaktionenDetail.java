package de.propra.splitter.services;

import java.util.ArrayList;

public record TransaktionenDetail (double geld, String sender, ArrayList<String> empfaenger, String grund) {

    public String alleEmpfaenger(){
        String s="";
        for (String p: empfaenger) {
            s += p + " ";
        }
        return s;
    }
}
