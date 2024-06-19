package me.theorenter.configurablevillagers.utils;

import me.theorenter.configurablevillagers.exceptions.CustomTradeKeyAlreadyExistsException;
import me.theorenter.configurablevillagers.exceptions.NoSuchCustomTradeKeyException;
import me.theorenter.configurablevillagers.object.CustomTrade;
import me.theorenter.configurablevillagers.object.VillagerWrapper;
import org.bukkit.entity.Villager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Storage of all custom merchant recipes.
 * Recipes are sent here in the process of being read by {@link TradeLoader}.
 */
public final class TradeStorage {
    private final Map<String, CustomTrade> MERCHANT_TRADES = new HashMap<>();

    private final Map<Villager.Profession, HashSet<String>> PROFESSION_INDEX_MAP = new HashMap<>();
    private final Map<Villager.Type, HashSet<String>> TYPE_INDEX_MAP = new HashMap<>();
    private final Map<Integer, HashSet<String>> LEVEL_INDEX_MAP = new HashMap<>();
    public TradeStorage() {

        PROFESSION_INDEX_MAP.put(Villager.Profession.ARMORER, new HashSet<>());
        PROFESSION_INDEX_MAP.put(Villager.Profession.BUTCHER, new HashSet<>());
        PROFESSION_INDEX_MAP.put(Villager.Profession.CLERIC, new HashSet<>());
        PROFESSION_INDEX_MAP.put(Villager.Profession.CARTOGRAPHER, new HashSet<>());
        PROFESSION_INDEX_MAP.put(Villager.Profession.FARMER, new HashSet<>());
        PROFESSION_INDEX_MAP.put(Villager.Profession.FISHERMAN, new HashSet<>());
        PROFESSION_INDEX_MAP.put(Villager.Profession.FLETCHER, new HashSet<>());
        PROFESSION_INDEX_MAP.put(Villager.Profession.LEATHERWORKER, new HashSet<>());
        PROFESSION_INDEX_MAP.put(Villager.Profession.LIBRARIAN, new HashSet<>());
        PROFESSION_INDEX_MAP.put(Villager.Profession.MASON, new HashSet<>());
        PROFESSION_INDEX_MAP.put(Villager.Profession.SHEPHERD, new HashSet<>());
        PROFESSION_INDEX_MAP.put(Villager.Profession.TOOLSMITH, new HashSet<>());
        PROFESSION_INDEX_MAP.put(Villager.Profession.WEAPONSMITH, new HashSet<>());

        TYPE_INDEX_MAP.put(Villager.Type.DESERT, new HashSet<>());
        TYPE_INDEX_MAP.put(Villager.Type.JUNGLE, new HashSet<>());
        TYPE_INDEX_MAP.put(Villager.Type.SNOW, new HashSet<>());
        TYPE_INDEX_MAP.put(Villager.Type.PLAINS, new HashSet<>());
        TYPE_INDEX_MAP.put(Villager.Type.SAVANNA, new HashSet<>());
        TYPE_INDEX_MAP.put(Villager.Type.SWAMP, new HashSet<>());
        TYPE_INDEX_MAP.put(Villager.Type.TAIGA, new HashSet<>());

        LEVEL_INDEX_MAP.put(1, new HashSet<>());
        LEVEL_INDEX_MAP.put(2, new HashSet<>());
        LEVEL_INDEX_MAP.put(3, new HashSet<>());
        LEVEL_INDEX_MAP.put(4, new HashSet<>());
        LEVEL_INDEX_MAP.put(5, new HashSet<>());
    }

     /**
     * @param recipeID The string identifier of the merchant recipe.
     * @param customTrade The custom merchant recipe.
     * @throws CustomTradeKeyAlreadyExistsException If custom trade with specified recipeID already exist.
     */
    public void register(@NotNull final String recipeID, @NotNull final CustomTrade customTrade) throws CustomTradeKeyAlreadyExistsException {
        if (MERCHANT_TRADES.containsKey(recipeID))
            throw new CustomTradeKeyAlreadyExistsException();

        MERCHANT_TRADES.put(recipeID, customTrade);

        // professions
        if (customTrade.getProfessions() != null)
            customTrade.getProfessions().forEach(profession -> PROFESSION_INDEX_MAP.get(profession).add(recipeID));
        else {
            PROFESSION_INDEX_MAP.forEach((k, v) -> v.add(recipeID));
        }
        // types
        if (customTrade.getTypes() != null)
            customTrade.getTypes().forEach(type -> TYPE_INDEX_MAP.get(type).add(recipeID));
        else {
            TYPE_INDEX_MAP.forEach((k, v) -> v.add(recipeID));
        }
        // levels
        if (customTrade.getLevels() != null)
            customTrade.getLevels().forEach(level -> LEVEL_INDEX_MAP.get(level).add(recipeID));
        else {
            LEVEL_INDEX_MAP.forEach((k, v) -> v.add(recipeID));
        }
    }

    /**
     * @param customTrade The custom merchant recipe.
     * @throws CustomTradeKeyAlreadyExistsException If custom trade with specified recipeID already exist.
     */
    public void register(@NotNull final CustomTrade customTrade) throws CustomTradeKeyAlreadyExistsException {
        register(customTrade.getID(), customTrade);
    }

    /**
     * @param recipeID The string identifier of the merchant recipe.
     * @throws NoSuchCustomTradeKeyException If custom trade with specified recipeID doesn't exist.
     */
    @SuppressWarnings("unused")
    public void unregister(@NotNull final String recipeID) throws NoSuchCustomTradeKeyException {
        if (!MERCHANT_TRADES.containsKey(recipeID))
            throw new NoSuchCustomTradeKeyException();

        MERCHANT_TRADES.remove(recipeID);
    }

    /**
     * @param recipeID The string identifier of the merchant recipe.
     * @return The {@link CustomTrade}.
     * @throws NoSuchCustomTradeKeyException If custom trade with specified recipeID already exist.
     */
    @NotNull
    @SuppressWarnings("unused")
    public CustomTrade get(@NotNull final String recipeID) throws NoSuchCustomTradeKeyException {
        CustomTrade c = MERCHANT_TRADES.get(recipeID);
        if (c == null)
            throw new NoSuchCustomTradeKeyException();
        return c;
    }

    @SuppressWarnings("unused")
    private Set<String> getAvailableTradesIdentifiers(@NotNull final Villager.Type type, @NotNull final Villager.Profession profession, int level, @NotNull List<String> currentTrades) {
        Set<String> typeIndex = TYPE_INDEX_MAP.get(type);
        Set<String> professionIndex = PROFESSION_INDEX_MAP.get(profession);
        Set<String> lvlIndex = LEVEL_INDEX_MAP.get(level);

        Set<String> availableIdentifiers = new HashSet<>(professionIndex);
        availableIdentifiers.retainAll(typeIndex);
        availableIdentifiers.retainAll(lvlIndex);
        availableIdentifiers.retainAll(new HashSet<>(currentTrades));
        return availableIdentifiers;
    }

    @NotNull
    private Set<String> getAvailableTradesIdentifiers(@NotNull final Villager.Type type, @NotNull final Villager.Profession profession, int level) {
        Set<String> typeIndex = TYPE_INDEX_MAP.get(type);
        Set<String> professionIndex = PROFESSION_INDEX_MAP.get(profession);
        Set<String> lvlIndex = LEVEL_INDEX_MAP.get(level);

        Set<String> availableIdentifiers = new HashSet<>(professionIndex);
        availableIdentifiers.retainAll(typeIndex);
        availableIdentifiers.retainAll(lvlIndex);
        return availableIdentifiers;
    }

    @Nullable
    public CustomTrade getRandomAvailableTrade(@NotNull final Villager villager) {

        ArrayList<String> availableTrades = new ArrayList<>(getAvailableTradesIdentifiers(villager.getVillagerType(), villager.getProfession(), villager.getVillagerLevel()));

        VillagerWrapper vw = new VillagerWrapper(villager);

        if (availableTrades.size() == 0) return null;

        CustomTrade trade = null;
        while (trade == null && !availableTrades.isEmpty()) {
            int rand = new Random().nextInt(availableTrades.size());
            String ID = availableTrades.get(rand);
            CustomTrade candidate = MERCHANT_TRADES.get(ID);

            if (!vw.hasCustomTrade(ID))
                trade = candidate;
            else {
                availableTrades.remove(rand);
            }
        }
        return trade;
    }

    @Nullable
    public CustomTrade getRandomAvailableTrade(@NotNull final Villager villager, int lvl) {

        ArrayList<String> availableTrades = new ArrayList<>(getAvailableTradesIdentifiers(villager.getVillagerType(), villager.getProfession(), lvl));

        VillagerWrapper vw = new VillagerWrapper(villager);

        if (availableTrades.size() == 0) return null;

        CustomTrade trade = null;
        while (trade == null && !availableTrades.isEmpty()) {
            int rand = new Random().nextInt(availableTrades.size());
            String ID = availableTrades.get(rand);
            CustomTrade candidate = MERCHANT_TRADES.get(ID);

            if (!vw.hasCustomTrade(ID))
                trade = candidate;
            else {
                availableTrades.remove(rand);
            }
        }
        return trade;
    }
}
