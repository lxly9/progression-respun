package com.gayasslily.progression_respun;

import com.gayasslily.progression_respun.block.ModBlocks;
import com.gayasslily.progression_respun.block.entity.ModBlockEntities;
import com.gayasslily.progression_respun.compat.CompatMods;
import com.gayasslily.progression_respun.component.ModDataComponentTypes;
import com.gayasslily.progression_respun.entity.attribute.ModEntityAttributes;
import com.gayasslily.progression_respun.item.ModItems;
import com.gayasslily.progression_respun.recipe.ModRecipes;
import com.gayasslily.progression_respun.util.*;
import com.gayasslily.progression_respun.worldgen.ModFeatures;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.Oxidizable;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.*;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Unique;

import java.util.List;
import java.util.Optional;

import static com.gayasslily.progression_respun.data.ModItemTagProvider.UNDER_ARMOR;
import static net.minecraft.state.property.Properties.LIT;
import static net.minecraft.state.property.Properties.WATERLOGGED;


public class ProgressionRespun implements ModInitializer {
	public static final String MOD_ID = "progression_respun";
	public static final String MC_ID = "minecraft";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static boolean isModLoaded;

	@Override
	public void onInitialize() {
		ModBlocks.initialize();
		ModBlockEntities.register();
		ModItems.initialize();
		ModFeatures.initialize();
		CompatMods.initialize();
        ModEntityAttributes.initialize();
		ModRecipes.register();
		ModBlocks.registerModBlocks();
		MobUtil.changeMobAttributes();
		MobUtil.despawnMobsOnWakeup();
		PlayerUtil.oneHitToOneHp();
		RecipeUtil.registerRecipeDisabler();
		RecipeUtil.grindingRecipe();
		ModDataComponentTypes.registerModDataComponentTypes();
		ArmorUtil.registerComponent();
        ComponentUtil.registerComponents();
//        LootTableUtil.replaceIngots();
		registerResourcePacks();
	}

	public static Identifier id(String name) {
		return Identifier.of(MOD_ID, name);
	}

    public static boolean isModLoaded(String modId) {
        return FabricLoader.getInstance().isModLoaded(modId);
    }

    public static Item getExternalItem(String modid, String path) {
        return Registries.ITEM.getOrEmpty(Identifier.of(modid, path)).orElse(null);
    }

	public static void registerResourcePacks() {
		ModContainer modContainer = FabricLoader.getInstance()
				.getModContainer("progression_respun")
				.orElseThrow(() -> new IllegalStateException("Missing mod modContainer"));

		ResourceManagerHelper.registerBuiltinResourcePack(
				Identifier.of(MOD_ID, "orngstone_copper"), modContainer,
				Text.translatable("pack.progression_respun.orngstone_copper"),
				ResourcePackActivationType.NORMAL
		);
		ResourceManagerHelper.registerBuiltinResourcePack(
				Identifier.of(MOD_ID, "redstone_copper"), modContainer,
				Text.translatable("pack.progression_respun.redstone_copper"),
				ResourcePackActivationType.ALWAYS_ENABLED
		);
		ResourceManagerHelper.registerBuiltinResourcePack(
				Identifier.of(MOD_ID, "progression_respun_compat"), modContainer,
				Text.translatable("pack.progression_respun.redstone_copper"),
				ResourcePackActivationType.DEFAULT_ENABLED
		);
	}

    public static Block getBlockByName(String name) {
        for (Block block : Registries.BLOCK) {
            Identifier id = Registries.BLOCK.getId(block);
            if (id.getPath().equals(name)) return block;
        }
        return Blocks.AIR;
    }

	public static Item getItemByName(String name) {
		for (Item item : Registries.ITEM) {
			Identifier id = Registries.ITEM.getId(item);
			if (id.getPath().equals(name)) {
				return item;
			}
		}
		return Items.AIR;
	}

    public static ItemStack getArmor(ItemStack stack) {
        if (stack != null) {
            if (stack.getItem() instanceof ArmorItem && stack.isIn(UNDER_ARMOR)) {
                var component = stack.get(ModDataComponentTypes.UNDER_ARMOR_CONTENTS);
                if (component != null && !component.isEmpty()) {
                    return component.get(0);
                }
            }
        }
        return ItemStack.EMPTY;
    }

    public static ItemStack getBait(ItemStack stack) {
        if (stack.getItem() instanceof FishingRodItem) {
            var component = stack.get(ModDataComponentTypes.FISHING_BAIT);
            if (component != null && !component.isEmpty()) {
                return component.get(0);
            }
        }
        return ItemStack.EMPTY;
    }

    public static boolean hasMending(ItemStack stack) {
        ItemEnchantmentsComponent enchants = stack.get(DataComponentTypes.ENCHANTMENTS);
        if (enchants != null) return enchants.getEnchantments().stream().anyMatch(entry -> entry.matchesKey(Enchantments.MENDING));
        return false;
    }

    public static boolean hasEnchant(ItemStack stack, RegistryKey<Enchantment> enchantment) {
        ItemEnchantmentsComponent enchants = stack.get(DataComponentTypes.ENCHANTMENTS);
        if (enchants != null) return enchants.getEnchantments().stream().anyMatch(entry -> entry.matchesKey(enchantment));
        return false;
    }

    public static ItemStack getNugget(ItemStack stack) {

        if (!stack.isEmpty()){
            String[] ingot = stack.toString().split(":");
            String material = ingot[1].replace("_ingot", "");
            Item nugget = getItemByName(material + "_nugget");
            Item shard = getItemByName(material + "_shard");
            if (ingot[1].equals("netherite_ingot")) return Items.NETHERITE_SCRAP.getDefaultStack();
            if (nugget != Items.AIR) return nugget.getDefaultStack();
            if (shard != Items.AIR) return shard.getDefaultStack();

            return ItemStack.EMPTY;
        }
        return ItemStack.EMPTY;
    }

    public static ItemStack ingredientToStack(Ingredient ingredient) {
        ItemStack[] stacks = ingredient.getMatchingStacks();
        if (stacks.length == 0) return ItemStack.EMPTY;
        return stacks[0].copy();
    }

    public static final List<BlockPos> POWER_PROVIDER_OFFSETS = BlockPos.stream(-3, 0, -3, 3, 2, 3).map(BlockPos::toImmutable).toList();

    @Unique
    public static int getLuminance(BlockState state) {
        return !state.get(WATERLOGGED) ? 14 : 0;
    }
    @Unique
    public static int getSoulLuminance(BlockState state) {
        return !state.get(WATERLOGGED) ? 10 : 0;
    }

    public static int getDelayForOxidization(Oxidizable.OxidationLevel oxidationLevel) {
        return switch (oxidationLevel) {
            case UNAFFECTED -> 4;
            case EXPOSED -> 20;
            case WEATHERED -> 60;
            case OXIDIZED -> 100;
        };
    }

    public static double progressionrespun$getMultiplierForWeight(LivingEntity entity, double base) {
        double weight = entity.getAttributeValue(ModEntityAttributes.GENERIC_WEIGHT);
        return base;
    }

    public static Block progressionrespun$getOxidizedPiston(Block original, Block piston) {
        if (piston instanceof Oxidizable oxidizable && original instanceof Oxidizable) {
            int degradation = oxidizable.getDegradationLevel().ordinal();
            String blockIdString = Registries.BLOCK.getId(original).getPath();
            switch (degradation) {
                case 1 -> original = getBlockByName("exposed_" + blockIdString);
                case 2 -> original = getBlockByName("weathered_" + blockIdString);
                case 3 -> original = getBlockByName("oxidized_" + blockIdString);
            }
        }
        return original;
    }

    public static Block progressionrespun$getWaxedOxidizedPiston(Block original, Block piston) {
        Optional<BlockState> optional = Optional.ofNullable(HoneycombItem.WAXED_TO_UNWAXED_BLOCKS.get().get(piston)).map(Block::getDefaultState);
        if (optional.isPresent()) {
            if (optional.get().getBlock() instanceof Oxidizable oxidizable){
                Block newOriginal = progressionrespun$getOxidizedPiston(original, optional.get().getBlock());
                String fullPath = Registries.BLOCK.getId(newOriginal).getPath();
                Block unwaxedVariant = getBlockByName(fullPath);
                Block waxedVariant = HoneycombItem.UNWAXED_TO_WAXED_BLOCKS.get().get(unwaxedVariant);
                return waxedVariant != null ? waxedVariant : original;
            }
        }
        return original;
    }
}