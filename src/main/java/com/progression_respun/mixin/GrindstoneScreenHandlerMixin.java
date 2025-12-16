package com.progression_respun.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.GrindstoneScreenHandler;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import static com.progression_respun.ProgressionRespun.LOGGER;

@Debug(export = true)
@Mixin(GrindstoneScreenHandler.class)
public abstract class GrindstoneScreenHandlerMixin {

    @Shadow
    protected abstract ItemStack grind(ItemStack item);

    @WrapOperation(method = "getOutputStack", at = @At(value = "INVOKE", target = "Lnet/minecraft/screen/GrindstoneScreenHandler;grind(Lnet/minecraft/item/ItemStack;)Lnet/minecraft/item/ItemStack;"))
    private ItemStack progressionrespun$damageWhileGrindingEnchants(GrindstoneScreenHandler instance, ItemStack item, Operation<ItemStack> original) {
        if (item != ItemStack.EMPTY && EnchantmentHelper.hasEnchantments(item)) {
            LOGGER.info("gayyy");
            ItemStack stack = item.copy();
            stack.setDamage(stack.getDamage() - stack.getMaxDamage()/4);
            original.call(instance, stack);
        }
        return original.call(instance, item);
    }
}
