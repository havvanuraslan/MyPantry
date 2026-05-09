package com.havvanuraslan.mypantry;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "grocery_items")
public class GroceryItem {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public String name;
    public double quantity;
    public String unit;
    public boolean isChecked;

    public GroceryItem(String name, double quantity, String unit) {
        this.name = name;
        this.quantity = quantity;
        this.unit = unit;
        this.isChecked = false;
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

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }
}