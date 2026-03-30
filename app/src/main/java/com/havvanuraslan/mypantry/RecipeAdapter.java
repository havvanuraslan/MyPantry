package com.havvanuraslan.mypantry;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.ViewHolder> {

    // Yapay zeka motorumuzun çıktı sınıfını kullanıyoruz
    private List<Recommendation_Engine.RecipeScore> recipeList;
    private Context context;
    private OnRecipeClickListener listener;

    public interface OnRecipeClickListener {
        void onRecipeClick(int recipeId); // Kendi SQLite veritabanımızın ID'si (Integer)
    }

    public RecipeAdapter(Context context, List<Recommendation_Engine.RecipeScore> recipeList, OnRecipeClickListener listener) {
        this.context = context;
        this.recipeList = recipeList;
        this.listener = listener;
    }

    public void updateList(List<Recommendation_Engine.RecipeScore> newList) {
        this.recipeList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recipe, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Recommendation_Engine.RecipeScore item = recipeList.get(position);
        Recipe_Entity recipe = item.recipe;

        // 1. Tarif Adı (İlk harfleri büyük yapıyoruz ki şık dursun)
        holder.tvName.setText(capitalizeWords(recipe.name));

        // 2. Eşleşme Yüzdesi (AI Score)
        double score = item.matchScore;
        holder.tvScore.setText(String.format("Match: %%%.1f", score));

        // Skora göre dinamik renk değişimi (UI/UX)
        if (score >= 80.0) {
            holder.tvScore.setTextColor(Color.parseColor("#2E7D32")); // Koyu Yeşil
        } else if (score >= 50.0) {
            holder.tvScore.setTextColor(Color.parseColor("#EF6C00")); // Turuncu
        } else {
            holder.tvScore.setTextColor(Color.parseColor("#C62828")); // Kırmızı
        }

        // Eksik malzemeler satırını şimdilik gizliyoruz
        if(holder.tvMissingInfo != null) {
            holder.tvMissingInfo.setVisibility(View.GONE);
        }

        // Çevrimdışı (Offline) çalıştığımız için internetten resim indirmiyoruz.
        // Onun yerine varsayılan bir ikon gösteriyoruz.
        if(holder.ivRecipeImage != null) {
            holder.ivRecipeImage.setImageResource(R.drawable.ic_launcher_foreground);
        }

        // Tıklama olayı (Detay sayfasına ID gönderir)
        holder.itemView.setOnClickListener(v -> listener.onRecipeClick(recipe.id));
    }

    @Override
    public int getItemCount() {
        return recipeList != null ? recipeList.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvScore, tvMissingInfo;
        ImageView ivRecipeImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvRecipeName);
            tvScore = itemView.findViewById(R.id.chipScore);
            tvMissingInfo = itemView.findViewById(R.id.tvMissingInfo);
            ivRecipeImage = itemView.findViewById(R.id.ivRecipe);
        }
    }

    // Görüntü kirliliğini engellemek için tüm harfleri küçültüp sadece baş harfleri büyütür
    private String capitalizeWords(String str) {
        if (str == null || str.isEmpty()) return str;
        String[] words = str.split("\\s+");
        StringBuilder capitalizeWord = new StringBuilder();
        for (String w : words) {
            if(w.length() > 0) {
                String first = w.substring(0, 1);
                String afterfirst = w.substring(1);
                capitalizeWord.append(first.toUpperCase()).append(afterfirst.toLowerCase()).append(" ");
            }
        }
        return capitalizeWord.toString().trim();
    }
}