package com.havvanuraslan.mypantry;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class HomeSuggestionsAdapter extends RecyclerView.Adapter<HomeSuggestionsAdapter.ViewHolder> {

    private final List<Recipe_Entity> suggestionList;
    private final Context context;

    public HomeSuggestionsAdapter(Context context, List<Recipe_Entity> suggestionList) {
        this.context = context;
        this.suggestionList = suggestionList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recipe, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Recipe_Entity recipe = suggestionList.get(position);

        if (recipe != null) {
            if (recipe.name != null && !recipe.name.isEmpty()) {
                holder.tvRecipeName.setText(recipe.name);
            } else {
                holder.tvRecipeName.setText("Delicious Smart Meal");
            }

            if (holder.tvRecipeMinutes != null) {
                int minutes = (recipe.minutes != null) ? recipe.minutes : 25;
                holder.tvRecipeMinutes.setText(minutes + " min");
            }

            if (holder.tvRecipeScore != null) {
                holder.tvRecipeScore.setText("Suggested");
            }

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
                try {
                    Intent intent = new Intent(context, RecipeDetailActivity.class);
                    intent.putExtra("RECIPE_ID", recipe.id);
                    context.startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });


            if (holder.btnFavoriteItem != null) {
                boolean isFav = (recipe.favorite_recipe != null && recipe.favorite_recipe == 1);
                holder.btnFavoriteItem.setImageResource(isFav ?
                        android.R.drawable.btn_star_big_on : android.R.drawable.btn_star_big_off);

                holder.btnFavoriteItem.setOnClickListener(v -> {
                    try {
                        int currentStatus = (recipe.favorite_recipe != null) ? recipe.favorite_recipe : 0;
                        int newStatus = (currentStatus == 1) ? 0 : 1;
                        recipe.favorite_recipe = newStatus;

                        holder.btnFavoriteItem.setImageResource(newStatus == 1 ?
                                android.R.drawable.btn_star_big_on : android.R.drawable.btn_star_big_off);

                        java.util.concurrent.ExecutorService executor = java.util.concurrent.Executors.newSingleThreadExecutor();
                        executor.execute(() -> {
                            try {
                                Recipe_Database.getDbInstance(context).recipeDao().updateFavoriteStatus(recipe.id, newStatus);

                                com.google.firebase.auth.FirebaseUser user = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
                                if (user != null) {
                                    com.google.firebase.database.DatabaseReference favRef = com.google.firebase.database.FirebaseDatabase.getInstance()
                                            .getReference("users")
                                            .child(user.getUid())
                                            .child("favorites")
                                            .child(String.valueOf(recipe.id));

                                    if (newStatus == 1) {
                                        favRef.setValue(true);
                                    } else {
                                        favRef.removeValue();
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        });

                        String toastMsg = (newStatus == 1) ? "Added to favorites! ✨" : "Removed from favorites.";
                        android.widget.Toast.makeText(context, toastMsg, android.widget.Toast.LENGTH_SHORT).show();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }
        }
    }

    @Override
    public int getItemCount() {
        return suggestionList != null ? suggestionList.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvRecipeName, tvRecipeMinutes, tvRecipeScore;
        ImageView ivRecipeImage, btnFavoriteItem;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRecipeName = itemView.findViewById(R.id.tvRecipeName);
            ivRecipeImage = itemView.findViewById(R.id.ivRecipe);
            tvRecipeMinutes = itemView.findViewById(R.id.tvRecipeTime);
            tvRecipeScore = itemView.findViewById(R.id.chipScore);
            btnFavoriteItem = itemView.findViewById(R.id.ivFavorite);
        }
    }
}