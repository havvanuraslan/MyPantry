package com.havvanuraslan.mypantry;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {Recipe_Entity.class}, version = 1, exportSchema = false)
public abstract class Recipe_Database extends RoomDatabase {

    public abstract Recipe_Dao recipeDao();

    private static volatile Recipe_Database INSTANCE;

    public static Recipe_Database getDbInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (Recipe_Database.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    Recipe_Database.class, "MyPantry_Master_DB")
                            .createFromAsset("MyPantry.db")
                            .fallbackToDestructiveMigration()
                            .allowMainThreadQueries()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}