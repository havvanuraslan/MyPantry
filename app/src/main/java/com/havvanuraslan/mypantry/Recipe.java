package com.havvanuraslan.mypantry;

import java.util.List;

public class Recipe {
    private String id;
    private String name;
    private String imageUrl;
    private List<String> ingredients;

    public Recipe(String id, String name, String imageUrl, List<String> ingredients) {
        this.id = id;
        this.name = name;
        this.imageUrl = imageUrl;
        this.ingredients = ingredients;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public List<String> getIngredients() {
        return ingredients;
    }

    public List<String> getIngredientList(){
        return ingredients;
    }

}
