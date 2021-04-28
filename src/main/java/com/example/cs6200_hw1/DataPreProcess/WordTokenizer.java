package com.example.cs6200_hw1.DataPreProcess;

import java.util.ArrayList;
import java.util.Arrays;

public class WordTokenizer {
    private ArrayList<String> basicTokens = null;
    // regex to turn all non-alphanum characters into whitespace
    private static final String symbolToSpace = "([[^0-9]&&[^a-z]&&[^A-Z]]+)";
    // regex to remove all non-alphanum symbols
    private static final String removeSymbols = "(\\')|(\\.)|(\t)";
    private int intIndex = 0;

    /**
     * Tokenize the input texts (a char array for the whole doc)
     * @param texts
     */
    public WordTokenizer(char[] texts){
        basicTokens = new ArrayList<>();
        String cleanedText = new String(texts).replaceAll(removeSymbols, "")
                .replaceAll(symbolToSpace, " ").replace("( )+", " ");

        String[] newToken = cleanedText.split(" ");
        // store the token into the arraylist.
        basicTokens.addAll(Arrays.asList(newToken));
    }

    /**
     * read and return then next word of the doc;
     * return null if EOD
     */
    public char[] nextWord(){
        if (intIndex >= basicTokens.size()){
            return null;
        }
        else {
            return basicTokens.get(intIndex++).toCharArray();
        }
    }

}
