package com.gayasslily.progression_respun.util;

import com.gayasslily.progression_respun.block.*;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.registry.OxidizableBlocksRegistry;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import org.apache.commons.lang3.function.TriFunction;
import org.jetbrains.annotations.NotNull;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

import static com.gayasslily.progression_respun.ProgressionRespun.MOD_ID;
import static com.gayasslily.progression_respun.ProgressionRespun.getBlockByName;

public class OxidizableUtil {

    public static void registerOxidizableFamily(Block baseBlock, String baseName, BiFunction<Oxidizable.OxidationLevel, Block.Settings, Block> factory, boolean hasItem) {
        Map<Oxidizable.OxidationLevel, Block> unwaxedStates = new EnumMap<>(Oxidizable.OxidationLevel.class);
        Map<Oxidizable.OxidationLevel, Block> waxedStates   = new EnumMap<>(Oxidizable.OxidationLevel.class);

        for (Oxidizable.OxidationLevel degradation : List.of(Oxidizable.OxidationLevel.EXPOSED, Oxidizable.OxidationLevel.WEATHERED, Oxidizable.OxidationLevel.OXIDIZED)) {
            String name = switch (degradation) {
                case EXPOSED   -> "exposed_" + baseName;
                case WEATHERED -> "weathered_" + baseName;
                case OXIDIZED  -> "oxidized_" + baseName;
                default -> throw new IllegalStateException("Unexpected oxidation level: " + degradation);
            };

            Block block = Registry.register(Registries.BLOCK, Identifier.of(MOD_ID, name), factory.apply(degradation, Block.Settings.copy(baseBlock)));
            unwaxedStates.put(degradation, block);

            if (hasItem) registerBlockItem(block, name, baseBlock);
            addEntities(block);
        }

        for (Oxidizable.OxidationLevel degradation : Oxidizable.OxidationLevel.values()) {
            String name = switch (degradation) {
                case UNAFFECTED -> "waxed_" + baseName;
                case EXPOSED   -> "waxed_exposed_" + baseName;
                case WEATHERED -> "waxed_weathered_" + baseName;
                case OXIDIZED  -> "waxed_oxidized_" + baseName;
            };

            Function<Block.Settings, ? extends Block> blockFactory = getBlockFactory(baseName);
            registerBlock(blockFactory, baseBlock, hasItem, name, waxedStates, degradation);
        }
        registerPairs(baseBlock, unwaxedStates, waxedStates);
    }

    public static void registerOxidizablePistonFamily(Block baseBlock, String baseName, TriFunction<Oxidizable.OxidationLevel, Boolean, Block.Settings, Block> factory, boolean hasItem) {
        Map<Oxidizable.OxidationLevel, Block> unwaxedStates = new EnumMap<>(Oxidizable.OxidationLevel.class);
        Map<Oxidizable.OxidationLevel, Block> waxedStates   = new EnumMap<>(Oxidizable.OxidationLevel.class);

        for (Oxidizable.OxidationLevel degradation : List.of(Oxidizable.OxidationLevel.EXPOSED, Oxidizable.OxidationLevel.WEATHERED, Oxidizable.OxidationLevel.OXIDIZED)) {
            String name = switch (degradation) {
                case EXPOSED   -> "exposed_" + baseName;
                case WEATHERED -> "weathered_" + baseName;
                case OXIDIZED  -> "oxidized_" + baseName;
                default -> throw new IllegalStateException("Unexpected oxidation level: " + degradation);
            };
            Block block;
            if (!baseName.contains("sticky")) block = Registry.register(Registries.BLOCK, Identifier.of(MOD_ID, name), factory.apply(degradation, false, Block.Settings.copy(baseBlock)));
            else block = Registry.register(Registries.BLOCK, Identifier.of(MOD_ID, name), factory.apply(degradation, true, Block.Settings.copy(baseBlock)));
            unwaxedStates.put(degradation, block);

            if (hasItem) registerBlockItem(block, name, baseBlock);
            addEntities(block);
        }

        for (Oxidizable.OxidationLevel degradation : Oxidizable.OxidationLevel.values()) {
            String name = switch (degradation) {
                case UNAFFECTED -> "waxed_" + baseName;
                case EXPOSED   -> "waxed_exposed_" + baseName;
                case WEATHERED -> "waxed_weathered_" + baseName;
                case OXIDIZED  -> "waxed_oxidized_" + baseName;
            };

            Block block;
            BiFunction<Boolean, Block.Settings, Block> waxedFactory = WaxedPistonBlock::new;
            if (!baseName.contains("sticky")) block = Registry.register(Registries.BLOCK, Identifier.of(MOD_ID, name), waxedFactory.apply(false, Block.Settings.copy(baseBlock)));
            else block = Registry.register(Registries.BLOCK, Identifier.of(MOD_ID, name), waxedFactory.apply(true, Block.Settings.copy(baseBlock)));
            waxedStates.put(degradation, block);

            if (hasItem) registerBlockItem(block, name, baseBlock);
            addEntities(block);
        }
        registerPairs(baseBlock, unwaxedStates, waxedStates);
    }
    
    public static void registerBlock(Function<Block.Settings, ? extends Block> blockFactory, Block baseBlock, boolean hasItem, String name, Map<Oxidizable.OxidationLevel, Block> waxedStates, Oxidizable.OxidationLevel degradation) {
        Block block = Registry.register(Registries.BLOCK, Identifier.of(MOD_ID, name), blockFactory.apply(Block.Settings.copy(baseBlock)));
        waxedStates.put(degradation, block);
        if (hasItem)registerBlockItem(block, name, baseBlock);
        addEntities(block);
    }

    private static @NotNull Function<Block.Settings, ? extends Block> getBlockFactory(String baseName) {
        Function<Block.Settings, ? extends Block> blockFactory = Block::new;
        if (baseName.equals("crafter")) blockFactory = CrafterBlock::new;
        if (baseName.equals("observer")) blockFactory = ObserverBlock::new;
        if (baseName.equals("piston_head")) blockFactory = WaxedPistonHeadBlock::new;
        if (baseName.equals("moving_piston")) blockFactory = WaxedPistonExtensionBlock::new;
        if (baseName.equals("dispenser")) blockFactory = DispenserBlock::new;
        if (baseName.equals("dropper")) blockFactory = DropperBlock::new;
        return blockFactory;
    }

    public static void registerPairs(Block baseBlock, Map<Oxidizable.OxidationLevel, Block> unwaxedStates, Map<Oxidizable.OxidationLevel, Block> waxedStates) {
        OxidizableBlocksRegistry.registerOxidizableBlockPair(baseBlock, unwaxedStates.get(Oxidizable.OxidationLevel.EXPOSED));
        OxidizableBlocksRegistry.registerOxidizableBlockPair(unwaxedStates.get(Oxidizable.OxidationLevel.EXPOSED), unwaxedStates.get(Oxidizable.OxidationLevel.WEATHERED));
        OxidizableBlocksRegistry.registerOxidizableBlockPair(unwaxedStates.get(Oxidizable.OxidationLevel.WEATHERED), unwaxedStates.get(Oxidizable.OxidationLevel.OXIDIZED));

        for (Oxidizable.OxidationLevel degradation : Oxidizable.OxidationLevel.values()) {
            Block unwaxed = (degradation == Oxidizable.OxidationLevel.UNAFFECTED) ? baseBlock : unwaxedStates.get(degradation);
            Block waxed = waxedStates.get(degradation);

            OxidizableBlocksRegistry.registerWaxableBlockPair(unwaxed, waxed);
        }
    }

    private static void registerBlockItem(Block block, String name, Block baseBlock) {
        Identifier id = Identifier.of(MOD_ID, name);
        Registry.register(Registries.ITEM, id, new BlockItem(block, new Item.Settings()));

        Identifier blockId = Registries.BLOCK.getId(baseBlock);
        String prefix = name.replace(blockId.getPath(), "").replace("waxed_", "");

        if (prefix.isEmpty()) name = name.replace("waxed_", "oxidized_");
        if (prefix.equals("exposed_")) name = name.replace(prefix, "");
        if (prefix.equals("weathered_")) name = name.replace(prefix, "exposed_");
        if (prefix.equals("oxidized_")) name = name.replace(prefix, "weathered_");

        String finalName = name;
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.REDSTONE).register(entries -> entries.addAfter(getBlockByName(finalName), block));
    }

    public static void addEntities(Block block) {
        if (block instanceof DispenserBlock) BlockEntityType.DISPENSER.addSupportedBlock(block);
        if (block instanceof DropperBlock) BlockEntityType.DROPPER.addSupportedBlock(block);
        if (block instanceof CrafterBlock) BlockEntityType.CRAFTER.addSupportedBlock(block);
        if (block instanceof PistonExtensionBlock) BlockEntityType.PISTON.addSupportedBlock(block);
    }
}
