package com.havvanuraslan.mypantry;

import java.util.List;

public class RecommendedRecipe {
    // The original recipe object fetched from the API by Person 2 (It is the class written by them)
    private Recipe originalRecipe;

    // The relevancy score calculated by you
    private int relevancyScore;

    // List of missing ingredients in the user's pantry (to be shown in red on the UI)
    private List<String> missingIngredients;

    // Notes for ingredients that have substitutes
    // E.g.: "No lemon juice, but you can use Vinegar"
    private List<String> substitutionNotes;

    public RecommendedRecipe(Recipe originalRecipe, int relevancyScore,
                             List<String> missingIngredients, List<String> substitutionNotes) {
        this.originalRecipe = originalRecipe;
        this.relevancyScore = relevancyScore;
        this.missingIngredients = missingIngredients;
        this.substitutionNotes = substitutionNotes;
    }

    // Getter methods...
    public int getScore() { return relevancyScore; }
    public Recipe getRecipe() { return originalRecipe; }
    public List<String> getMissingIngredients() { return missingIngredients; }
    public List<String> getSubstitutionNotes() { return substitutionNotes; }

}
