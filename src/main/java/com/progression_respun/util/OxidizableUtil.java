package com.progression_respun.util;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.registry.OxidizableBlocksRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.Oxidizable;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

public class OxidizableUtil {

    public static void registerOxidizableFamily(Block baseBlock, String baseName, Block.Settings settings, String namespace, BiFunction<Oxidizable.OxidationLevel, Block.Settings, Block> factory) {
        Map<Oxidizable.OxidationLevel, Block> unwaxedStates = new EnumMap<>(Oxidizable.OxidationLevel.class);
        Map<Oxidizable.OxidationLevel, Block> waxedStates   = new EnumMap<>(Oxidizable.OxidationLevel.class);

        for (Oxidizable.OxidationLevel level : List.of(Oxidizable.OxidationLevel.EXPOSED, Oxidizable.OxidationLevel.WEATHERED, Oxidizable.OxidationLevel.OXIDIZED)) {
            String name = switch (level) {
                case EXPOSED   -> "exposed_" + baseName;
                case WEATHERED -> "weathered_" + baseName;
                case OXIDIZED  -> "oxidized_" + baseName;
                default -> throw new IllegalStateException("Unexpected oxidation level: " + level);
            };

            Block block = Registry.register(
                    Registries.BLOCK,
                    Identifier.of(namespace, name),
                    factory.apply(level, Block.Settings.copy(baseBlock))
            );
            unwaxedStates.put(level, block);

            registerBlockItem(block, namespace, name, baseBlock);
            addEntities(block);
        }

        for (Oxidizable.OxidationLevel level : Oxidizable.OxidationLevel.values()) {
            String name = switch (level) {
                case UNAFFECTED -> "waxed_" + baseName;
                case EXPOSED   -> "waxed_exposed_" + baseName;
                case WEATHERED -> "waxed_weathered_" + baseName;
                case OXIDIZED  -> "waxed_oxidized_" + baseName;
            };

            Block block = Registry.register(
                    Registries.BLOCK,
                    Identifier.of(namespace, name),
                    new Block(Block.Settings.copy(baseBlock))
            );
            waxedStates.put(level, block);

            registerBlockItem(block, namespace, name, baseBlock);
            addEntities(block);
        }

        OxidizableBlocksRegistry.registerOxidizableBlockPair(baseBlock, unwaxedStates.get(Oxidizable.OxidationLevel.EXPOSED));
        OxidizableBlocksRegistry.registerOxidizableBlockPair(unwaxedStates.get(Oxidizable.OxidationLevel.EXPOSED), unwaxedStates.get(Oxidizable.OxidationLevel.WEATHERED));
        OxidizableBlocksRegistry.registerOxidizableBlockPair(unwaxedStates.get(Oxidizable.OxidationLevel.WEATHERED), unwaxedStates.get(Oxidizable.OxidationLevel.OXIDIZED));

        for (Oxidizable.OxidationLevel level : Oxidizable.OxidationLevel.values()) {
            Block unwaxed = (level == Oxidizable.OxidationLevel.UNAFFECTED)
                    ? baseBlock
                    : unwaxedStates.get(level);

            Block waxed = waxedStates.get(level);

            OxidizableBlocksRegistry.registerWaxableBlockPair(unwaxed, waxed);
        }
    }

    private static void registerBlockItem(Block block, String namespace, String name, Block baseBlock) {
        Identifier id = Identifier.of(namespace, name);
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

    public static Block getBlockByName(String name) {
        for (Block block : Registries.BLOCK) {
            Identifier id = Registries.BLOCK.getId(block);
            if (id.getPath().equals(name)) return block;
        }
        return Blocks.AIR;
    }

    public static void addEntities(Block block) {
        if (block.toString().contains("dispenser")) BlockEntityType.DISPENSER.addSupportedBlock(block);
        if (block.toString().contains("dropper")) BlockEntityType.DROPPER.addSupportedBlock(block);
        if (block.toString().contains("crafter")) BlockEntityType.CRAFTER.addSupportedBlock(block);
    }
}
