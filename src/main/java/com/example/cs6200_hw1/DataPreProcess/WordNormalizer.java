package com.example.cs6200_hw1.DataPreProcess;

/**
 * standardize the words by lowercase them
 */
public class WordNormalizer {
    public char[] lowercase(char[] chars){
        // noarmlize all the words into lowercase words
        for (int i = 0; i < chars.length; i++){
            chars[i] = Character.toLowerCase(chars[i]);
        }
        return chars;
    }

    //TODO: implement a word stemmer
    public String toStem(char[] chars) {
        String string = new String(chars);
        return string;
    }
}
