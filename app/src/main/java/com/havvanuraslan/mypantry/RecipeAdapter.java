package com.havvanuraslan.mypantry;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.ViewHolder> {

    private List<Recommendation_Engine.RecipeScore> recipeList;
    private Context context;
    private OnRecipeClickListener listener;

    public interface OnRecipeClickListener {
        void onRecipeClick(int recipeId);
        void onFavoriteClick(Recipe_Entity recipe);
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

        holder.tvName.setText(capitalizeWords(recipe.name));

        if (recipe.minutes != null && recipe.minutes > 0) {
            holder.tvRecipeTime.setText("⏱️ " + recipe.minutes + " min");
            holder.tvRecipeTime.setVisibility(View.VISIBLE);
        } else {
            holder.tvRecipeTime.setVisibility(View.GONE);
        }

        double score = item.matchScore;
        holder.tvScore.setText(String.format("Match: %%%.1f", score));
        if (score >= 80.0) holder.tvScore.setTextColor(Color.parseColor("#2E7D32"));
        else if (score >= 50.0) holder.tvScore.setTextColor(Color.parseColor("#EF6C00"));
        else holder.tvScore.setTextColor(Color.parseColor("#C62828"));

        recipe.isFavorite = (recipe.favorite_recipe != null && recipe.favorite_recipe == 1);
        updateFavoriteUI(holder, recipe.isFavorite);

        holder.cvFavorite.setOnClickListener(v -> {
            recipe.isFavorite = !recipe.isFavorite;
            recipe.favorite_recipe = recipe.isFavorite ? 1 : 0;

            updateFavoriteUI(holder, recipe.isFavorite);

            if (listener != null) {
                listener.onFavoriteClick(recipe);
            }
        });

        if (holder.ivRecipeImage != null) {
            int targetDrawableId = R.drawable.pantry_spices;

            StringBuilder sourceBuilder = new StringBuilder();
            if (recipe.ingredients != null && !recipe.ingredients.isEmpty()) {
                sourceBuilder.append(recipe.ingredients.toLowerCase()).append(" ");
            }
            if (recipe.name != null && !recipe.name.isEmpty()) {
                sourceBuilder.append(recipe.name.toLowerCase());
            }
            String sourceText = sourceBuilder.toString();

            if (!sourceText.isEmpty()) {
                if (sourceText.contains("coffee") || sourceText.contains("tea") || sourceText.contains("juice") || sourceText.contains("milkshake") || sourceText.contains("drink") || sourceText.contains("smoothie")) {
                    targetDrawableId = R.drawable.drinks;
                } else if (sourceText.contains("chocolate") || sourceText.contains("sugar") || sourceText.contains("cocoa") || sourceText.contains("cake") || sourceText.contains("cookie") || sourceText.contains("pudding") || sourceText.contains("dessert")) {
                    targetDrawableId = R.drawable.desserts;
                } else if (sourceText.contains("fish") || sourceText.contains("shrimp") || sourceText.contains("salmon") || sourceText.contains("tuna") || sourceText.contains("seafood")) {
                    targetDrawableId = R.drawable.seafood;
                } else if (sourceText.contains("meat") || sourceText.contains("chicken") || sourceText.contains("beef") || sourceText.contains("pork") || sourceText.contains("lamb") || sourceText.contains("steak") || sourceText.contains("tavuk") || sourceText.contains("et")) {
                    targetDrawableId = R.drawable.meat_and_veggies;
                } else if (sourceText.contains("rice") || sourceText.contains("lentil") || sourceText.contains("beans") || sourceText.contains("chickpea") || sourceText.contains("pasta") || sourceText.contains("spaghetti") || sourceText.contains("makarna") || sourceText.contains("pilav")) {
                    targetDrawableId = R.drawable.grains_and_pulses;
                } else if (sourceText.contains("flour") || sourceText.contains("yeast") || sourceText.contains("baking powder") || sourceText.contains("bread") || sourceText.contains("pie") || sourceText.contains("dough") || sourceText.contains("börek") || sourceText.contains("poğaça")) {
                    targetDrawableId = R.drawable.bakery_and_flour;
                } else if (sourceText.contains("breakfast") || sourceText.contains("toast") || sourceText.contains("pancake") || sourceText.contains("egg") || sourceText.contains("yumurta") || sourceText.contains("tost")) {
                    targetDrawableId = R.drawable.breakfast;
                } else if (sourceText.contains("salad") || sourceText.contains("mezze") || sourceText.contains("lettuce") || sourceText.contains("cucumber") || sourceText.contains("olive oil") || sourceText.contains("salata")) {
                    targetDrawableId = R.drawable.salads;
                } else if (sourceText.contains("tomato") || sourceText.contains("potato") || sourceText.contains("onion") || sourceText.contains("garlic") || sourceText.contains("carrot") || sourceText.contains("broccoli") || sourceText.contains("sebze") || sourceText.contains("patates")) {
                    targetDrawableId = R.drawable.mixed_vegetables;
                } else {
                    targetDrawableId = R.drawable.world_cuisine_1;
                }
            }

            holder.ivRecipeImage.setImageResource(targetDrawableId);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onRecipeClick(recipe.id);
            }
        });
    }

    private void updateFavoriteUI(ViewHolder holder, boolean isFavorite) {
        holder.ivFavorite.setImageResource(isFavorite ?
                android.R.drawable.btn_star_big_on : android.R.drawable.btn_star_big_off);
    }

    @Override
    public int getItemCount() {
        return recipeList != null ? recipeList.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvScore, tvRecipeTime;
        ImageView ivRecipeImage, ivFavorite;
        CardView cvFavorite;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvRecipeName);
            tvScore = itemView.findViewById(R.id.chipScore);
            ivRecipeImage = itemView.findViewById(R.id.ivRecipe);
            tvRecipeTime = itemView.findViewById(R.id.tvRecipeTime);
            ivFavorite = itemView.findViewById(R.id.ivFavorite);
            cvFavorite = itemView.findViewById(R.id.cvFavorite);
        }
    }

    private String capitalizeWords(String str) {
        if (str == null || str.isEmpty()) return str;
        String[] words = str.split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (String w : words) {
            if (w.length() > 0) sb.append(w.substring(0, 1).toUpperCase()).append(w.substring(1).toLowerCase()).append(" ");
        }
        return sb.toString().trim();
    }
}