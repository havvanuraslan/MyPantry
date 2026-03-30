package com.havvanuraslan.mypantry;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "recipes")
public class Recipe_Entity {

    @PrimaryKey
    public int id;

    public String name;
    public Integer minutes;
    public String tags;
    public String ingredients;

    // YENİ EKLENEN SÜTUN: Python'daki steps ile birebir aynı isimde olmalı
    public String steps;

    @ColumnInfo(name = "recipe_vector")
    public String recipeVector;
}