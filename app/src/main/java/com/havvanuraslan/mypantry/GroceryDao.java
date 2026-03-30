package com.havvanuraslan.mypantry;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface GroceryDao {
    @Insert
    void insert(GroceryItem item);

    @Delete
    void delete(GroceryItem item);

    @Update
    void update(GroceryItem item);

    @Query("SELECT * FROM grocery_items ORDER BY id DESC")
    List<GroceryItem> getAllItems();
}