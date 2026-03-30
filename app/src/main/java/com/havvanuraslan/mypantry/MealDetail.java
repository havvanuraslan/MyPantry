package com.havvanuraslan.mypantry;

import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.List;

public class MealDetail {
    @SerializedName("idMeal")
    private String idMeal;

    @SerializedName("strMeal")
    private String strMeal;

    @SerializedName("strCategory") // EKSİK OLAN KISIM 1
    private String strCategory;

    @SerializedName("strInstructions") // EKSİK OLAN KISIM 2
    private String strInstructions;

    @SerializedName("strMealThumb")
    private String strMealThumb;

    // Malzemeler (Arkadaşının yazdığı gibi)
    @SerializedName("strIngredient1") private String i1;
    @SerializedName("strIngredient2") private String i2;
    @SerializedName("strIngredient3") private String i3;
    @SerializedName("strIngredient4") private String i4;
    @SerializedName("strIngredient5") private String i5;
    @SerializedName("strIngredient6") private String i6;
    @SerializedName("strIngredient7") private String i7;
    @SerializedName("strIngredient8") private String i8;
    @SerializedName("strIngredient9") private String i9;
    @SerializedName("strIngredient10") private String i10;
    @SerializedName("strIngredient11") private String i11;
    @SerializedName("strIngredient12") private String i12;
    @SerializedName("strIngredient13") private String i13;
    @SerializedName("strIngredient14") private String i14;
    @SerializedName("strIngredient15") private String i15;
    @SerializedName("strIngredient16") private String i16;
    @SerializedName("strIngredient17") private String i17;
    @SerializedName("strIngredient18") private String i18;
    @SerializedName("strIngredient19") private String i19;
    @SerializedName("strIngredient20") private String i20;

    // Getter Metotları
    public String getIdMeal() { return idMeal; }
    public String getStrMeal() { return strMeal; }
    public String getStrMealThumb() { return strMealThumb; }

    // ARTIK BU METOTLAR VAR OLDUĞU İÇİN HATA VERMEYECEK:
    public String getStrCategory() { return strCategory; }
    public String getStrInstructions() { return strInstructions; }

    public List<String> getIngredientList() {
        List<String> list = new ArrayList<>();

        addIfValid(list, i1);
        addIfValid(list, i2);
        addIfValid(list, i3);
        addIfValid(list, i4);
        addIfValid(list, i5);
        addIfValid(list, i6);
        addIfValid(list, i7);
        addIfValid(list, i8);
        addIfValid(list, i9);
        addIfValid(list, i10);
        addIfValid(list, i11);
        addIfValid(list, i12);
        addIfValid(list, i13);
        addIfValid(list, i14);
        addIfValid(list, i15);
        addIfValid(list, i16);
        addIfValid(list, i17);
        addIfValid(list, i18);
        addIfValid(list, i19);
        addIfValid(list, i20);

        return list;
    }

    private void addIfValid(List<String> list, String value) {
        if (value != null && !value.trim().isEmpty()) {
            list.add(value.trim().toLowerCase());
        }
    }
}