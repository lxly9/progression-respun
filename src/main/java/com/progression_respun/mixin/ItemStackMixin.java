package com.progression_respun.mixin;

import com.google.common.collect.Lists;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.progression_respun.ProgressionRespun;
import com.progression_respun.component.ModDataComponentTypes;
import com.progression_respun.component.type.UnderArmorContentsComponent;
import com.progression_respun.util.ArmorUtil;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.fabricmc.fabric.api.item.v1.FabricItemStack;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.component.*;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.item.tooltip.TooltipAppender;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static com.progression_respun.block.ModBlockTags.BURNABLE_COBWEBS;
import static com.progression_respun.data.ModItemTagProvider.CAN_BURN_COBWEBS;
import static com.progression_respun.data.ModItemTagProvider.UNDER_ARMOR;

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

    @Shadow protected abstract <T extends TooltipAppender> void appendTooltip(ComponentType<T> componentType, Item.TooltipContext context, Consumer<Text> textConsumer, TooltipType type);

    @Shadow protected abstract void appendAttributeModifiersTooltip(Consumer<Text> textConsumer, @Nullable PlayerEntity player);

    @Shadow public abstract boolean isDamaged();

    @Shadow @Final private ComponentMapImpl components;

    @Shadow public abstract Text getName();

    @Shadow public abstract Rarity getRarity();

    @Shadow public abstract boolean isOf(Item item);

    @Shadow @Final private static Text DISABLED_TEXT;

    @Unique
    private boolean isBroken() {
        return isDamageable() && getDamage() >= getMaxDamage();
    }

    @Inject(method = "damage(ILnet/minecraft/server/world/ServerWorld;Lnet/minecraft/server/network/ServerPlayerEntity;Ljava/util/function/Consumer;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;decrement(I)V", shift = At.Shift.BEFORE), cancellable = true)
    private void damageRestrictDecrement(int amount, ServerWorld world, @Nullable ServerPlayerEntity player, Consumer<Item> breakCallback, CallbackInfo ci, @Local(ordinal = 1) int i) {
        Item item = getItem();
        boolean noDestroy = item instanceof ToolItem || item instanceof ArmorItem || item instanceof ShieldItem || item instanceof PotionItem;

        if (!noDestroy) {
            decrement(amount);
        }
        if (!noDestroy || i - amount < this.getMaxDamage()) {
            breakCallback.accept(item);
        }
        ci.cancel();
    }

//    @Inject(method = "getTooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/Item;appendTooltip(Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/Item$TooltipContext;Ljava/util/List;Lnet/minecraft/item/tooltip/TooltipType;)V", ordinal = 0, shift = At.Shift.BEFORE))
//    private void getBrokenTooltip(Item.TooltipContext context, @Nullable PlayerEntity player, TooltipType type,
//                                  CallbackInfoReturnable<List<Text>> cir, @Local Consumer<Text> tooltip) {
//        if (isBroken()) {
//            tooltip.accept(BROKEN_TEXT);
//        }
//    }

    @ModifyReturnValue(method = "getName", at = @At("RETURN"))
    private Text modifyName(Text original) {
        ItemStack stack = (ItemStack)(Object) this;
        String with = "util.progression_respun.with";
        if (stack.getItem() instanceof ArmorItem underArmorItem && stack.isIn(UNDER_ARMOR)) {
            UnderArmorContentsComponent component = stack.get(ModDataComponentTypes.UNDER_ARMOR_CONTENTS);
            if (component != null && !component.isEmpty()) {
                ItemStack armorItem = component.get(0);
                if (isBroken()) return Text.translatable("item.progression_respun.tooltip.broken", Text.translatable(with, original, armorItem.getName())).formatted(Formatting.RED);
                return Text.translatable(with, original, armorItem.getName());
            }
        }
        if (stack.getItem() instanceof EnchantedBookItem) {
            ItemEnchantmentsComponent storedEnchants = stack.get(DataComponentTypes.STORED_ENCHANTMENTS);
            if (storedEnchants != null && !storedEnchants.isEmpty()) {
                Object2IntMap.Entry<RegistryEntry<Enchantment>> enchantment = storedEnchants.getEnchantmentEntries().stream().findFirst().get();
                Text enchantmentName = enchantment.getKey().value().description();
                return Text.translatable(with, original, enchantmentName);
            }
        }
        if (isBroken()) return Text.translatable("item.progression_respun.tooltip.broken", original).formatted(Formatting.RED);
        return original;
    }

    @WrapMethod(method = "appendAttributeModifiersTooltip")
    private void appendArmorToUnderArmorAttributesTooltip(Consumer<Text> textConsumer, @Nullable PlayerEntity player, Operation<Void> original) {
        AttributeModifiersComponent attributeModifiersComponent = this.getOrDefault(DataComponentTypes.ATTRIBUTE_MODIFIERS, AttributeModifiersComponent.DEFAULT);
        if (!attributeModifiersComponent.showInTooltip()) {
            return;
        }
        ItemStack underArmor = (ItemStack) (Object) this;
        ItemStack armor = null;

        if (underArmor.getItem() instanceof ArmorItem) {
            if (underArmor.isIn(UNDER_ARMOR)) {
                var component = underArmor.get(ModDataComponentTypes.UNDER_ARMOR_CONTENTS);
                if (component != null && !component.isEmpty()) {
                    armor = component.get(0);
                }
            }

            for (AttributeModifierSlot slot : AttributeModifierSlot.values()) {
                var collected = ArmorUtil.collectModifiersForSlot(underArmor, armor, slot);
                if (collected.isEmpty()) continue;

                textConsumer.accept(ScreenTexts.EMPTY);
                textConsumer.accept(Text.translatable("item.modifiers." + slot.asString()).formatted(Formatting.GRAY));

                for (var entry : collected.entrySet()) {
                    ArmorUtil.printAttributeLine(textConsumer, entry.getKey(), entry.getValue().getLeft(), entry.getValue().getRight());
                }
            }
        } else {
            original.call(textConsumer, player);
        }
    }

    @ModifyReturnValue(method = "getTooltip", at = @At("RETURN"))
    private List<Text> isUnderArmor(List<Text> original, @Local(argsOnly = true) Item.TooltipContext context, @Local(argsOnly = true) PlayerEntity player, @Local(argsOnly = true) TooltipType type) {
        ItemStack stack = (ItemStack)(Object)this;

        if (stack.getItem() instanceof ArmorItem && stack.isIn(UNDER_ARMOR)) {
            var component = stack.get(ModDataComponentTypes.UNDER_ARMOR_CONTENTS);
            if (component != null && !component.isEmpty()) {
                ItemStack stack1 = component.get(0);
                BlockPredicatesChecker blockPredicatesChecker2;

                if (!type.isCreative() && this.contains(DataComponentTypes.HIDE_TOOLTIP)) {
                    return List.of();
                }
                ArrayList<Text> list = Lists.newArrayList();
                MutableText mutableText = Text.empty().append(stack.getName()).formatted(stack1.getRarity().getFormatting());

                if (stack1.contains(DataComponentTypes.CUSTOM_NAME)) {
                    mutableText.formatted(Formatting.ITALIC);
                }
                list.add(mutableText);

                Consumer<Text> consumer = list::add;
                if (!stack1.contains(DataComponentTypes.HIDE_ADDITIONAL_TOOLTIP)) {
                    stack1.getItem().appendTooltip(stack, context, list, type);
                }

                appendTooltip(DataComponentTypes.TRIM, context, consumer, type);
                appendTooltip(DataComponentTypes.STORED_ENCHANTMENTS, context, consumer, type);
                appendTooltip(DataComponentTypes.ENCHANTMENTS, context, consumer, type);
                appendTooltip(DataComponentTypes.DYED_COLOR, context, consumer, type);
                appendTooltip(DataComponentTypes.LORE, context, consumer, type);
                appendAttributeModifiersTooltip(consumer, player);
                appendTooltip(DataComponentTypes.UNBREAKABLE, context, consumer, type);
                BlockPredicatesChecker blockPredicatesChecker = stack1.get(DataComponentTypes.CAN_BREAK);
                if (blockPredicatesChecker != null && blockPredicatesChecker.showInTooltip()) {
                    consumer.accept(ScreenTexts.EMPTY);
                    consumer.accept(BlockPredicatesChecker.CAN_BREAK_TEXT);
                    blockPredicatesChecker.addTooltips(consumer);
                }
                if ((blockPredicatesChecker2 = stack1.get(DataComponentTypes.CAN_PLACE_ON)) != null && blockPredicatesChecker2.showInTooltip()) {
                    consumer.accept(ScreenTexts.EMPTY);
                    consumer.accept(BlockPredicatesChecker.CAN_PLACE_TEXT);
                    blockPredicatesChecker2.addTooltips(consumer);
                }
                if (type.isAdvanced()) {
                    if (stack1.isDamaged()) {
                        list.add(Text.translatable("item.durability", stack1.getMaxDamage() - stack1.getDamage(), stack1.getMaxDamage()));
                        list.add(Text.translatable("util.durability.underarmor", stack.getMaxDamage() - stack.getDamage(), stack.getMaxDamage()));
                    }
                    list.add(Text.literal(Registries.ITEM.getId(stack1.getItem()).toString()).formatted(Formatting.DARK_GRAY));
                    list.add(Text.literal(Registries.ITEM.getId(stack.getItem()).toString()).formatted(Formatting.DARK_GRAY));
                    int i = stack1.getComponents().size();
                    if (i > 0) {
                        list.add(Text.translatable("item.components", i).formatted(Formatting.DARK_GRAY));
                    }
                }
                if (player != null && !stack1.getItem().isEnabled(player.getWorld().getEnabledFeatures())) {
                    list.add(DISABLED_TEXT);
                }
                return list;
            }
        }
        return original;
    }

    @WrapOperation(method = "getTooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;getName()Lnet/minecraft/text/Text;"))
    private Text progressionrespun$getTooltip(ItemStack instance, Operation<Text> original) {
        ItemStack armorStack = getUnderArmor(instance);
        if (armorStack != ItemStack.EMPTY) {
            return original.call(armorStack);
        }
        return original.call(instance);
    }

    @WrapOperation(method = "getTooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;contains(Lnet/minecraft/component/ComponentType;)Z"))
    private <T> boolean progressionrespun$getTooltip1(ItemStack instance, ComponentType<? extends T> componentType, Operation<Boolean> original) {
        ItemStack armorStack = getUnderArmor(instance);
        if (armorStack != ItemStack.EMPTY) {
            return original.call(armorStack, componentType);
        }
        return original.call(instance, componentType);
    }

    @WrapOperation(method = "getTooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;getItem()Lnet/minecraft/item/Item;"))
    private Item progressionrespun$getTooltip3(ItemStack instance, Operation<Item> original) {
        ItemStack armorStack = getUnderArmor(instance);
        if (armorStack != ItemStack.EMPTY) {
            return original.call(armorStack);
        }
        return original.call(instance);
    }

    @WrapOperation(method = "getTooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;get(Lnet/minecraft/component/ComponentType;)Ljava/lang/Object;"))
    private <T> Object progressionrespun$getTooltip4(ItemStack instance, ComponentType<? extends T> componentType, Operation<Object> original) {
        ItemStack armorStack = getUnderArmor(instance);
        if (armorStack != ItemStack.EMPTY) {
            return original.call(armorStack, componentType);
        }
        return original.call(instance, componentType);
    }

    @Redirect(method = "getTooltip", at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z"))
    private <E> boolean progressionrespun$getTooltip5(List<Text> instance, E e) {
        ItemStack stack = (ItemStack) (Object) this;
        ItemStack armorStack = getUnderArmor(stack);
        if (armorStack != ItemStack.EMPTY) {
            instance.add(Text.translatable("item.durability", armorStack.getMaxDamage() - armorStack.getDamage(), armorStack.getMaxDamage()));
            instance.add(Text.translatable("util.durability.underarmor", stack.getMaxDamage() - stack.getDamage(), stack.getMaxDamage()));
            return false;
        }
        return true;
    }

    @Redirect(method = "getTooltip", at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z"))
    private <E> boolean progressionrespun$getTooltip6(List<Text> instance, E e) {
        ItemStack stack = (ItemStack) (Object) this;
        ItemStack armorStack = getUnderArmor(stack);
        if (armorStack != ItemStack.EMPTY) {
            instance.add(Text.literal(Registries.ITEM.getId(armorStack.getItem()).toString()).formatted(Formatting.DARK_GRAY));
            instance.add(Text.literal(Registries.ITEM.getId(stack.getItem()).toString()).formatted(Formatting.DARK_GRAY));
            return false;
        }
        return true;
    }

    @WrapOperation(method = "getTooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;getItem()Lnet/minecraft/item/Item;"))
    private Item progressionrespun$getTooltip7(ItemStack instance, Operation<Item> original) {
        ItemStack armorStack = getUnderArmor(instance);
        if (armorStack != ItemStack.EMPTY) {
            return original.call(armorStack);
        }
        return original.call(instance);
    }

    @Unique
    private ItemStack getUnderArmor(ItemStack stack) {
        if (stack.getItem() instanceof ArmorItem && stack.isIn(UNDER_ARMOR)) {
            var component = stack.get(ModDataComponentTypes.UNDER_ARMOR_CONTENTS);
            if (component != null && !component.isEmpty()) {
                return component.get(0);
            }
        }
        return ItemStack.EMPTY;
    }

    @Inject(method = "useOnBlock", at = @At("HEAD"), cancellable = true)
    private void useOnBlockIfNotBroken(ItemUsageContext context, CallbackInfoReturnable<ActionResult> cir) {
        ItemStack stack = (ItemStack) (Object) this;
        World world = context.getWorld();
        BlockPos pos = context.getBlockPos();
        BlockState state = world.getBlockState(pos);
        PlayerEntity player = context.getPlayer();
        Hand hand = context.getHand();
        Direction side = context.getSide();
        EquipmentSlot slot = hand == Hand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND;

        if (isBroken()) {
            cir.setReturnValue(ActionResult.PASS);
        }
        if (stack.isIn(CAN_BURN_COBWEBS) && state.isIn(BURNABLE_COBWEBS)) {
            if (!world.isClient && player != null) {
                world.setBlockState(pos, Blocks.AIR.getDefaultState());
                if (stack.isDamageable()) stack.damage(1, player, slot);
                player.playSound(SoundEvents.ITEM_FLINTANDSTEEL_USE, 0.8f, 0.8f + world.getRandom().nextFloat() * 0.4f);
                ((ServerWorld) world).spawnParticles(ParticleTypes.FLAME, pos.getX() + 0.3, pos.getY() + 0.3, pos.getZ() + 0.3, 15, 0.2, 0.2, 0.2, 0.01);
            }
            cir.setReturnValue(ActionResult.SUCCESS);
            cir.cancel();
        }
//        if (stack.isOf(Items.FLINT)) {
//            BlockItem blockItem = (BlockItem) ModBlocks.FLINT_PEBBLES.asItem();
//
//            ItemPlacementContext placementContext = new ItemPlacementContext(world, player, context.getHand(), blockItem.getDefaultStack(), new BlockHitResult(context.getHitPos(), side, pos, context.hitsInsideBlock()));
//
//            if (state.isOf(ModBlocks.FLINT_PEBBLES)) {
//                Pebbles pebbles = state.get(PEBBLES);
//                if (pebbles == Pebbles.ONE) {
//                    world.setBlockState(pos, state.with(PEBBLES, Pebbles.TWO));
//                    stack.decrementUnlessCreative(1, player);
//                    cir.setReturnValue(ActionResult.SUCCESS);
//                    cir.cancel();
//                }
//                if (pebbles == Pebbles.TWO) {
//                    world.setBlockState(pos, state.with(PEBBLES, Pebbles.THREE));
//                    stack.decrementUnlessCreative(1, player);
//                    cir.setReturnValue(ActionResult.SUCCESS);
//                    cir.cancel();
//                }
//            }
//            if (placementContext.canPlace()) {
//                blockItem.place(placementContext);
//                stack.decrementUnlessCreative(1, player);
//                cir.setReturnValue(ActionResult.SUCCESS);
//                cir.cancel();
//            }
//        }
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
