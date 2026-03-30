package com.havvanuraslan.mypantry;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Singleton Tasarım Deseni ile oluşturulmuş Cihaz İçi (On-Device) Yapay Zeka Motoru.
 * JSON dosyasındaki vektörleri RAM'e yükler ve Kosinüs Benzerliği hesaplamalarını yapar.
 */
public class Food2VecEngine {

    private static Food2VecEngine instance;
    private Map<String, double[]> wordVectors;
    private final int VECTOR_SIZE = 50; // Python'da 100 boyutlu eğitmiştik (vector_size=100)

    // Private Constructor (Singleton Pattern - Dışarıdan new ile oluşturulamaz)
    private Food2VecEngine(Context context) {
        wordVectors = new HashMap<>();
        loadEmbeddings(context);
    }

    // Sistemin her yerinden bu motora tek bir instance üzerinden erişeceğiz
    public static synchronized Food2VecEngine getInstance(Context context) {
        if (instance == null) {
            instance = new Food2VecEngine(context.getApplicationContext());
        }
        return instance;
    }

    // 1. JSON dosyasını okuyup HashMap'e (Sözlüğe) atma işlemi
    private void loadEmbeddings(Context context) {
        try {
            InputStream is = context.getAssets().open("Food2Vec_Embeddings.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();

            String jsonString = new String(buffer, StandardCharsets.UTF_8);
            JSONObject jsonObject = new JSONObject(jsonString);

            // Tüm malzemeleri ve matematiksel vektörlerini haritaya kaydet
            for (java.util.Iterator<String> it = jsonObject.keys(); it.hasNext(); ) {
                String word = it.next();
                JSONArray vectorArray = jsonObject.getJSONArray(word);
                double[] vector = new double[VECTOR_SIZE];
                for (int i = 0; i < VECTOR_SIZE; i++) {
                    vector[i] = vectorArray.getDouble(i);
                }
                wordVectors.put(word, vector);
            }
            Log.d("Food2VecEngine", "Model başarıyla yüklendi! Kelime sayısı: " + wordVectors.size());

        } catch (Exception e) {
            Log.e("Food2VecEngine", "JSON Okuma Hatası: " + e.getMessage());
        }
    }

    // 2. Bir malzemenin vektörünü getiren metod
    public double[] getVector(String ingredient) {
        return wordVectors.get(ingredient.toLowerCase().trim());
    }

    // 3. İki vektör (malzeme) arasındaki Kosinüs Benzerliğini hesaplayan matematiksel motor
    public double calculateCosineSimilarity(double[] vectorA, double[] vectorB) {
        if (vectorA == null || vectorB == null) return 0.0;

        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;

        for (int i = 0; i < VECTOR_SIZE; i++) {
            dotProduct += vectorA[i] * vectorB[i];
            normA += Math.pow(vectorA[i], 2);
            normB += Math.pow(vectorB[i], 2);
        }

        if (normA == 0.0 || normB == 0.0) return 0.0;
        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }
}