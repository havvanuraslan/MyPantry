package com.havvanuraslan.mypantry;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo; // Gerekirse ekle

@Entity(tableName = "pantry_items")
public class PantryItem {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public String name;
    private double quantity;
    private String unit;

    public PantryItem(String name, double quantity, String unit) {
        this.name = name;
        this.quantity= quantity;
        this.unit = unit;
    }


    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public double getQuantity() {
        return quantity;
    }
    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public String getUnit() {
        return unit;
    }
    public void setUnit(String unit) {
        this.unit = unit;
    }
}