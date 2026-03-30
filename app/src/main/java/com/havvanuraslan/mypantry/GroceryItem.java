package com.havvanuraslan.mypantry;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "grocery_items") // Farklı bir tablo ismi veriyoruz
public class GroceryItem {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public String name;
    public boolean isChecked; // Ürün alındı mı? (Checkbox için)

    public GroceryItem(String name) {
        this.name = name;
        this.isChecked = false; // İlk eklendiğinde tiksiz olsun
    }

    public String getName() {
        return name;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }
}