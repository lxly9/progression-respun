package com.progression_respun.block;

import com.progression_respun.ProgressionRespun;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;

public class ModBlocks {
    public static final Block FLINT_PEBBLES = register(new PebblesBlock(AbstractBlock.Settings.create()
            .sounds(BlockSoundGroup.STONE).hardness(0.0f).noCollision()), "flint_pebbles", false);

    public static final Block CRUCIBLE_BLOCK = register(new CrucibleBlock(AbstractBlock.Settings.create().nonOpaque().hardness(1.0F)), "copper_crucible", true);

    private static Block register(Block block, String name, boolean hasItem) {
        Identifier id = ProgressionRespun.id(name);
        if (hasItem) {
            Item item = new BlockItem(block, new Item.Settings());
            Registry.register(Registries.ITEM, id, item);
        }
        return Registry.register(Registries.BLOCK, id, block);
    }

    public static void initialize() {
    }
}
