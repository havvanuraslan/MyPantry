package com.havvanuraslan.mypantry;

import androidx.room.Dao;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;

@Dao
public interface Recipe_Dao {

    @Query("SELECT * FROM recipes LIMIT :limit OFFSET :offset")
    List<Recipe_Entity> getRecipesInBatches(int limit, int offset);

    @Query("SELECT COUNT(id) FROM recipes")
    int getTotalRecipeCount();

    @Query("SELECT * FROM recipes WHERE id = :recipeId LIMIT 1")
    Recipe_Entity getRecipeById(int recipeId);

    @Query("SELECT * FROM recipes")
    List<Recipe_Entity> getAllRecipes();

    @Query("SELECT * FROM recipes WHERE ingredients LIKE '%' || :ingredient || '%' LIMIT 5000")
    List<Recipe_Entity> getCandidateRecipes(String ingredient);

    @Query("SELECT * FROM recipes WHERE favorite_recipe = 1")
    List<Recipe_Entity> getFavoriteRecipes();

    @Update
    void updateRecipe(Recipe_Entity recipe);

    @Query("UPDATE recipes SET favorite_recipe = :isFavorite WHERE id = :recipeId")
    void updateFavoriteStatus(int recipeId, int isFavorite);
}