package com.havvanuraslan.mypantry;
import com.google.gson.annotations.SerializedName;
import java.util.List;
public class MealListResponse {
    @SerializedName("meals")
    private List<Meal> meals;

    public List<Meal> getMeals() { return meals; }
}
