package com.progression_respun.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.progression_respun.ProgressionRespun;
import com.progression_respun.component.ModDataComponentTypes;
import com.progression_respun.util.ArmorUtil;
import net.fabricmc.fabric.api.item.v1.FabricItemStack;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FarmlandBlock;
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
import net.minecraft.registry.tag.EnchantmentTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.progression_respun.ProgressionRespun.getArmor;
import static com.progression_respun.block.ModBlockTags.BURNABLE_COBWEBS;
import static com.progression_respun.data.ModItemTagProvider.*;
import static com.progression_respun.util.PropertyUtil.FERTILIZED;

@Debug(export = true)
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

    @Shadow public abstract Text getName();

    @Shadow
    protected abstract <T extends TooltipAppender> void appendTooltip(ComponentType<T> componentType, Item.TooltipContext context, Consumer<Text> textConsumer, TooltipType type);

    @Shadow
    public abstract boolean hasEnchantments();

    @Unique
    private boolean isBroken() {
        return isDamageable() && getDamage() >= getMaxDamage();
    }

    @Inject(method = "damage(ILnet/minecraft/server/world/ServerWorld;Lnet/minecraft/server/network/ServerPlayerEntity;Ljava/util/function/Consumer;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;decrement(I)V", shift = At.Shift.BEFORE), cancellable = true)
    private void progressionrespun$damageRestrictDecrement(int amount, ServerWorld world, @Nullable ServerPlayerEntity player, Consumer<Item> breakCallback, CallbackInfo ci, @Local(ordinal = 1) int i) {
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

//    @ModifyReturnValue(method = "getName", at = @At("RETURN"))
//    private Text progressionrespun$modifyName(Text original) {
//        ItemStack stack = (ItemStack)(Object) this;
//        String with = "util.progression_respun.with";
//        if (stack.getItem() instanceof ArmorItem underArmorItem && stack.isIn(UNDER_ARMOR)) {
//            UnderArmorContentsComponent component = stack.get(ModDataComponentTypes.UNDER_ARMOR_CONTENTS);
//            if (component != null && !component.isEmpty()) {
//                ItemStack armorItem = component.get(0);
//                if (isBroken()) return Text.translatable("item.progression_respun.tooltip.broken", Text.translatable(with, original, armorItem.getName())).formatted(Formatting.RED);
//                return Text.translatable(with, original, armorItem.getName());
//            }
//        }
//        if (stack.getItem() instanceof EnchantedBookItem) {
//            ItemEnchantmentsComponent storedEnchants = stack.get(DataComponentTypes.STORED_ENCHANTMENTS);
//            if (storedEnchants != null && !storedEnchants.isEmpty()) {
//                Object2IntMap.Entry<RegistryEntry<Enchantment>> enchantment = storedEnchants.getEnchantmentEntries().stream().findFirst().get();
//                Text enchantmentName = enchantment.getKey().value().description();
//                return Text.translatable(with, original, enchantmentName);
//            }
//        }
//        if (isBroken()) return Text.translatable("item.progression_respun.tooltip.broken", original).formatted(Formatting.RED);
//        return original;
//    }

    @WrapMethod(method = "appendAttributeModifiersTooltip")
    private void progressionrespun$appendArmorToUnderArmorAttributesTooltip(Consumer<Text> textConsumer, @Nullable PlayerEntity player, Operation<Void> original) {
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

    @Redirect(method = "getTooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;getRarity()Lnet/minecraft/util/Rarity;"))
    private Rarity progressionrespun$getTooltip(ItemStack instance) {
        ItemStack armorStack = getArmor(instance);
        if (armorStack != ItemStack.EMPTY) {
            return armorStack.getRarity();
        }
        return instance.getRarity();
    }

    @WrapOperation(method = "getTooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;contains(Lnet/minecraft/component/ComponentType;)Z", ordinal = 3))
    private <T> boolean progressionrespun$getTooltip1(ItemStack instance, ComponentType<? extends T> componentType, Operation<Boolean> original) {
        ItemStack armorStack = getArmor(instance);
        if (armorStack != ItemStack.EMPTY) {
            return original.call(armorStack, componentType);
        }
        return original.call(instance, componentType);
    }

    @WrapOperation(method = "getTooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;appendTooltip(Lnet/minecraft/component/ComponentType;Lnet/minecraft/item/Item$TooltipContext;Ljava/util/function/Consumer;Lnet/minecraft/item/tooltip/TooltipType;)V"))
    private <T> void progressionrespun$getTooltip2(ItemStack instance, ComponentType<T> componentType, Item.TooltipContext context, Consumer<Text> textConsumer, TooltipType type, Operation<Void> original, @Local List<Text> list) {
        ItemStack armorStack = getArmor(instance);

        if (componentType != DataComponentTypes.ENCHANTMENTS) {
            if (armorStack != ItemStack.EMPTY) {
                original.call(armorStack, componentType, context, textConsumer, type);
                return;
            }
            original.call(instance, componentType, context, textConsumer, type);
        }
    }

    @WrapOperation(method = "getTooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;appendTooltip(Lnet/minecraft/component/ComponentType;Lnet/minecraft/item/Item$TooltipContext;Ljava/util/function/Consumer;Lnet/minecraft/item/tooltip/TooltipType;)V", ordinal = 2))
    private <T> void progressionrespun$getEnchantmentsTooltip(ItemStack instance, ComponentType<T> componentType, Item.TooltipContext context, Consumer<Text> textConsumer, TooltipType type, Operation<Void> original, @Local List<Text> list) {
        appendEnchantmentsTooltip((ItemStack) (Object) this, list);
    }

    @WrapOperation(method = "getTooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;getItem()Lnet/minecraft/item/Item;", ordinal = 0))
    private Item progressionrespun$getTooltip3(ItemStack instance, Operation<Item> original) {
        ItemStack armorStack = getArmor(instance);
        if (armorStack != ItemStack.EMPTY) {
            return original.call(armorStack);
        }
        return original.call(instance);
    }

    @WrapOperation(method = "getTooltip", at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z", ordinal = 2))
    private <E> boolean progressionrespun$getTooltip5(List<Text> instance, E e, Operation<Boolean> original) {
        ItemStack stack = (ItemStack) (Object) this;
        ItemStack armorStack = getArmor(stack);
        if (armorStack != ItemStack.EMPTY) {
            instance.add(Text.translatable("item.durability", armorStack.getMaxDamage() - armorStack.getDamage(), armorStack.getMaxDamage()));
            original.call(instance, Text.translatable("util.durability.underarmor", stack.getMaxDamage() - stack.getDamage(), stack.getMaxDamage()));
            return false;
        }
        return original.call(instance, e);
    }

    @WrapOperation(method = "getTooltip", at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z", ordinal = 3))
    private <E> boolean progressionrespun$getTooltip6(List<Text> instance, E e, Operation<Boolean> original) {
        ItemStack stack = (ItemStack) (Object) this;
        ItemStack armorStack = getArmor(stack);
        if (armorStack != ItemStack.EMPTY) {
            original.call(instance, Text.literal(Registries.ITEM.getId(armorStack.getItem()).toString()).formatted(Formatting.DARK_GRAY));
            original.call(instance, Text.literal(Registries.ITEM.getId(stack.getItem()).toString()).formatted(Formatting.DARK_GRAY));
            return false;
        }
        return original.call(instance, e);
    }

    @WrapOperation(method = "getTooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/Item;isEnabled(Lnet/minecraft/resource/featuretoggle/FeatureSet;)Z"))
    private boolean progressionrespun$getTooltip7(Item instance, FeatureSet featureSet, Operation<Boolean> original) {
        ItemStack armorStack = getArmor(instance.getDefaultStack());
        if (armorStack != ItemStack.EMPTY) {
            return original.call(armorStack.getItem(), featureSet);
        }
        return original.call(instance, featureSet);
    }

    @WrapMethod(method = "useOnBlock")
    private ActionResult progressionrespun$useOnBlockIfNotBroken(ItemUsageContext context, Operation<ActionResult> original) {

        if (isBroken()) {
            return ActionResult.PASS;
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
        return original.call(context);
    }

    @WrapMethod(method = "useOnBlock")
    private ActionResult progressionrespun$burnCobwebs(ItemUsageContext context, Operation<ActionResult> original) {
        ItemStack stack = (ItemStack) (Object) this;
        World world = context.getWorld();
        BlockPos pos = context.getBlockPos();
        BlockState state = world.getBlockState(pos);
        PlayerEntity player = context.getPlayer();
        Hand hand = context.getHand();
        Direction side = context.getSide();
        EquipmentSlot slot = hand == Hand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND;

        if (stack.isIn(CAN_BURN_COBWEBS) && state.isIn(BURNABLE_COBWEBS)) {
            if (!world.isClient && player != null) {
                world.setBlockState(pos, Blocks.AIR.getDefaultState());
                if (stack.isDamageable()) stack.damage(1, player, slot);
                player.playSound(SoundEvents.ITEM_FLINTANDSTEEL_USE, 0.8f, 0.8f + world.getRandom().nextFloat() * 0.4f);
                ((ServerWorld) world).spawnParticles(ParticleTypes.FLAME, pos.getX() + 0.3, pos.getY() + 0.3, pos.getZ() + 0.3, 15, 0.2, 0.2, 0.2, 0.01);
            }
            return ActionResult.SUCCESS;
        }
        return original.call(context);
    }

    @WrapMethod(method = "useOnBlock")
    private ActionResult progressionrespun$fertilizeFarmland(ItemUsageContext context, Operation<ActionResult> original) {
        ItemStack stack = (ItemStack) (Object) this;
        World world = context.getWorld();
        BlockPos pos = context.getBlockPos();
        BlockState state = world.getBlockState(pos);
        PlayerEntity player = context.getPlayer();

        if (stack.isIn(CAN_FERTILIZE_FARMLAND) && state.getBlock() instanceof FarmlandBlock && !state.get(FERTILIZED)) {
            if (!world.isClient && player != null) {
                world.setBlockState(pos, state.with(FERTILIZED, true));
                stack.decrementUnlessCreative(1, player);
                player.playSound(SoundEvents.BLOCK_ROOTED_DIRT_HIT, 0.8f, 0.8f + world.getRandom().nextFloat() * 0.4f);
            }
            return ActionResult.SUCCESS;
        }
        return original.call(context);
    }

    @Inject(method = "getMiningSpeedMultiplier", at = @At("HEAD"), cancellable = true)
    private void progressionrespun$getMiningSpeedMultiplierIfNotBroken(BlockState state, CallbackInfoReturnable<Float> cir) {
        if (isBroken()) {
            cir.setReturnValue(1.0f);
        }
    }

    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    private void progressionrespun$useIfNotBroken(World world, PlayerEntity user, Hand hand, CallbackInfoReturnable<TypedActionResult<ItemStack>> cir) {
        if (isBroken()) {
            cir.setReturnValue(TypedActionResult.fail((ItemStack) (Object) this));
        }
    }

    @Inject(method = "finishUsing", at = @At("HEAD"), cancellable = true)
    private void progressionrespun$finishUsingIfNotBroken(World world, LivingEntity user, CallbackInfoReturnable<ItemStack> cir) {
        if (isBroken()) {
            cir.setReturnValue((ItemStack) (Object) this);
        }
    }

    @Inject(method = "postHit", at = @At("HEAD"), cancellable = true)
    private void progressionrespun$postHitIfNotBroken(LivingEntity target, PlayerEntity player, CallbackInfoReturnable<Boolean> cir) {
        if (isBroken()) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "postDamageEntity", at = @At("HEAD"), cancellable = true)
    private void progressionrespun$postDamageEntityIfNotBroken(LivingEntity target, PlayerEntity player, CallbackInfo ci) {
        if (isBroken()) {
            ci.cancel();
        }
    }

    @Inject(method = "postMine", at = @At("HEAD"), cancellable = true)
    private void progressionrespun$postMineIfNotBroken(World world, BlockState state, BlockPos pos, PlayerEntity miner, CallbackInfo ci) {
        if (isBroken()) {
            ci.cancel();
        }
    }

    @Inject(method = "isSuitableFor", at = @At("HEAD"), cancellable = true)
    private void progressionrespun$isSuitableForIfNotBroken(BlockState state, CallbackInfoReturnable<Boolean> cir) {
        if (isBroken()) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "getUseAction", at = @At("HEAD"), cancellable = true)
    private void progressionrespun$getUseActionIfNotBroken(CallbackInfoReturnable<UseAction> cir) {
        if (isBroken()) {
            cir.setReturnValue(UseAction.NONE);
        }
    }

    @Inject(method = "onStoppedUsing", at = @At("HEAD"), cancellable = true)
    private void progressionrespun$onStoppedUsingIfNotBroken(World world, LivingEntity user, int remainingUseTicks, CallbackInfo ci) {
        if (isBroken()) {
            ci.cancel();
        }
    }

    @Inject(method = "applyAttributeModifiers", at = @At("HEAD"), cancellable = true)
    private void progressionrespun$applyAttributeModifiersIfNotBroken(EquipmentSlot slot, BiConsumer<RegistryEntry<EntityAttribute>, EntityAttributeModifier> attributeModifierConsumer, CallbackInfo ci) {
        if (isBroken()) {
            ci.cancel();
        }
    }

    @ModifyReturnValue(method = "isEnchantable", at = @At("RETURN"))
    public boolean progressionrespun$isEnchantable(boolean original) {
        ItemStack item = ((ItemStack) (Object) this);
        if (isBroken()) {
            return false;
        }
        if (item.getItem() instanceof ArmorItem && item.isIn(UNDER_ARMOR)){
            var component = item.getComponents().get(ModDataComponentTypes.UNDER_ARMOR_CONTENTS);
            if (component != null) {
                if (!component.isEmpty()) {
                    return !getArmor(item).hasEnchantments();
                } else {
                    return false;
                }
            }
        } else {
            return item.isIn(ConventionalItemTags.ENCHANTABLES);
        }
        return original;
    }

    @Unique
    private void appendEnchantmentsTooltip(ItemStack itemStack, List<Text> list) {
        ItemStack armorStack = getArmor(itemStack);

        List<Text> curses = new ArrayList<>();
        List<Text> enchantments = new ArrayList<>();
        List<Text> enchantments1 = new ArrayList<>();


        if (itemStack.hasEnchantments() || itemStack.getItem() instanceof EnchantedBookItem) {
            ItemEnchantmentsComponent component;
            if (itemStack.getItem() instanceof EnchantedBookItem) {
                component = itemStack.get(DataComponentTypes.STORED_ENCHANTMENTS);
            } else {
                component = itemStack.getEnchantments();
            }

            if (component != null) {
                for (Entry<RegistryEntry<Enchantment>> entry : component.getEnchantmentEntries()) {
                    RegistryEntry<Enchantment> enchantment = entry.getKey();
                    int level = entry.getIntValue();
                    Text line = Enchantment.getName(enchantment, level);

                    if (enchantment.isIn(EnchantmentTags.CURSE)) {
                        curses.add(line);
                    } else {
                        enchantments.add(line);
                    }
                }
            }

            enchantments = enchantments.stream().map(t -> Text.of(Text.literal(" " + t.getString()).formatted(Formatting.GOLD))).collect(Collectors.toList());
        }
        if (!armorStack.isEmpty() && armorStack.hasEnchantments()) {
            ItemEnchantmentsComponent component = armorStack.getEnchantments();

            for (Entry<RegistryEntry<Enchantment>> entry : component.getEnchantmentEntries()) {
                RegistryEntry<Enchantment> enchantment = entry.getKey();
                int level = entry.getIntValue();
                Text line = Enchantment.getName(enchantment, level);

                if (enchantment.isIn(EnchantmentTags.CURSE)) {
                    curses.add(line);
                } else {
                    enchantments1.add(line);
                }
            }

            enchantments1 = enchantments1.stream().map(t -> Text.of(Text.literal(" " + t.getString()).formatted(Formatting.GOLD))).collect(Collectors.toList());
        }

        if (!enchantments.isEmpty() && armorStack.isEmpty()) {
            list.add(Text.translatable("tooltip.progression_respun.enchantments").formatted(Formatting.GRAY));
            list.addAll(enchantments);
        }
        if (!enchantments1.isEmpty() && !armorStack.isEmpty()) {
            list.add(Text.translatable("tooltip.progression_respun.enchantments").formatted(Formatting.GRAY));
            list.addAll(enchantments1);
        }
        if (!curses.isEmpty()) {
            curses = curses.stream().map(t -> Text.of(Text.literal(" " + t.getString()).formatted(Formatting.RED))).collect(Collectors.toList());
            list.add(Text.translatable("tooltip.progression_respun.curses").formatted(Formatting.GRAY));
            HashSet<Text> set = new HashSet<>(curses);
            curses = set.stream().toList();

            list.addAll(curses);
        }
    }
}
