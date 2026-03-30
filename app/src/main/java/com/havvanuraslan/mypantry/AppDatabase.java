package com.havvanuraslan.mypantry;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

// DİKKAT: entities kısmına GroceryItem.class'ı ekledik ve versiyonu 2 yaptık
@Database(entities = {PantryItem.class, GroceryItem.class}, version = 2)
public abstract class AppDatabase extends RoomDatabase {

    public abstract PantryDao pantryDao();   // Kiler için
    public abstract GroceryDao groceryDao(); // Alışveriş Listesi için (Bunu ekledik)

    private static AppDatabase INSTANCE;

    public static AppDatabase getDbInstance(Context context) {
        if(INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "MyPantryDB")
                    .allowMainThreadQueries()
                    .fallbackToDestructiveMigration() // Veritabanı yapısı değişince çökmemesi için
                    .build();
        }
        return INSTANCE;
    }
}