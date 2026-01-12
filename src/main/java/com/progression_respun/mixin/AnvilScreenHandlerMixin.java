package com.progression_respun.mixin;

import com.llamalad7.mixinextras.sugar.Local;
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

import static com.progression_respun.ProgressionRespun.*;

@Debug()
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

    @Inject(method = "updateResult", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;contains(Lnet/minecraft/component/ComponentType;)Z", shift = At.Shift.AFTER), cancellable = true)
    private void progressionrespun$mendingRepairs(CallbackInfo ci, @Local(name = "itemStack") ItemStack itemStack, @Local(name = "itemStack2") ItemStack itemStack2, @Local(name = "itemStack3") ItemStack itemStack3, @Local(name = "i") int i) {
        ItemStack armorStack = getArmor(itemStack2);
        boolean armor = armorStack != ItemStack.EMPTY;
        ToolMaterial material;
        ArmorMaterial armorMaterial;
        ItemStack nugget = ItemStack.EMPTY;
        ItemStack armorNugget = ItemStack.EMPTY;

        if (itemStack2.getItem() instanceof ToolItem toolItem) {
            material = toolItem.getMaterial();
            nugget = getNugget(material.getRepairIngredient());
        }
        if (itemStack2.getItem() instanceof ArmorItem UnderArmorItem) {
            armorMaterial = UnderArmorItem.getMaterial().value();
            nugget = getNugget(armorMaterial.repairIngredient().get());
            if (armor && armorStack.getItem() instanceof ArmorItem armorItem) {
                armorMaterial = armorItem.getMaterial().value();
                armorNugget = getNugget(armorMaterial.repairIngredient().get());
            }
        }


        boolean nuggetValue = itemStack3.isOf(nugget.getItem()) && nugget != ItemStack.EMPTY;
        boolean armorNuggetValue = itemStack3.isOf(armorNugget.getItem()) && armorNugget != ItemStack.EMPTY;

        boolean hasMending = itemStack2.contains(DataComponentTypes.ENCHANTMENTS) && hasMending(itemStack2);
        boolean hasArmorMending = armor && armorStack.contains(DataComponentTypes.ENCHANTMENTS) && hasMending(armorStack);

        int k = Math.min(itemStack2.getDamage(), itemStack2.getMaxDamage() / 4);
        int j = Math.min(armorStack.getDamage(), armorStack.getMaxDamage() / 4);

        if (k <= 0 && !armor) {
            this.output.setStack(0, ItemStack.EMPTY);
            this.levelCost.set(0);
            return;
        }

        if (j <= 0 && k <= 0) {
            this.output.setStack(0, ItemStack.EMPTY);
            this.levelCost.set(0);
            return;
        }

        int m;
        if (hasMending && nuggetValue && !armor){

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
        }

        if (hasArmorMending && armorNuggetValue){
            LOGGER.info(String.valueOf(armorStack));

            for (m = 0; j > 0 && m < itemStack3.getCount(); m++) {
                int n = armorStack.getDamage() - j;
                armorStack.setDamage(n);
                i++;
                j = Math.min(armorStack.getDamage(), armorStack.getMaxDamage() / 4);
            }

            repairItemUsage = m;
            isRepairing.set(1);
            levelCost.set(0);
            output.setStack(0, itemStack2);
            this.sendContentUpdates();
            ci.cancel();
        }
        if (armor && itemStack3.getItem().canRepair(armorStack, itemStack3)){
            for (m = 0; j > 0 && m < itemStack3.getCount(); m++) {
                int n = armorStack.getDamage() - j;
                armorStack.setDamage(n);
                i++;
                j = Math.min(armorStack.getDamage(), armorStack.getMaxDamage() / 4);
            }

            repairItemUsage = m;
            isRepairing.set(1);
            levelCost.set(0);
            output.setStack(0, itemStack2);
            this.sendContentUpdates();
            ci.cancel();
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
