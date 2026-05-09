package com.havvanuraslan.mypantry;

public class IngredientNormalizer {


    public static String normalize(String word) {
        if (word == null || word.isEmpty()) return "";

        word = word.toLowerCase().trim();

        if (word.equals("tomatoes")) return "tomato";
        if (word.equals("potatoes")) return "potato";
        if (word.equals("leaves")) return "leaf";
        if (word.equals("halves")) return "half";

        if (word.endsWith("ies")) {
            // berries -> berry, cherries -> cherry
            return word.substring(0, word.length() - 3) + "y";
        } else if (word.endsWith("oes")) {
            // mangoes -> mango
            return word.substring(0, word.length() - 2);
        } else if (word.endsWith("s") && !word.endsWith("ss") && !word.endsWith("us") && !word.endsWith("is") && word.length() > 3) {
            // eggs -> egg, apples -> apple, carrots -> carrot
            return word.substring(0, word.length() - 1);
        }

        return word;
    }
}