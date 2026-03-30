package com.havvanuraslan.mypantry;

import androidx.room.Dao;
import androidx.room.Query;
import java.util.List;

@Dao
public interface Recipe_Dao {

    // 1. RAM'i koruyan sayfalama metodu
    @Query("SELECT * FROM recipes LIMIT :limit OFFSET :offset")
    List<Recipe_Entity> getRecipesInBatches(int limit, int offset);

    // 2. Toplam sayıyı getiren metot
    @Query("SELECT COUNT(id) FROM recipes")
    int getTotalRecipeCount();

    @Query("SELECT * FROM recipes WHERE id = :recipeId LIMIT 1")
    Recipe_Entity getRecipeById(int recipeId);

    // DİKKAT: Hata veren kısım burasıydı! Üzerine @Query ekledik.
    // (Gerçi yeni yazdığımız RecommendationEngine sınıfında artık
    // bunu kullanmıyoruz ama kodda duracaksa mutlaka etiketi olmalı).
    @Query("SELECT * FROM recipes")
    List<Recipe_Entity> getAllRecipes();
    // Malzeme ismine göre SQL ile hızlıca ön eleme (Pre-filtering) yapar
    @Query("SELECT * FROM recipes WHERE ingredients LIKE '%' || :ingredient || '%' LIMIT 5000")
    List<Recipe_Entity> getCandidateRecipes(String ingredient);
}