package com.example.cs6200_hw1.DataPreProcess;

public class DigitsRemover {

    public boolean isNumeric(String str) {
        return str.matches("-?\\d+(\\.\\d+)?");
//        try {
//            Double.parseDouble(str);
//            return true;
//        } catch(NumberFormatException e){
//            return false;
//        }
    }
}
