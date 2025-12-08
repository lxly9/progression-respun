package com.progression_respun.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.block.EnchantingTableBlock;
import net.minecraft.block.entity.ChiseledBookshelfBlockEntity;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.EnchantmentScreenHandler;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
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
    @Unique
    private final List<EnchantmentLevelEntry> possibleEnchantments = new ArrayList<>();
    @Unique
    private int bookAmount = 0;

    @Inject(method = "method_17411", at = @At(value = "HEAD"))
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
        }
    }

    @WrapOperation(method = "method_17411", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/EnchantingTableBlock;canAccessPowerProvider(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/BlockPos;)Z"))
    private boolean progression_respun$chiseledBookshelfProvidesPower(World world, BlockPos tablePos, BlockPos providerOffset, Operation<Boolean> original) {
        if (!original.call(world, tablePos, providerOffset)) return false;
        if (!(world.getBlockEntity(tablePos.add(providerOffset)) instanceof ChiseledBookshelfBlockEntity bookshelf)) return true;

        int bookCount = 0;
        for (int i = 0; i < bookshelf.size(); ++i) {
            ItemStack itemStack = bookshelf.getStack(i);
            if (!itemStack.isEmpty()) ++bookCount;
        }
        return bookCount >= 0;
    }

    @Unique
    private int getEnchantmentsPerButton(int buttonIndex) {
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

        if (entries.isEmpty()) return List.of();

        int desiredCount = getEnchantmentsPerButton(buttonId);
        List<EnchantmentLevelEntry> result = new ArrayList<>(original);

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