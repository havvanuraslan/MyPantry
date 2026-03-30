package com.havvanuraslan.mypantry;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface PantryDao {
    @Insert
    void insert(PantryItem item); // Ekleme

    @Delete
    void delete(PantryItem item); // Silme

    @Update
    void update(PantryItem item); // Güncelleme (+/- butonları için)

    @Query("SELECT * FROM pantry_items ORDER BY id DESC")
    List<PantryItem> getAllItems(); // Hepsini getir
}