package com.havvanuraslan.mypantry;

import java.util.HashMap;
import java.util.Map;

public class SubstitutionEngine {
    private Map<String, String> substitutions;

    public SubstitutionEngine() {
        this.substitutions = new HashMap<>();

        // Dairy & Fat
        substitutions.put("butter", "margarine");
        substitutions.put("margarine", "butter");
        substitutions.put("milk", "soy milk");
        substitutions.put("soy milk", "almond milk");
        substitutions.put("almond milk", "oat milk");
        substitutions.put("parmesan cheese", "pecorino romano");
        substitutions.put("yogurt", "sour cream");
        substitutions.put("sour cream", "yogurt");
        substitutions.put("cream", "milk + butter");
        substitutions.put("heavy cream", "milk + butter");
        substitutions.put("buttermilk", "milk + lemon juice");
        substitutions.put("ricotta", "cottage cheese");
        substitutions.put("mozzarella", "mild cheddar");
        substitutions.put("cheddar", "gouda");
        substitutions.put("feta", "white cheese");
        substitutions.put("ghee", "butter");
        substitutions.put("evaporated milk", "milk + butter");
        substitutions.put("condensed milk", "milk + sugar");

        // Acids / Sweeteners
        substitutions.put("lemon juice", "vinegar");
        substitutions.put("lime juice", "lemon juice");
        substitutions.put("vinegar", "lemon juice");
        substitutions.put("sugar", "honey");
        substitutions.put("honey", "maple syrup");
        substitutions.put("soy sauce", "tamari");
        substitutions.put("balsamic vinegar", "grape vinegar + molasses");
        substitutions.put("apple cider vinegar", "white vinegar");
        substitutions.put("brown sugar", "white sugar + molasses");
        substitutions.put("molasses", "grape molasses");
        substitutions.put("agave syrup", "honey");
        substitutions.put("rice vinegar", "white vinegar + sugar");

        // Cooking Ingredients
        substitutions.put("all-purpose flour", "cornstarch");
        substitutions.put("cornstarch", "all-purpose flour");
        substitutions.put("baking soda", "baking powder");
        substitutions.put("baking powder", "baking soda");
        substitutions.put("white wine", "chicken broth");
        substitutions.put("red wine", "beef broth");
        substitutions.put("arrowroot powder", "cornstarch");
        substitutions.put("gelatin", "agar agar");
        substitutions.put("agar agar", "gelatin");
        substitutions.put("yeast", "fresh yeast");
        substitutions.put("self-rising flour", "all-purpose flour + baking powder + salt");
        substitutions.put("panko breadcrumbs", "breadcrumbs");
        substitutions.put("bread crumbs", "crushed crackers");
        substitutions.put("shortening", "butter");
        substitutions.put("vegetable broth", "water + vegetable seasoning");

        // Vegetables / Herbs / Spices
        substitutions.put("onion", "onion powder");
        substitutions.put("garlic", "garlic powder");
        substitutions.put("shallot", "onion");
        substitutions.put("celery", "carrot + green pepper");
        substitutions.put("celery leaves", "parsley");
        substitutions.put("leek", "onion");
        substitutions.put("green onion", "chives");
        substitutions.put("chili pepper", "red pepper flakes");
        substitutions.put("bell pepper", "red sweet pepper");
        substitutions.put("spinach", "chard");

        substitutions.put("paprika", "mild chili flakes");
        substitutions.put("smoked paprika", "paprika + barbecue sauce");
        substitutions.put("cumin", "ground coriander");
        substitutions.put("turmeric", "curry powder");
        substitutions.put("curry powder", "turmeric + cumin + coriander");
        substitutions.put("bay leaf", "thyme");
        substitutions.put("thyme", "oregano");
        substitutions.put("oregano", "thyme");
        substitutions.put("rosemary", "thyme");

        // Proteins
        substitutions.put("chicken breast", "turkey breast");
        substitutions.put("ground beef", "ground chicken");
        substitutions.put("sausage", "ground meat seasoned like sausage");
        substitutions.put("bacon", "pastrami");
        substitutions.put("ham", "turkey ham");

        // Grains & Others
        substitutions.put("breadcrumbs", "dried bread crumbs");
        substitutions.put("quinoa", "bulgur");
        substitutions.put("couscous", "fine bulgur");
        substitutions.put("rice", "bulgur");
        substitutions.put("brown rice", "whole wheat bulgur");

        // Oils
        substitutions.put("olive oil", "sunflower oil");
        substitutions.put("coconut oil", "butter");
        substitutions.put("sesame oil", "sesame + neutral oil mixture");

        // Sauces
        substitutions.put("fish sauce", "soy sauce + lemon");
        substitutions.put("worcestershire sauce", "soy sauce + vinegar + sugar");
        substitutions.put("ketchup", "tomato paste + sugar + vinegar");
        substitutions.put("mayonnaise", "yogurt + a little oil");
        substitutions.put("mustard", "mustard powder + water + vinegar");
        substitutions.put("tomato paste", "reduced tomato puree");

        // Legumes & Nut Products
        substitutions.put("peanut butter", "tahini or almond butter");
        substitutions.put("chickpeas", "white beans");
        substitutions.put("lentils", "red lentils or green lentils");
    }

    public String getSubstitute(String missingIngredient) {
        if (missingIngredient == null || missingIngredient.isEmpty()) {
            return null;
        }
        return substitutions.get(missingIngredient.toLowerCase().trim());
    }

    public static void main(String[] args) {
        SubstitutionEngine substitutionEngine = new SubstitutionEngine();
        System.out.println(substitutionEngine.getSubstitute("butter"));
    }


}
