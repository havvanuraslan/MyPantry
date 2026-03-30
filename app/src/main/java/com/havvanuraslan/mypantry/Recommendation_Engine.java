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

                // 1. Kiler Vektörü Hesaplama (Kullanıcının profili)
                double[] pantryVector = new double[50];
                int validWordCount = 0;

                for (String ingredient : userPantry) {
                    double[] vec = aiEngine.getVector(ingredient.toLowerCase().trim());
                    if (vec != null) {
                        for (int i = 0; i < 50; i++) pantryVector[i] += vec[i];
                        validWordCount++;
                    }
                }

                if (validWordCount == 0) {
                    mainHandler.post(() -> callback.onError("Kilerdeki malzemeler yapay zeka tarafından tanınmadı!"));
                    return;
                }

                for (int i = 0; i < 50; i++) pantryVector[i] /= validWordCount;

                // --- 2. HIZLI ÖN ELEME (CANDIDATE GENERATION) ---
                // 200.000 tarifi taramak yerine, sadece bizim malzemelerimizi içerenleri topluyoruz.
                // Aynı tarif birden fazla kez gelmesin diye ID'ye göre Map (Sözlük) kullanıyoruz.
                Map<Integer, Recipe_Entity> candidatePool = new HashMap<>();

                for (String ingredient : userPantry) {
                    String cleanIng = ingredient.toLowerCase().trim();
                    List<Recipe_Entity> matches = recipeDao.getCandidateRecipes(cleanIng);
                    for (Recipe_Entity recipe : matches) {
                        candidatePool.put(recipe.id, recipe); // ID ile ekle (Kopya olmaz)
                    }
                }

                // Eğer kilerdeki şeylerden hiçbir sonuç çıkmadıysa hata dön
                if (candidatePool.isEmpty()) {
                    mainHandler.post(() -> callback.onSuccess(new ArrayList<>()));
                    return;
                }

                // --- 3. YAPAY ZEKA İLE PUANLAMA VE SIRALAMA (RANKING) ---
                // Artık elimizde 200.000 değil, sadece mantıklı olan 5.000 - 10.000 arası tarif var!
                PriorityQueue<RecipeScore> topRecipesQueue = new PriorityQueue<>(topN);

                for (Recipe_Entity recipe : candidatePool.values()) {
                    if (recipe.recipeVector != null && !recipe.recipeVector.isEmpty()) {
                        double[] recipeVector = parseVectorString(recipe.recipeVector);
                        double similarity = aiEngine.calculateCosineSimilarity(pantryVector, recipeVector);

                        // Benzerlik %40'tan büyükse değerlendirmeye al
                        if (similarity > 0.40) {
                            topRecipesQueue.offer(new RecipeScore(recipe, similarity * 100));

                            if (topRecipesQueue.size() > topN) {
                                topRecipesQueue.poll();
                            }
                        }
                    }
                }

                // Sonuçları büyükten küçüğe sırala
                List<RecipeScore> finalResults = new ArrayList<>(topRecipesQueue);
                Collections.reverse(finalResults);

                mainHandler.post(() -> callback.onSuccess(finalResults));

            } catch (Exception e) {
                mainHandler.post(() -> callback.onError("Hesaplama Hatası: " + e.getMessage()));
            }
        });
    }

    private double[] parseVectorString(String vectorString) {
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