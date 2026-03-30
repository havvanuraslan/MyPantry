package com.havvanuraslan.mypantry;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

// DÜZELTME: 'public class' yerine 'public interface' olmalı
public interface TheMealDBApi {

    // Malzemeye göre filtreleme (Mevcut)
    @GET("filter.php")
    Call<MealListResponse> filterByIngredient(@Query("i") String ingredient);

    // ID'ye göre detay (Mevcut)
    @GET("lookup.php")
    Call<MealDetailResponse> lookupMealById(@Query("i") String mealId);

    // YENİ EKLENEN: Arama Çubuğu için isme göre arama
    @GET("search.php")
    Call<MealListResponse> searchMeals(@Query("s") String query);
}