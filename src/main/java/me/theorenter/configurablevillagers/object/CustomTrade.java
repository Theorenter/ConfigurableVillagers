package me.theorenter.configurablevillagers.object;

import org.bukkit.entity.Villager;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;


public class CustomTrade {
    private final String ID;
    private final MerchantRecipe recipe;
    private List<Villager.Profession> professions;
    private List<Integer> levels;

    private List<Villager.Type> types;
    public CustomTrade(@NotNull final String ID,
                       @NotNull final ItemStack result,
                       int maxUses)
    {
        this.ID = ID;

        if (maxUses < 0)
            throw new IllegalArgumentException();

        this.recipe = new MerchantRecipe(result, maxUses);
        this.levels = Arrays.asList(1, 2, 3, 4, 5);
    }

    public void addIngredient(@NotNull final ItemStack ingredient) {
        recipe.addIngredient(ingredient);
    }

    public void setPriceMultiplier(float priceMultiplier) {
        this.recipe.setPriceMultiplier(priceMultiplier);
    }

    public void setVillagerExp(int villagerExp) {
        recipe.setVillagerExperience(villagerExp);
    }

    public void setPlayerExp(boolean flag) {
        recipe.setExperienceReward(flag);
    }

    public void setAvailableProfessions(@NotNull final List<Villager.Profession> professions) {
        this.professions = professions;
    }

    public void setTradeLevels(@NotNull final List<Integer> levels) {
        this.levels = levels;
    }

    public void setAvailableTypes(@NotNull final List<Villager.Type> types) {
        this.types = types;
    }

    public void setIgnoreDiscounts(boolean flag) {
        this.recipe.setIgnoreDiscounts(flag);
    }

    @NotNull
    public String getID() {
        return ID;
    }

    @NotNull
    public MerchantRecipe getRecipe() {
        return recipe;
    }


    public List<Integer> getLevels() {
        return levels;
    }

    public List<Villager.Type> getTypes() {
        return types;
    }

    public List<Villager.Profession> getProfessions() {
        return professions;
    }
}
