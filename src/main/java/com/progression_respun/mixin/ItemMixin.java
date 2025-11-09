package com.progression_respun.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.progression_respun.data.ModItemTagProvider;
import net.minecraft.component.ComponentMap;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

import static com.progression_respun.ProgressionRespun.getItemByName;
import static com.progression_respun.data.ModItemTagProvider.*;

@Mixin(Item.class)
public class ItemMixin {


    @Shadow
    @Final
    @Mutable
    private ComponentMap components;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void changeStackSize(Item.Settings settings, CallbackInfo ci) {
        Item item = (Item) (Object) this;
        int newStackSize = -1;
        int newMaxDamage = -1;

        if (item instanceof BedItem) {
            newStackSize = 16;
        }
        if (item instanceof PotionItem) {
            newMaxDamage = 3;
        }

        if (newStackSize > 0) {
            ComponentMap override = ComponentMap.builder()
                    .add(DataComponentTypes.MAX_STACK_SIZE, newStackSize)
                    .build();

            components = ComponentMap.of(components, override);
        }

        if (newMaxDamage > 0) {
            ComponentMap override = ComponentMap.builder()
                    .add(DataComponentTypes.MAX_DAMAGE, newMaxDamage)
                    .add(DataComponentTypes.DAMAGE, 0)
                    .build();

            components = ComponentMap.of(components, override);
        }
    }

    @Inject(method = "appendTooltip", at = @At("HEAD"))
    private void UnderArmorTooltip(ItemStack stack, Item.TooltipContext context, List<Text> tooltip, TooltipType type, CallbackInfo ci) {
        if (stack.getItem() instanceof ArmorItem && !stack.isIn(UNDER_ARMOR)) {
            if (!(stack.getDamage() >= stack.getMaxDamage()) && !stack.isIn(BYPASSES_UNDER_ARMOR)) {
                tooltip.add(Text.translatable("tag.item.progression_respun.needs_under_armor").formatted(Formatting.GRAY));
            }
        }
    }

    @ModifyReturnValue(method = "isEnchantable", at = @At("RETURN"))
    public boolean isEnchantable(boolean original) {
        ItemStack item = ((Item) (Object) this).getDefaultStack();
        return !item.isIn(ModItemTagProvider.UNDER_ARMOR);
    }
}
