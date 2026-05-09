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

        if(holder.ivRecipeImage != null) {
            holder.ivRecipeImage.setImageResource(R.drawable.ic_launcher_foreground);
        }

        holder.itemView.setOnClickListener(v -> listener.onRecipeClick(recipe.id));
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