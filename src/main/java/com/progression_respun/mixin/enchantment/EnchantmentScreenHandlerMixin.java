package com.progression_respun.mixin.enchantment;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.sugar.Local;
import com.progression_respun.block.ModBlockTags;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalEnchantmentTags;
import net.minecraft.block.entity.ChiseledBookshelfBlockEntity;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.BookItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.EnchantmentTags;
import net.minecraft.screen.EnchantmentScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.progression_respun.ProgressionRespun.LOGGER;
import static com.progression_respun.ProgressionRespun.POWER_PROVIDER_OFFSETS;

@Debug(export = true)
@Mixin(EnchantmentScreenHandler.class)
public class EnchantmentScreenHandlerMixin {
    @Shadow @Final private Random random;
    @Mutable
    @Shadow
    @Final
    public int[] enchantmentPower;
    @Shadow
    @Final
    private ScreenHandlerContext context;
    @Unique
    private final List<EnchantmentLevelEntry> possibleEnchantments = new ArrayList<>();
    @Unique
    private final List<EnchantmentLevelEntry> usableEnchantments = new ArrayList<>();
    @Unique
    private int bookAmount = 0;
    @Unique
    private float curseChance = 0.5f;

    @Inject(method = "method_17411", at = @At(value = "HEAD"))
    private void progression_respun$getEnchantments(ItemStack itemStack, World world, BlockPos tablePos, CallbackInfo ci) {
        this.possibleEnchantments.clear();
        this.bookAmount = 0;
        this.curseChance = 0.5f;
        for (BlockPos blockPos : POWER_PROVIDER_OFFSETS) {
            if (world.getBlockState(blockPos).isIn(ModBlockTags.DECREASES_CURSE)) {
                curseChance = curseChance - 0.1f;
            }
            if (world.getBlockState(blockPos).isIn(ModBlockTags.INCREASES_CURSE)) {
                curseChance = curseChance + 0.1f;
                LOGGER.info(String.valueOf(curseChance));
            }
            if (!(world.getBlockEntity(tablePos.add(blockPos)) instanceof ChiseledBookshelfBlockEntity bookshelf)) {
                continue;
            }
            for (int i = 0; i < bookshelf.size(); i++) {
                if (bookshelf.getStack(i).isOf(Items.ENCHANTED_BOOK)) {
                    ++this.bookAmount;
                    Set<EnchantmentLevelEntry> possibleEnchantments = EnchantmentHelper.getEnchantments(bookshelf.getStack(i)).getEnchantmentEntries().stream().map(entry -> new EnchantmentLevelEntry(entry.getKey(), entry.getIntValue())).collect(Collectors.toSet());
                    this.possibleEnchantments.addAll(possibleEnchantments);
                }
            }
        }
    }

    @WrapMethod(method = "onContentChanged")
    private void progressionrespun$checkForValidEnchants(Inventory inventory, Operation<Void> original) {
        ItemStack itemStack = inventory.getStack(0);
        context.run((world, tablePos) -> {
            this.possibleEnchantments.clear();
            this.usableEnchantments.clear();
            this.bookAmount = 0;
            for (BlockPos blockPos : POWER_PROVIDER_OFFSETS) {
                if (!(world.getBlockEntity(tablePos.add(blockPos)) instanceof ChiseledBookshelfBlockEntity bookshelf)) {
                    continue;
                }
                for (int i = 0; i < bookshelf.size(); i++) {
                    if (bookshelf.getStack(i).isOf(Items.ENCHANTED_BOOK)) {
                        ++this.bookAmount;
                        Set<EnchantmentLevelEntry> possibleEnchantments = EnchantmentHelper.getEnchantments(bookshelf.getStack(i)).getEnchantmentEntries().stream().map(entry -> new EnchantmentLevelEntry(entry.getKey(), entry.getIntValue())).collect(Collectors.toSet());
                        this.possibleEnchantments.addAll(possibleEnchantments);
                        List<EnchantmentLevelEntry> entries = this.possibleEnchantments.stream().filter(e -> e.enchantment.value().isAcceptableItem(itemStack)).toList();
                        this.usableEnchantments.addAll(entries);
                    }
                }
            }
        });
        if (usableEnchantments.isEmpty() && (itemStack.isEmpty() || itemStack.getItem() instanceof BookItem)) {
            this.enchantmentPower[0] = 0;
            this.enchantmentPower[1] = 0;
            this.enchantmentPower[2] = 0;
            original.call(new SimpleInventory(2));
        } else {
            original.call(inventory);
        }
    }

    @Unique
    private int progressionrespun$getEnchantmentsPerButton(int buttonIndex) {
        return switch (buttonIndex) {
            case 1 -> 3;
            case 2 -> 5;
            default -> 1;
        };
    }

    @ModifyReturnValue(method = "generateEnchantments", at = @At("RETURN"))
    private List<EnchantmentLevelEntry> progression_respun$addEnchantments(List<EnchantmentLevelEntry> original, @Local(name = "stack", ordinal = 0, argsOnly = true) ItemStack stack, @Local(name = "buttonId", ordinal = 0, argsOnly = true) int slot, @Local(argsOnly = true) DynamicRegistryManager registryManager) {
        if (this.usableEnchantments.isEmpty()) {
            this.enchantmentPower[0] = 0;
            this.enchantmentPower[1] = 0;
            this.enchantmentPower[2] = 0;
            return List.of();
        }

        List<EnchantmentLevelEntry> entries = this.possibleEnchantments.stream().filter(e -> e.enchantment.value().isAcceptableItem(stack)).toList();
        int usable = entries.size();

        if (entries.isEmpty()) return List.of();
        if (slot == 0) {
            this.enchantmentPower[0] = 7;
        }
        if (slot == 1 && usable <= 1) {
            this.enchantmentPower[1] = 0;
            return List.of();
        } else if (slot == 1) {
            this.enchantmentPower[1] = 15;
        }
        if (slot == 2 && entries.size() <= 2) {
            this.enchantmentPower[2] = 0;
            return List.of();
        } else if (slot == 2) {
            this.enchantmentPower[2] = 30;
        }

        int desiredCount = progressionrespun$getEnchantmentsPerButton(slot);
        List<EnchantmentLevelEntry> result = new ArrayList<>();

        List<EnchantmentLevelEntry> enchantmentLevelEntries = new ArrayList<>(entries);
        enchantmentLevelEntries.removeAll(result);
        java.util.Collections.shuffle(enchantmentLevelEntries, new java.util.Random(this.random.nextLong()));

        while (result.size() < desiredCount && !enchantmentLevelEntries.isEmpty()) {
            java.util.Collections.shuffle(enchantmentLevelEntries, new java.util.Random(this.random.nextLong()));
            EnchantmentLevelEntry entry = enchantmentLevelEntries.getFirst();
            result.add(entry);
//            if (random.nextFloat() > curseChance) {
//                result.add(entry);
//            } else {
//                Registry<Enchantment> entryList = registryManager.get(EnchantmentTags.CURSE.registry());
//                RegistryEntry<Enchantment> curse = entryList.getRandom(random).get();
//                result.add(new EnchantmentLevelEntry(curse, 1));
//                LOGGER.info(String.valueOf(curse));
//                LOGGER.info(String.valueOf(curseChance));
//            }
            enchantmentLevelEntries.remove(entry);
            EnchantmentHelper.removeConflicts(enchantmentLevelEntries, entry);
        }
        return result;
    }
}