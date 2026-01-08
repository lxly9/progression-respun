package com.progression_respun.mixin.fishing_rod;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.progression_respun.ProgressionRespun;
import com.progression_respun.component.ModDataComponentTypes;
import com.progression_respun.component.type.FishingBaitContentsComponent;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.TemptGoal;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.FishingRodItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import static com.progression_respun.ProgressionRespun.getBait;

@Mixin(PigEntity.class)
public class PigEntityMixin extends AnimalEntity {

    protected PigEntityMixin(EntityType<? extends AnimalEntity> entityType, World world) {
        super(entityType, world);
    }

    @WrapOperation(method = "getControllingPassenger", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;isHolding(Lnet/minecraft/item/Item;)Z"))
    private boolean progressionrespun$useCarrotOnARod(PlayerEntity instance, Item item, Operation<Boolean> original) {
        ItemStack stack = instance.getInventory().getMainHandStack();
        if (stack.getItem() instanceof FishingRodItem) {
            return getBait(stack).isOf(Items.CARROT);
        }
        return original.call(instance, item);
    }

    @Inject(method = "initGoals", at = @At("HEAD"))
    private void progressionrespun$followCarrotOnARod(CallbackInfo ci) {
        this.goalSelector.add(4, new TemptGoal(this, 1.2, stack -> getBait(stack).isOf(Items.CARROT), false));
    }

    @Override
    public boolean isBreedingItem(ItemStack stack) {
        return false;
    }

    @Override
    public @Nullable PassiveEntity createChild(ServerWorld world, PassiveEntity entity) {
        return null;
    }
}
