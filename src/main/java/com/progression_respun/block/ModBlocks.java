package com.progression_respun.block;

import com.progression_respun.ProgressionRespun;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.block.*;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;


public class ModBlocks {
    public static final Block FLINT_PEBBLES = register(new PebblesBlock(AbstractBlock.Settings.create().sounds(BlockSoundGroup.STONE).hardness(0.0f).noCollision()), "flint_pebbles", true);

    public static final Block CRUCIBLE_BLOCK = register(new CrucibleBlock(AbstractBlock.Settings.create().nonOpaque().hardness(3.0F).sounds(BlockSoundGroup.COPPER_BULB).luminance(CrucibleBlock::getLuminance)), "copper_crucible", true);

    private static Block register(Block block, String name, boolean hasItem) {
        Identifier id = ProgressionRespun.id(name);
        if (hasItem) {
            Item item = new BlockItem(block, new Item.Settings());
            Registry.register(Registries.ITEM, id, item);
        }
        return Registry.register(Registries.BLOCK, id, block);

    }

    public static void registerModBlocks() {

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL).register(entries -> {
            entries.addAfter(Blocks.BLAST_FURNACE, CRUCIBLE_BLOCK);
        });
    }

    public static void initialize() {
//        OxidizableUtil.registerOxidizableFamily(Blocks.DROPPER, "dropper", Block.Settings.copy(Blocks.DROPPER), "progression_respun", OxidizableDropperBlock::new);
//        OxidizableUtil.registerOxidizableFamily(Blocks.DISPENSER, "dispenser", Block.Settings.copy(Blocks.DISPENSER), "progression_respun", OxidizableDispenserBlock::new);
    }
}
