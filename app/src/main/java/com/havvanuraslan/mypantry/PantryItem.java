package com.havvanuraslan.mypantry;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "pantry_items") // Bu satır onu veritabanı tablosu yapar
public class PantryItem {

    @PrimaryKey(autoGenerate = true)
    public int id; // Her ürünün benzersiz kimliği

    public String name;
    public int quantity;

    public PantryItem(String name, int quantity) {
        this.name = name;
        this.quantity = quantity;
    }

    // Getter metodları (Adapter için lazım olabilir)
    public String getName() { return name; }
    public int getQuantity() { return quantity; }
}