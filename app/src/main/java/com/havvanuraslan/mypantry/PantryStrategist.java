package com.havvanuraslan.mypantry;

import java.util.List;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
public class PantryStrategist {

        private final List<String> priorityList;
        private final Set<String> commonNoiseIngredients;

        public PantryStrategist() {

            this.priorityList = Arrays.asList(
                    "chicken", "beef", "pork", "lamb", "salmon", "fish", "shrimp",
                    "tuna", "turkey", "duck", "sausage", "bacon", "ham",
                    "meatballs", "ground beef", "ground chicken", "ground turkey",

                    "tofu", "tempeh", "seitan", "lentils", "chickpeas", "beans",
                    "edamame", "halloumi", "paneer", "eggs",

                    "rice", "pasta", "noodles", "bread", "tortilla", "potatoes",
                    "sweet potatoes", "quinoa", "bulgur", "couscous",

                    "avocado", "mushrooms", "tomatoes", "zucchini", "eggplant",
                    "bell pepper", "spinach", "broccoli", "cauliflower",

                    "cheese", "parmesan", "cheddar", "feta", "cream cheese",
                    "peas", "corn", "asparagus", "artichoke"
            );

            this.commonNoiseIngredients = new HashSet<>(Arrays.asList(
                    "flour", "all-purpose flour", "sugar", "salt", "pepper",
                    "water", "oil", "olive oil", "butter", "margarine",

                    "garlic", "onion", "ginger",

                    "paprika", "cumin", "oregano", "thyme", "bay leaf",
                    "coriander", "turmeric", "chili powder",

                    "vinegar", "lemon juice", "lime juice", "milk",
                    "soy sauce", "tomato paste", "broth", "stock",

                    "yeast", "baking soda", "baking powder",

                    "rice vinegar", "honey", "ketchup", "mayonnaise",
                    "mustard", "coffee", "tea", "cocoa", "vanilla",

                    "soy milk", "almond milk", "oat milk"
            ));
        }

        public String getBestFilterIngredient(List<String> userPantry) {
            if (userPantry == null || userPantry.isEmpty()) {
                return null;
            }

            for (String priorityIngredient : priorityList) {
                if (userPantry.contains(priorityIngredient)) {
                    return priorityIngredient;
                }
            }

            for (String pantryIngredient : userPantry) {
                if (!commonNoiseIngredients.contains(pantryIngredient)) {
                    return pantryIngredient;
                }
            }

            return null;
        }


}
