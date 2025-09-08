package com.progression_respun.mixin;

import com.progression_respun.item.ModItems;
import net.minecraft.block.BlockState;
import net.minecraft.block.GrindstoneBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

import static com.progression_respun.data.ModItemTagProvider.*;

@Mixin(Item.class)
public class ItemMixin {
    @Inject(method = "appendTooltip", at = @At("HEAD"))
    private void UnderArmorTooltip(ItemStack stack, Item.TooltipContext context, List<Text> tooltip, TooltipType type, CallbackInfo ci) {
        if (stack.getItem() instanceof ArmorItem && !stack.isIn(UNDER_ARMOR)) {
            if (!(stack.getDamage() >= stack.getMaxDamage()) && !stack.isIn(BYPASSES_UNDER_ARMOR)) {
                tooltip.add(Text.translatable("tag.item.progression_respun.needs_under_armor").formatted(Formatting.GRAY));
            }
        }
    }
}
