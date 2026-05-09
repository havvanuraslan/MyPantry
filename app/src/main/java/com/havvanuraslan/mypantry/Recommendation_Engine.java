package com.havvanuraslan.mypantry;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Recommendation_Engine {

    public interface OnRecommendationsReady {
        void onSuccess(List<RecipeScore> recommendedRecipes);
        void onError(String message);
    }

    public static class RecipeScore implements Comparable<RecipeScore> {
        public Recipe_Entity recipe;
        public double matchScore;

        public RecipeScore(Recipe_Entity recipe, double matchScore) {
            this.recipe = recipe;
            this.matchScore = matchScore;
        }

        @Override
        public int compareTo(RecipeScore other) {
            return Double.compare(this.matchScore, other.matchScore);
        }
    }


    public void findBestRecipes(Context context, List<String> userPantry, int topN, OnRecommendationsReady callback) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler mainHandler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            try {
                Food2VecEngine aiEngine = Food2VecEngine.getInstance(context);
                Recipe_Dao recipeDao = Recipe_Database.getDbInstance(context).recipeDao();
                PantryStrategist strategist = new PantryStrategist();

                double[] pantryVector = calculatePantryVector(aiEngine, userPantry);
                if (pantryVector == null) {
                    mainHandler.post(() -> callback.onError("Pantry ingredients were not recognized by the AI!"));
                    return;
                }

                Map<Integer, Recipe_Entity> candidatePool = new HashMap<>();
                String bestIngredient = strategist.getBestFilterIngredient(userPantry);
                if (bestIngredient == null && !userPantry.isEmpty()) bestIngredient = userPantry.get(0);

                if (bestIngredient != null) {
                    List<Recipe_Entity> matches = recipeDao.getCandidateRecipes(bestIngredient.toLowerCase().trim());
                    for (Recipe_Entity recipe : matches) candidatePool.put(recipe.id, recipe);
                }

                if (candidatePool.isEmpty()) {
                    mainHandler.post(() -> callback.onSuccess(new ArrayList<>()));
                    return;
                }

                PriorityQueue<RecipeScore> topRecipesQueue = new PriorityQueue<>(topN);
                for (Recipe_Entity recipe : candidatePool.values()) {
                    syncFavoriteState(recipe);

                    if (recipe.recipe_vector != null && !recipe.recipe_vector.isEmpty()) {
                        double similarity = aiEngine.calculateCosineSimilarity(pantryVector, parseVectorString(recipe.recipe_vector));
                        if (similarity > 0.40) {
                            topRecipesQueue.offer(new RecipeScore(recipe, similarity * 100));
                            if (topRecipesQueue.size() > topN) topRecipesQueue.poll();
                        }
                    }
                }

                List<RecipeScore> finalResults = new ArrayList<>(topRecipesQueue);
                Collections.sort(finalResults, Collections.reverseOrder());
                mainHandler.post(() -> callback.onSuccess(finalResults));

            } catch (Exception e) {
                mainHandler.post(() -> callback.onError("AI Calculation Error: " + e.getMessage()));
            } finally {
                executor.shutdown();
            }
        });
    }


    public void calculateScoresForSpecificList(Context context, List<Recipe_Entity> recipes, List<String> userPantry, OnRecommendationsReady callback) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler mainHandler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            try {
                Food2VecEngine aiEngine = Food2VecEngine.getInstance(context);
                double[] pantryVector = calculatePantryVector(aiEngine, userPantry);

                List<RecipeScore> results = new ArrayList<>();

                for (Recipe_Entity recipe : recipes) {
                    syncFavoriteState(recipe);

                    if (pantryVector != null && recipe.recipe_vector != null && !recipe.recipe_vector.isEmpty()) {
                        double similarity = aiEngine.calculateCosineSimilarity(pantryVector, parseVectorString(recipe.recipe_vector));
                        results.add(new RecipeScore(recipe, similarity * 100));
                    } else {
                        results.add(new RecipeScore(recipe, 0.0));
                    }
                }

                Collections.sort(results, Collections.reverseOrder());
                mainHandler.post(() -> callback.onSuccess(results));

            } catch (Exception e) {
                mainHandler.post(() -> callback.onError("Scoring Error: " + e.getMessage()));
            } finally {
                executor.shutdown();
            }
        });
    }


    private double[] calculatePantryVector(Food2VecEngine aiEngine, List<String> userPantry) {
        double[] pantryVector = new double[50];
        int validWordCount = 0;
        for (String ingredient : userPantry) {
            double[] vec = aiEngine.getVector(ingredient.toLowerCase().trim());
            if (vec != null) {
                for (int i = 0; i < 50; i++) pantryVector[i] += vec[i];
                validWordCount++;
            }
        }
        if (validWordCount == 0) return null;
        for (int i = 0; i < 50; i++) pantryVector[i] /= validWordCount;
        return pantryVector;
    }

    private void syncFavoriteState(Recipe_Entity recipe) {
        recipe.isFavorite = (recipe.favorite_recipe != null && recipe.favorite_recipe == 1);
    }

    private double[] parseVectorString(String vectorString) {
        if (vectorString == null) return new double[50];
        String[] parts = vectorString.split(",");
        double[] vector = new double[50];
        for (int i = 0; i < parts.length && i < 50; i++) {
            try {
                vector[i] = Double.parseDouble(parts[i].trim());
            } catch (NumberFormatException e) {
                vector[i] = 0.0;
            }
        }
        return vector;
    }
}