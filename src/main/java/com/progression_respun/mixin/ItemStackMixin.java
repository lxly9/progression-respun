package com.progression_respun.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.sugar.Local;
import com.progression_respun.ProgressionRespun;
import com.progression_respun.component.ModDataComponentTypes;
import com.progression_respun.component.type.UnderArmorContentsComponent;
import net.fabricmc.fabric.api.item.v1.FabricItemStack;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.component.ComponentHolder;
import net.minecraft.component.ComponentMap;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static com.progression_respun.block.ModBlockTags.BURNABLE_COBWEBS;
import static com.progression_respun.data.ModItemTagProvider.CAN_BURN_COBWEBS;
import static com.progression_respun.data.ModItemTagProvider.UNDER_ARMOR;
import static net.minecraft.data.DataProvider.LOGGER;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin implements ComponentHolder, FabricItemStack {
    @Unique private static final Text BROKEN_TEXT = Text.translatable(Util.createTranslationKey(
            "item", ProgressionRespun.id("tooltip.broken"))).formatted(Formatting.RED);

    @Shadow public abstract boolean isDamageable();
    @Shadow public abstract int getDamage();
    @Shadow public abstract int getMaxDamage();
    @Shadow public abstract Item getItem();
    @Shadow public abstract void decrement(int amount);
    @Shadow public abstract ComponentMap getComponents();

    @Shadow public abstract boolean isIn(TagKey<Item> tag);

    @Shadow public abstract void applyAttributeModifier(AttributeModifierSlot slot, BiConsumer<RegistryEntry<EntityAttribute>, EntityAttributeModifier> attributeModifierConsumer);

    @Shadow protected abstract void appendAttributeModifierTooltip(Consumer<Text> textConsumer, @Nullable PlayerEntity player, RegistryEntry<EntityAttribute> attribute, EntityAttributeModifier modifier);

    @Unique
    private boolean isBroken() {
        return isDamageable() && getDamage() >= getMaxDamage();
    }

    @Inject(method = "damage(ILnet/minecraft/server/world/ServerWorld;Lnet/minecraft/server/network/ServerPlayerEntity;Ljava/util/function/Consumer;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;decrement(I)V", shift = At.Shift.BEFORE), cancellable = true)
    private void damageRestrictDecrement(int amount, ServerWorld world, @Nullable ServerPlayerEntity player, Consumer<Item> breakCallback, CallbackInfo ci, @Local(ordinal = 1) int i) {
        Item item = getItem();
        ItemStack stack = item.getDefaultStack();
        boolean noDestroy = item instanceof ToolItem || item instanceof ArmorItem || item instanceof ShieldItem;

        if (!noDestroy) {
            decrement(amount);
        }
        if (!noDestroy || i - amount < this.getMaxDamage()) {
            breakCallback.accept(item);
        }
        ci.cancel();

    }

    @Inject(method = "getTooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/Item;appendTooltip(Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/Item$TooltipContext;Ljava/util/List;Lnet/minecraft/item/tooltip/TooltipType;)V", ordinal = 0, shift = At.Shift.BEFORE))
    private void getBrokenTooltip(Item.TooltipContext context, @Nullable PlayerEntity player, TooltipType type,
                                  CallbackInfoReturnable<List<Text>> cir, @Local Consumer<Text> tooltip) {
        if (isBroken()) {
            tooltip.accept(BROKEN_TEXT);
        }
    }

    @ModifyReturnValue(method = "getName", at = @At("RETURN"))
    private Text MaterialWithArmorName(Text original) {
        ItemStack stack = (ItemStack)(Object) this;
        if (stack.getItem() instanceof ArmorItem underArmorItem && stack.isIn(UNDER_ARMOR)) {
            UnderArmorContentsComponent component = stack.get(ModDataComponentTypes.UNDER_ARMOR_CONTENTS);
            if (component != null && !component.isEmpty()) {
                ItemStack armorItem = component.get(0);
                String[] material = underArmorItem.getMaterial().getIdAsString().split("[/:_]");
                String materialTranslation = "util.progression_respun." + material[1];
                String with = "util.progression_respun.with";
                return Text.translatable(materialTranslation).append(" ").append(Text.translatable(with)).append(" ").append(Text.translatable(armorItem.getTranslationKey()));
            }
            return this.getItem().getName(stack);
        }
        return this.getItem().getName(stack);
    }

    @WrapMethod(method = "appendAttributeModifiersTooltip")
    private void appendArmorToUnderArmorAttributes(Consumer<Text> textConsumer, @Nullable PlayerEntity player, Operation<Void> original) {
        AttributeModifiersComponent attributeModifiersComponent = this.getOrDefault(DataComponentTypes.ATTRIBUTE_MODIFIERS, AttributeModifiersComponent.DEFAULT);
        if (!attributeModifiersComponent.showInTooltip()) {
            return;
        }
        if (this.getItem() instanceof ArmorItem && this.isIn(UNDER_ARMOR)) {
            UnderArmorContentsComponent component = this.get(ModDataComponentTypes.UNDER_ARMOR_CONTENTS);
            if (component != null && !component.isEmpty()) {
                ItemStack armor = component.get(0);
                if (armor.isEmpty() || !(armor.getItem() instanceof ArmorItem)) return;

                for (AttributeModifierSlot attributeModifierSlot : AttributeModifierSlot.values()) {
                    MutableBoolean mutableBoolean = new MutableBoolean(true);
                    this.applyAttributeModifier(attributeModifierSlot, (attribute, modifier) -> {
                        if (mutableBoolean.isTrue()) {
                            textConsumer.accept(ScreenTexts.EMPTY);
                            textConsumer.accept(Text.translatable("item.modifiers." + attributeModifierSlot.asString()).formatted(Formatting.GRAY));
                            mutableBoolean.setFalse();
                        }
                        this.appendAttributeModifierTooltip(textConsumer, player, attribute, modifier);
                    });
                }
            }
        }
        for (AttributeModifierSlot attributeModifierSlot : AttributeModifierSlot.values()) {
            MutableBoolean mutableBoolean = new MutableBoolean(true);
            this.applyAttributeModifier(attributeModifierSlot, (attribute, modifier) -> {
                if (mutableBoolean.isTrue()) {
                    textConsumer.accept(ScreenTexts.EMPTY);
                    textConsumer.accept(Text.translatable("item.modifiers." + attributeModifierSlot.asString()).formatted(Formatting.GRAY));
                    mutableBoolean.setFalse();
                }
                this.appendAttributeModifierTooltip(textConsumer, player, attribute, modifier);
            });
        }
    }

    @Inject(method = "useOnBlock", at = @At("HEAD"), cancellable = true)
    private void useOnBlockIfNotBroken(ItemUsageContext context, CallbackInfoReturnable<ActionResult> cir) {
        if (isBroken()) {
            cir.setReturnValue(ActionResult.PASS);
        }
    }

    @Inject(method = "getMiningSpeedMultiplier", at = @At("HEAD"), cancellable = true)
    private void getMiningSpeedMultiplierIfNotBroken(BlockState state, CallbackInfoReturnable<Float> cir) {
        if (isBroken()) {
            cir.setReturnValue(1.0f);
        }
    }

    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    private void useIfNotBroken(World world, PlayerEntity user, Hand hand, CallbackInfoReturnable<TypedActionResult<ItemStack>> cir) {
        if (isBroken()) {
            cir.setReturnValue(TypedActionResult.fail((ItemStack) (Object) this));
        }
    }

    @Inject(method = "finishUsing", at = @At("HEAD"), cancellable = true)
    private void finishUsingIfNotBroken(World world, LivingEntity user, CallbackInfoReturnable<ItemStack> cir) {
        if (isBroken()) {
            cir.setReturnValue((ItemStack) (Object) this);
        }
    }

    @Inject(method = "postHit", at = @At("HEAD"), cancellable = true)
    private void postHitIfNotBroken(LivingEntity target, PlayerEntity player, CallbackInfoReturnable<Boolean> cir) {
        if (isBroken()) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "postDamageEntity", at = @At("HEAD"), cancellable = true)
    private void postDamageEntityIfNotBroken(LivingEntity target, PlayerEntity player, CallbackInfo ci) {
        if (isBroken()) {
            ci.cancel();
        }
    }

    @Inject(method = "postMine", at = @At("HEAD"), cancellable = true)
    private void postMineIfNotBroken(World world, BlockState state, BlockPos pos, PlayerEntity miner, CallbackInfo ci) {
        if (isBroken()) {
            ci.cancel();
        }
    }

    @Inject(method = "isSuitableFor", at = @At("HEAD"), cancellable = true)
    private void isSuitableForIfNotBroken(BlockState state, CallbackInfoReturnable<Boolean> cir) {
        if (isBroken()) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "getUseAction", at = @At("HEAD"), cancellable = true)
    private void getUseActionIfNotBroken(CallbackInfoReturnable<UseAction> cir) {
        if (isBroken()) {
            cir.setReturnValue(UseAction.NONE);
        }
    }

    @Inject(method = "onStoppedUsing", at = @At("HEAD"), cancellable = true)
    private void onStoppedUsingIfNotBroken(World world, LivingEntity user, int remainingUseTicks, CallbackInfo ci) {
        if (isBroken()) {
            ci.cancel();
        }
    }

    @Inject(method = "applyAttributeModifiers", at = @At("HEAD"), cancellable = true)
    private void applyAttributeModifiersIfNotBroken(EquipmentSlot slot, BiConsumer<RegistryEntry<EntityAttribute>, EntityAttributeModifier> attributeModifierConsumer, CallbackInfo ci) {
        if (isBroken()) {
            ci.cancel();
        }
    }

    @Inject(method = "isEnchantable", at = @At("HEAD"), cancellable = true)
    private void isEnchantableIfNotBroken(CallbackInfoReturnable<Boolean> cir) {
        if (isBroken()) {
            cir.setReturnValue(false);
        }
//        if (this.isIn(UNDER_ARMOR)) {
//            cir.setReturnValue(false);
//        }
    }
}
