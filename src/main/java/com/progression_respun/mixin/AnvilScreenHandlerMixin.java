package com.progression_respun.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.progression_respun.component.ModDataComponentTypes;
import com.progression_respun.component.type.UnderArmorContentsComponent;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.*;
import net.minecraft.recipe.Ingredient;
import net.minecraft.screen.*;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

import static com.progression_respun.ProgressionRespun.*;

@Mixin(AnvilScreenHandler.class)
public abstract class AnvilScreenHandlerMixin extends ForgingScreenHandler {
    @Unique private final Property isRepairing = Property.create();

    @Shadow @Final private Property levelCost;

    @Shadow
    @Final
    private static Logger LOGGER;

    @Shadow
    private int repairItemUsage;

    public AnvilScreenHandlerMixin(@Nullable ScreenHandlerType<?> type, int syncId, PlayerInventory playerInventory, ScreenHandlerContext context) {
        super(type, syncId, playerInventory, context);
    }

    @Inject(method = "<init>(ILnet/minecraft/entity/player/PlayerInventory;Lnet/minecraft/screen/ScreenHandlerContext;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/screen/AnvilScreenHandler;addProperty(Lnet/minecraft/screen/Property;)Lnet/minecraft/screen/Property;"))
    private void progressionrespun$addRepairProperty(int syncId, PlayerInventory inventory, ScreenHandlerContext context, CallbackInfo ci) {
        addProperty(isRepairing);
    }

    @Inject(method = "canTakeOutput", at = @At("HEAD"), cancellable = true)
    private void progressionrespun$canTakeFreeRepair(PlayerEntity player, boolean present, CallbackInfoReturnable<Boolean> cir) {
        if (isRepairing.get() != 0) cir.setReturnValue(true);
    }

    @ModifyConstant(method = "method_24922", constant = @Constant(floatValue = 0.12f))
    private static float progressionrespun$reduceBreakChance(float constant) {
        return 0.1f;
    }

    @Inject(method = "updateResult", at = @At(value = "HEAD"))
    private void progressionrespun$resetRepair(CallbackInfo ci) {
        isRepairing.set(0);
    }

    @Inject(method = "updateResult", at = @At(value = "FIELD", target = "Lnet/minecraft/screen/AnvilScreenHandler;repairItemUsage:I", ordinal = 1, shift = At.Shift.AFTER), cancellable = true)
    private void progressionrespun$freeRepairs(CallbackInfo ci, @Local(ordinal = 1) ItemStack itemStack2) {
        isRepairing.set(1);
        levelCost.set(0);
        output.setStack(0, itemStack2);
        this.sendContentUpdates();
        ci.cancel();
    }

    @Inject(method = "updateResult", at = @At(value = "HEAD"), cancellable = true)
    private void progressionrespun$mendingRepairs(CallbackInfo ci) {
        ItemStack itemStack = this.input.getStack(0);
        ItemStack itemStack2 = itemStack.copy();
        ItemStack itemStack3 = this.input.getStack(1);
        ItemStack armorStack = getArmor(itemStack2).copy();
        int i = 0;
        boolean armor = armorStack != ItemStack.EMPTY;
        ToolMaterial material;
        ArmorMaterial armorMaterial;
        ItemStack nugget = ItemStack.EMPTY;
        ItemStack armorNugget = ItemStack.EMPTY;

        int k = Math.min(itemStack2.getDamage(), itemStack2.getMaxDamage() / 4);
        int j = Math.min(armorStack.getDamage(), armorStack.getMaxDamage() / 4);

        if (!armor){
            if (k <= 0) {
                this.output.setStack(0, ItemStack.EMPTY);
                this.levelCost.set(0);
                ci.cancel();
                return;
            }
        } else {
            if (j <= 0) {
                this.output.setStack(0, ItemStack.EMPTY);
                this.levelCost.set(0);
                ci.cancel();
                return;
            } else if (k <= 0) {
                this.output.setStack(0, ItemStack.EMPTY);
                this.levelCost.set(0);
                ci.cancel();
                return;
            }
        }

        if (!armor && hasMending(itemStack2)) {
            if (itemStack2.getItem() instanceof ToolItem toolItem) {
                material = toolItem.getMaterial();
                nugget = getNugget(material.getRepairIngredient());
            }
            if (itemStack2.getItem() instanceof ArmorItem UnderArmorItem) {
                armorMaterial = UnderArmorItem.getMaterial().value();
                nugget = getNugget(armorMaterial.repairIngredient().get());
            }
            if (itemStack3.isOf(nugget.getItem()) && nugget != ItemStack.EMPTY) {
                int m;

                for (m = 0; k > 0 && m < itemStack3.getCount(); m++) {
                    int n = itemStack2.getDamage() - k;
                    itemStack2.setDamage(n);
                    i++;
                    k = Math.min(itemStack2.getDamage(), itemStack2.getMaxDamage() / 4);
                }

                repairItemUsage = m;
                isRepairing.set(1);
                levelCost.set(0);
                output.setStack(0, itemStack2);
                this.sendContentUpdates();
                ci.cancel();
                return;
            }
        } else if (armor) {
            if (armorStack.getItem() instanceof ArmorItem armorItem) {
                armorMaterial = armorItem.getMaterial().value();
                armorNugget = getNugget(armorMaterial.repairIngredient().get());
                if (hasMending(armorStack) && itemStack3.isOf(armorNugget.getItem()) && armorNugget != ItemStack.EMPTY) {
                    int m;

                    for (m = 0; j > 0 && m < itemStack3.getCount(); m++) {
                        int n = armorStack.getDamage() - j;
                        armorStack.setDamage(n);
                        i++;
                        j = Math.min(armorStack.getDamage(), armorStack.getMaxDamage() / 4);
                    }

                    itemStack2.set(ModDataComponentTypes.UNDER_ARMOR_CONTENTS, new UnderArmorContentsComponent(List.of(armorStack)));
                    repairItemUsage = m;
                    isRepairing.set(1);
                    levelCost.set(0);
                    output.setStack(0, itemStack2);
                    this.sendContentUpdates();
                    ci.cancel();
                    return;
                }
                if (armorMaterial.repairIngredient().get().test(itemStack3)){
                    int m;

                    for (m = 0; j > 0 && m < itemStack3.getCount(); m++) {
                        int n = armorStack.getDamage() - j;
                        armorStack.setDamage(n);
                        i++;
                        j = Math.min(armorStack.getDamage(), armorStack.getMaxDamage() / 4);
                    }

                    itemStack2.set(ModDataComponentTypes.UNDER_ARMOR_CONTENTS, new UnderArmorContentsComponent(List.of(armorStack)));
                    repairItemUsage = m;
                    isRepairing.set(1);
                    levelCost.set(0);
                    output.setStack(0, itemStack2);
                    this.sendContentUpdates();
                    ci.cancel();
                    return;
                }
            }
        }
    }

    @Inject(method = "updateResult", at = @At("HEAD"), cancellable = true)
    private void progressionrespun$disableCombining(CallbackInfo ci) {
        ItemStack stack2 = this.input.getStack(1);

        if (!stack2.isEmpty()) {
            if (stack2.contains(DataComponentTypes.STORED_ENCHANTMENTS)) {
                ItemEnchantmentsComponent enchantmentsComponent = stack2.getComponents().get(DataComponentTypes.STORED_ENCHANTMENTS);
                if (enchantmentsComponent != null && !enchantmentsComponent.isEmpty()) {
                    this.output.setStack(0, ItemStack.EMPTY);
                    this.levelCost.set(0);
                    this.repairItemUsage = 0;
                    ci.cancel();
                }
            } else if (stack2.contains(DataComponentTypes.ENCHANTMENTS)) {
                ItemEnchantmentsComponent enchantmentsComponent = stack2.getComponents().get(DataComponentTypes.ENCHANTMENTS);
                if (enchantmentsComponent != null && !enchantmentsComponent.isEmpty()) {
                    this.output.setStack(0, ItemStack.EMPTY);
                    this.levelCost.set(0);
                    this.repairItemUsage = 0;
                    ci.cancel();
                }
            }
        }
    }

    @Unique
    private ItemStack getNugget(Ingredient ingredient) {
        ItemStack stack = ingredientToStack(ingredient);

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

    @Unique
    private static ItemStack ingredientToStack(Ingredient ingredient) {
        ItemStack[] stacks = ingredient.getMatchingStacks();
        if (stacks.length == 0) return ItemStack.EMPTY;
        return stacks[0].copy();
    }
}
