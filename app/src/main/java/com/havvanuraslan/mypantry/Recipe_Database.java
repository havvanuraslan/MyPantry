package com.havvanuraslan.mypantry;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

// Sadece RecipeEntity var. Versiyon 1.
@Database(entities = {Recipe_Entity.class}, version = 1, exportSchema = false)
public abstract class Recipe_Database extends RoomDatabase {

    public abstract Recipe_Dao recipeDao();

    private static Recipe_Database INSTANCE;

    public static Recipe_Database getDbInstance(Context context) {
        if(INSTANCE == null) {
            synchronized (Recipe_Database.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    Recipe_Database.class, "MyPantry_RecipesDB") // İsim farklı!
                            .createFromAsset("MyPantry.db") // Python'dan gelen devasa dosya
                            .allowMainThreadQueries()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}