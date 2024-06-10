package org.example.serializedDatas;

import java.util.ArrayList;
import java.util.List;

public class OrderSerialized {
    private ArrayList<String> ingredients;

    public ArrayList<String> getIngredients() {
        return ingredients;
    }

    public void setIngredients(ArrayList<String> ingredients) {
        this.ingredients = ingredients;
    }

    public OrderSerialized(ArrayList<String> ingredients) {
        this.ingredients = ingredients;
    }
}
