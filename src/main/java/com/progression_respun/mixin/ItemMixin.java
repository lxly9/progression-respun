package com.progression_respun.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.progression_respun.ProgressionRespun;
import com.progression_respun.component.ModDataComponentTypes;
import com.progression_respun.data.ModItemTagProvider;
import net.minecraft.component.ComponentMap;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.item.*;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

import static com.progression_respun.data.ModItemTagProvider.*;

@Mixin(Item.class)
public class ItemMixin {


    @Shadow
    @Final
    @Mutable
    private ComponentMap components;

    @Shadow
    @Final
    private static Logger LOGGER;

    @Unique
    private int progressionrespun$getStackSizePerNutrition(int nutrition) {
        return switch (nutrition) {
            case 1,2,3 -> 64;
            case 4 -> 32;
            case 5,6 -> 16;
            case 7,8,9,10,11,12,13,14 -> 8;
            case 15,16,17,18,19,20 -> 4;
            default -> 64;
        };
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void progressionrespun$changeStackSize(Item.Settings settings, CallbackInfo ci) {
        Item item = (Item) (Object) this;
        int newStackSize = -1;
        int newMaxDamage = -1;

        if (item instanceof BedItem) newStackSize = 16;
        if (item instanceof PotionItem) newMaxDamage = 3;

        FoodComponent foodComponent = item.getComponents().get(DataComponentTypes.FOOD);
        if (foodComponent != null) {
            int nutrition = foodComponent.nutrition();
            newStackSize = progressionrespun$getStackSizePerNutrition(nutrition);
            if (nutrition > 2 && foodComponent.effects().size() > 1) {
                newStackSize = 1;
            }
        }

        if (newStackSize > 0) {
            ComponentMap override = ComponentMap.builder().add(DataComponentTypes.MAX_STACK_SIZE, newStackSize).build();
            components = ComponentMap.of(components, override);
        }

        if (newMaxDamage > 0) {
            ComponentMap override = ComponentMap.builder().add(DataComponentTypes.MAX_DAMAGE, newMaxDamage).add(DataComponentTypes.DAMAGE, 0).build();
            components = ComponentMap.of(components, override);
        }
    }

    @Inject(method = "appendTooltip", at = @At("HEAD"))
    private void progressionrespun$underArmorTooltip(ItemStack stack, Item.TooltipContext context, List<Text> tooltip, TooltipType type, CallbackInfo ci) {
        if (stack.getItem() instanceof ArmorItem && !stack.isIn(UNDER_ARMOR)) {
            if (!(stack.getDamage() >= stack.getMaxDamage()) && !stack.isIn(BYPASSES_UNDER_ARMOR)) {
                tooltip.add(Text.translatable("tag.item.progression_respun.needs_under_armor").formatted(Formatting.GRAY));
            }
        }
    }

    @ModifyReturnValue(method = "isEnchantable", at = @At("RETURN"))
    public boolean progressionrespun$isEnchantable(boolean original) {
        Item item = ((Item) (Object) this);
        if (item instanceof ArmorItem && item.getDefaultStack().isIn(UNDER_ARMOR)){
            var component = item.getComponents().get(ModDataComponentTypes.UNDER_ARMOR_CONTENTS);
            if (component != null && !component.isEmpty()) {
                ItemStack armorStack = component.get(0);
                return armorStack != ItemStack.EMPTY;
            }
        }
        return original;
    }
}
