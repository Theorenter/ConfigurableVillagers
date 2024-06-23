package me.theorenter.configurablevillagers.utils;

import me.theorenter.configurablevillagers.ConfigurableVillagers;
import me.theorenter.configurablevillagers.exceptions.CustomTradeKeyAlreadyExistsException;
import me.theorenter.configurablevillagers.exceptions.IngredientsNotFoundException;
import me.theorenter.configurablevillagers.exceptions.ResultNotFoundException;
import me.theorenter.configurablevillagers.object.CustomTrade;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Villager;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Loads all trades from the trades.yml config in plugin folder.
 */
public final class TradeLoader {
   private final ConfigurableVillagers plugin;
   private final FileConfiguration fileConfig;

    /**
     * Constructor of the loader.
     * @param plugin The main plugin.
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
   public TradeLoader(@NotNull final ConfigurableVillagers plugin) throws IOException, InvalidConfigurationException {
       this.plugin = plugin;

       File file;

       file = new File(plugin.getDataFolder() + File.separator + "settings",
               "trades.yml");
       if (!file.exists()) {
           file.getParentFile().mkdirs();
           plugin.saveResource("settings" + File.separator + "trades.yml", false);
       }

       this.fileConfig = new YamlConfiguration();
       this.fileConfig.load(file);
   }


    /**
     * Loads all custom recipes from the trades.yml config in plugin folder.
     */
    public void loadAll() {

        // Each key is an itemID
        List<Object> customTrades = (List<Object>) fileConfig.getList("trades");

        if (customTrades == null || customTrades.isEmpty()) {
            plugin.getLogger().warning("No custom trades were found in the trades.yml file!");
            return;
        }

        int loadedTrades = 0;

        for (Object recipeObject : customTrades) {
            if (!(recipeObject instanceof LinkedHashMap)) {
                plugin.getLogger().warning("Unable to read recipes. Found invalid recipe format");
                plugin.getLogger().warning("This recipe will be skipped!");
            }
            else {
                LinkedHashMap recipeMap = (LinkedHashMap) recipeObject;
                String ID = (String) recipeMap.get("ID");
                try {
                    CustomTrade ct = load(recipeMap, ID);
                    loadedTrades++;
                    plugin.getTradeStorage().register(ct);
                } catch (ResultNotFoundException e) {
                    plugin.getLogger().warning("Unable to read \"" + ID + "\" recipe. Invalid recipe result!");
                    e.printStackTrace();
                } catch (IngredientsNotFoundException e) {
                    plugin.getLogger().warning("Unable to read \"" + ID + "\" recipe. Invalid recipe ingredients!");
                    e.printStackTrace();
                } catch (CustomTradeKeyAlreadyExistsException e) {
                    plugin.getLogger().warning("Unable to register \"" + ID + "\" recipe. Recipe with this ID already registered!");
                }
            }
        }
        plugin.getLogger().info("Loaded " + loadedTrades + " / " + customTrades.size() + " custom trades from \"trades.yml\"");
    }

    /**
     * Load single custom merchant from the trades.yml
     * @param trade The section of the trade.
     * @param ID The identifier of the custom trade.
     * @return {@link CustomTrade}.
     */
    private CustomTrade load(@NotNull final LinkedHashMap<String, Object> trade, @NotNull final String ID) throws ResultNotFoundException, IngredientsNotFoundException {
        CustomTrade customTrade;

        ItemStack result;
        List<ItemStack> ingredients;
        ItemStack firstIngredient;
        ItemStack secondIngredient = null;

        int maxUses = (int) trade.getOrDefault("max-uses", 12);
        double priceMultiplier = (double) trade.getOrDefault("price-multiplier", 0);
        int villagerExperience = (int) trade.getOrDefault("experience", 0);
        boolean giveExperienceToPlayer = (boolean) trade.getOrDefault("give-experience-to-player", false);

        // requirement variables
        List<Villager.Profession> professions = toVillagerProfessionList((List<String>) trade.getOrDefault("professions", Arrays.asList(
                Villager.Profession.ARMORER, Villager.Profession.BUTCHER, Villager.Profession.CARTOGRAPHER, Villager.Profession.CLERIC, Villager.Profession.FARMER, Villager.Profession.FISHERMAN,
                Villager.Profession.FLETCHER, Villager.Profession.LEATHERWORKER, Villager.Profession.LIBRARIAN, Villager.Profession.MASON, Villager.Profession.SHEPHERD, Villager.Profession.TOOLSMITH)));
        List<Integer> levels = (List<Integer>) trade.getOrDefault("levels",
                Arrays.asList(1, 2, 3, 4, 5));
        List<Villager.Type> villagerTypes = toVillagerTypeList((List<String>) trade.getOrDefault("villager-types",
                Arrays.asList(Villager.Type.SNOW, Villager.Type.TAIGA, Villager.Type.PLAINS, Villager.Type.SWAMP, Villager.Type.JUNGLE, Villager.Type.SAVANNA, Villager.Type.DESERT)));

        LinkedHashMap resultData = (LinkedHashMap) trade.get("result");
        if (resultData == null) {
            throw new ResultNotFoundException();
        }
        result = toItemStack(resultData);
        ingredients = toItemStackList((List<Map<?, ?>>)trade.get("ingredients"));
        if (ingredients.size() < 1) {
            throw new IngredientsNotFoundException();
        }
        firstIngredient = ingredients.get(0);
        if (ingredients.size() > 1) {
            secondIngredient = ingredients.get(1);
        }

        customTrade = new CustomTrade(ID, result, maxUses);

        if (firstIngredient != null)
            customTrade.addIngredient(firstIngredient);
        else {
            plugin.getLogger().warning("Cannot load recipe \"" + ID + "\"! Invalid first ingredient!" );
            throw new IngredientsNotFoundException();
        }
        if (secondIngredient != null)
            customTrade.addIngredient(secondIngredient);
        if (priceMultiplier != 0)
            customTrade.setPriceMultiplier((float) priceMultiplier);
        if (villagerExperience != 0)
            customTrade.setVillagerExp(villagerExperience);
        customTrade.setPlayerExp(giveExperienceToPlayer);
        if (!professions.isEmpty())
            customTrade.setAvailableProfessions(professions);
        if (!levels.isEmpty())
            customTrade.setTradeLevels(levels);
        if (!villagerTypes.isEmpty())
            customTrade.setAvailableTypes(villagerTypes);

        return customTrade;
    }

    private List<Villager.Type> toVillagerTypeList(List<String> list) {
        List<Villager.Type> types = new ArrayList<>();
        for(String item : list) {
            Villager.Type type = Villager.Type.valueOf(item);
            types.add(type);
        }
        return types;
    }

    private List<Villager.Profession> toVillagerProfessionList(List<String> list) {
        List<Villager.Profession> professions = new ArrayList<>();
        for(String item : list) {
            Villager.Profession profession = Villager.Profession.valueOf(item);
            professions.add(profession);
        }
        return professions;
    }

    private List<ItemStack> toItemStackList(List<?> list) {
        List<ItemStack> ingredients = new ArrayList<>();
        for(Object item : list) {
            ingredients.add(toItemStack((Map<?, ?>) item));
        }
        return ingredients;
    }

    @SuppressWarnings("deprecation")
    private ItemStack toItemStack(Map<?, ?> map) {

        String material = (String) map.get("material");
        Integer amount = (Integer) map.get("amount");
        if(amount == null) amount = 1;

        String nbt = (String) map.get("nbt");

        ItemStack itemStack = new ItemStack(Material.valueOf(material), amount);

        // this is unsupported - use at your own risk
        if(nbt != null) {
            org.bukkit.UnsafeValues unsafe = Bukkit.getUnsafe();
            unsafe.modifyItemStack(itemStack, nbt);
        }

        ItemMeta itemMeta = itemStack.getItemMeta();

        // set enchantments
        List<ItemEnchantment> enchantments = toItemEnchantmentList(
                (ArrayList<?>) map.get("enchantments")
        );
        if(enchantments != null) {
            for(ItemEnchantment enchantment : enchantments) {
                itemMeta.addEnchant(
                        enchantment.getEnchantment(),
                        enchantment.getLevel(),
                        enchantment.ignoreLevelRestriction()
                );
            }
        }

        // set display name
        if(map.containsKey("displayName")) {
            String displayName = (String) map.get("displayName");
            itemMeta.setDisplayName(displayName);
        }

        // set lore
        if(map.containsKey("lore")) {
            List<?> loreList = (List<?>) map.get("lore");
            itemMeta.setLore(toLore(loreList));
        }

        // set attribute modifiers
        List<AttributeModifierWrapper> wrappers = toAttributeModifierWrapperList(
                (ArrayList<?>) map.get("attributeModifiers")
        );
        if(wrappers != null) {
            for(AttributeModifierWrapper wrapper : wrappers) {
                itemMeta.addAttributeModifier(
                        wrapper.geAttribute(),
                        wrapper.getModifier()
                );
            }
        }

        itemStack.setItemMeta(itemMeta);
        return itemStack;

    }

    private List<String> toLore(List<?> list) {
        if(list == null) return null;
        List<String> lore = new ArrayList<>();
        list.forEach(item -> {
            if(item instanceof String) lore.add((String) item);
        });
        if(lore.size() > 0) return lore;
        return null;
    }

    private List<AttributeModifierWrapper> toAttributeModifierWrapperList(ArrayList<?> list) {

        if(list == null) return null;

        List<AttributeModifierWrapper> wrappers = new ArrayList<>();

        for(Object item : list) {

            if(!(item instanceof HashMap)) continue;
            HashMap<?,?> map = (HashMap<?,?>) item;

            String nameString = (String) map.get("name");
            Attribute attribute = Attribute.valueOf(nameString.toUpperCase());

            Double amount;
            try {
                amount = (Double) map.get("amount");
            }
            catch(ClassCastException exception) {
                amount = Double.valueOf((Integer) map.get("amount"));
            }

            String operationString = (String) map.get("operation");
            AttributeModifier.Operation operation;

            switch(operationString.toUpperCase()) {
                case "ADD":
                    operation = AttributeModifier.Operation.ADD_NUMBER;
                    break;
                case "MULTIPLY":
                    operation = AttributeModifier.Operation.ADD_SCALAR;
                    break;
                case "MULTIPLY_ALL_MODIFIERS":
                    operation = AttributeModifier.Operation.MULTIPLY_SCALAR_1;
                    break;
                default:
                    throw new IllegalArgumentException(
                            "attributeModifier.operation must be one of " +
                                    "ADD | MULTIPLY | MULTIPLY_ALL_MODIFIERS"
                    );
            }

            String slotString = (String) map.get("slot");
            EquipmentSlot slot = null;
            if(slotString != null) slot = EquipmentSlot.valueOf(slotString.toUpperCase());

            AttributeModifier modifier = new AttributeModifier(
                    UUID.randomUUID(),
                    nameString.toUpperCase(),
                    amount,
                    operation,
                    slot
            );

            AttributeModifierWrapper wrapper = new AttributeModifierWrapper(
                    attribute,
                    modifier
            );

            wrappers.add(wrapper);
        }

        if(wrappers.size() > 0) return wrappers;
        return null;

    }

    private List<ItemEnchantment> toItemEnchantmentList(ArrayList<?> list) {

        if(list == null) {
            return null;
        }

        List<ItemEnchantment> itemEnchantments = new ArrayList<>();

        for(Object item : list) {

            if(!(item instanceof HashMap)) continue;
            HashMap<?,?> enchantmentMap = (HashMap<?,?>) item;

            Enchantment enchantment;
            String enchantmentType = ((String) enchantmentMap.get("type"));
            try {
                String[] enchantmentArray = enchantmentType.split(":");
                String nameSpace = enchantmentArray[0].toLowerCase();
                String nameKey = enchantmentArray[1].toLowerCase();
                enchantment = Registry.ENCHANTMENT.get(new NamespacedKey(nameSpace, nameKey));
            } catch (ArrayIndexOutOfBoundsException ex) {
                plugin.getLogger().warning("Incorrect enchantment: "+ enchantmentType);
                return null;
            }

            Integer level = (Integer) enchantmentMap.get("level");
            if(level == null) level = 1;

            Boolean ignoreLevelRestriction = (Boolean) enchantmentMap.get("ignoreLevelRestriction");
            if(ignoreLevelRestriction == null) ignoreLevelRestriction = false;

            ItemEnchantment itemEnchantment = new ItemEnchantment(
                    enchantment,
                    level,
                    ignoreLevelRestriction
            );

            itemEnchantments.add(itemEnchantment);

        }

        if(itemEnchantments.size() > 0) {
            return itemEnchantments;
        }

        return null;

    }
}
