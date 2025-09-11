package com.progression_respun.mixin;

import com.progression_respun.item.ModItems;
import net.minecraft.component.ComponentMap;
import net.minecraft.component.DataComponentTypes;
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
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

import static com.progression_respun.data.ModItemTagProvider.*;

@Mixin(Item.class)
public class ItemMixin {


    @Shadow
    @Final
    @Mutable
    private ComponentMap components;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void makeBoatsAndMinecartsStackable(Item.Settings settings, CallbackInfo ci) {
        Item self = (Item) (Object) this;
        int newStackSize = -1;

        if (self instanceof BedItem) {
            newStackSize = 16;
        }

        if (newStackSize > 0) {
            ComponentMap override = ComponentMap.builder()
                    .add(DataComponentTypes.MAX_STACK_SIZE, newStackSize)
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
    @Inject(method = "useOnBlock", at = @At("HEAD"), cancellable = true)
    private void polishDiamonds(ItemUsageContext context, CallbackInfoReturnable<ActionResult> cir) {
        PlayerEntity player = context.getPlayer();
        World world = context.getWorld();
        ItemStack stack = player.getMainHandStack();
        int stackSize = 1;
        if (stack.isOf(Items.DIAMOND)) {
            if (!world.isClient) {
                ServerWorld serverWorld = (ServerWorld) world;
                if (player.isSneaking() && stack.getCount() > 1) {
                    stackSize = stack.getCount();
                }
                stack.decrement(stackSize);
                ItemStack newStack = new ItemStack(ModItems.POLISHED_DIAMOND, stackSize);
                player.getInventory().insertStack(newStack);
                int totalXp = 0;
                for (int i = 0; i < stackSize; i++) {
                    totalXp += serverWorld.getRandom().nextBetween(1, 5);
                }
                ExperienceOrbEntity.spawn(serverWorld, context.getHitPos(), totalXp);
                world.playSound(null, context.getBlockPos(), SoundEvents.BLOCK_GRINDSTONE_USE, SoundCategory.BLOCKS);
                cir.setReturnValue(ActionResult.SUCCESS);
                cir.cancel();
            } else {
                cir.setReturnValue(ActionResult.CONSUME);
                cir.cancel();
            }
        }
    }
}
