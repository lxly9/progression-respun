package com.progression_respun.mixin.enchantment;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.block.EnchantingTableBlock;
import net.minecraft.block.entity.ChiseledBookshelfBlockEntity;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.BookItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
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

    @Inject(method = "method_17411", at = @At(value = "HEAD"), cancellable = true)
    private void progression_respun$getEnchantments(ItemStack itemStack, World world, BlockPos tablePos, CallbackInfo ci) {
        this.possibleEnchantments.clear();
        this.bookAmount = 0;
        for (BlockPos blockPos : EnchantingTableBlock.POWER_PROVIDER_OFFSETS) {
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
            if (possibleEnchantments.isEmpty()) ci.cancel();
        }
    }

    @WrapMethod(method = "onContentChanged")
    private void progressionrespun$checkForValidEnchants(Inventory inventory, Operation<Void> original) {
        ItemStack itemStack = inventory.getStack(0);
        context.run((world, tablePos) -> {
            original.call(inventory);
            this.possibleEnchantments.clear();
            this.usableEnchantments.clear();
            this.bookAmount = 0;
            for (BlockPos blockPos : EnchantingTableBlock.POWER_PROVIDER_OFFSETS) {
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
        if (usableEnchantments.isEmpty() || itemStack.getItem() instanceof BookItem) {
            original.call(new SimpleInventory(2));
        } else {
            original.call(inventory);
        }
    }


//    @WrapOperation(method = "method_17411", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/EnchantingTableBlock;canAccessPowerProvider(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/BlockPos;)Z"))
//    private boolean progression_respun$chiseledBookshelfProvidesPower(World world, BlockPos tablePos, BlockPos providerOffset, Operation<Boolean> original) {
//        if (!original.call(world, tablePos, providerOffset)) return false;
//        if (!(world.getBlockEntity(tablePos.add(providerOffset)) instanceof ChiseledBookshelfBlockEntity bookshelf)) return true;
//
//        int bookCount = 0;
//        for (int i = 0; i < bookshelf.size(); ++i) {
//            ItemStack itemStack = bookshelf.getStack(i);
//            if (!itemStack.isEmpty()) ++bookCount;
//        }
//        return bookCount >= 3;
//    }

    @Unique
    private int progressionrespun$getEnchantmentsPerButton(int buttonIndex) {
        return switch (buttonIndex) {
            case 1 -> 3;
            case 2 -> 5;
            default -> 1;
        };
    }

    @ModifyReturnValue(method = "generateEnchantments", at = @At("RETURN"))
    private List<EnchantmentLevelEntry> progression_respun$addEnchantments(List<EnchantmentLevelEntry> original, @Local(name = "stack", ordinal = 0, argsOnly = true) ItemStack stack, @Local(name = "buttonId", ordinal = 0, argsOnly = true) int buttonId) {
        if (this.possibleEnchantments.isEmpty()) return List.of();

        List<EnchantmentLevelEntry> entries = this.possibleEnchantments.stream().filter(e -> e.enchantment.value().isAcceptableItem(stack)).toList();
        int usable = entries.size();

        if (entries.isEmpty()) return List.of();
        if (buttonId == 1 && (enchantmentPower[1] <= 1 || usable <= 1)) {
            this.enchantmentPower[1] = 0;
            return List.of();
        }
        if (buttonId == 2 && (enchantmentPower[2] <= 2 || usable <= 2)) {
            this.enchantmentPower[2] = 0;
            return List.of();
        }

        int desiredCount = progressionrespun$getEnchantmentsPerButton(buttonId);
//        if (enchantmentPower.length < 2 && (buttonId == 1 || buttonId == 2)) desiredCount = desiredCount -1;
        List<EnchantmentLevelEntry> result = new ArrayList<>();

        List<EnchantmentLevelEntry> enchantmentLevelEntries = new ArrayList<>(entries);
        enchantmentLevelEntries.removeAll(result);

        while (result.size() < desiredCount && !enchantmentLevelEntries.isEmpty()) {
            int index = random.nextInt(enchantmentLevelEntries.size());
            result.add(enchantmentLevelEntries.get(index));
            enchantmentLevelEntries.remove(index);
        }
        return result;
    }
}