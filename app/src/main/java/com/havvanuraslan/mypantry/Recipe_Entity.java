package com.havvanuraslan.mypantry;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.Ignore;
import java.io.Serializable;

@Entity(tableName = "recipes")
public class Recipe_Entity implements Serializable {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "id")
    public int id;

    @ColumnInfo(name = "name")
    public String name;

    @ColumnInfo(name = "minutes")
    public Integer minutes;

    @ColumnInfo(name = "tags")
    public String tags;

    @ColumnInfo(name = "ingredients")
    public String ingredients;

    @ColumnInfo(name = "steps")
    public String steps;

    @ColumnInfo(name = "recipe_vector")
    public String recipe_vector;

    @ColumnInfo(name = "favorite_recipe")
    public Integer favorite_recipe = 0;

    @Ignore
    public String image_url;

    @Ignore
    public boolean isFavorite = false;

    public Recipe_Entity() {}
}