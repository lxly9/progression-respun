package com.gayasslily.progression_respun.block;

import com.gayasslily.progression_respun.ProgressionRespun;
import com.gayasslily.progression_respun.util.OxidizableUtil;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.block.*;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;


public class ModBlocks {
    public static final Block FLINT_PEBBLES = register(new PebblesBlock(AbstractBlock.Settings.create().sounds(BlockSoundGroup.STONE).hardness(0.0f).noCollision()), "flint_pebbles", false);
    public static final Block CRUCIBLE_BLOCK = register(new CrucibleBlock(AbstractBlock.Settings.create().nonOpaque().hardness(3.0F).sounds(BlockSoundGroup.COPPER_BULB).luminance(CrucibleBlock::getLuminance)), "copper_crucible", true);
    public static final Block GLOW_TORCH = register(new TorchBlock(ParticleTypes.GLOW, AbstractBlock.Settings.copy(Blocks.TORCH)), "glow_torch", true);

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
        OxidizableUtil.registerOxidizableFamily(Blocks.DROPPER, "dropper", OxidizableDropperBlock::new, true);
        OxidizableUtil.registerOxidizableFamily(Blocks.DISPENSER, "dispenser", OxidizableDispenserBlock::new, true);
        OxidizableUtil.registerOxidizableFamily(Blocks.OBSERVER, "observer", OxidizableObserverBlock::new, true);
        OxidizableUtil.registerOxidizableFamily(Blocks.CRAFTER, "crafter", OxidizableCrafterBlock::new, true);
        OxidizableUtil.registerOxidizablePistonFamily(Blocks.PISTON, "piston", OxidizablePistonBlock::new, true);
        OxidizableUtil.registerOxidizablePistonFamily(Blocks.STICKY_PISTON, "sticky_piston", OxidizablePistonBlock::new, true);
        OxidizableUtil.registerOxidizableFamily(Blocks.PISTON_HEAD, "piston_head", OxidizablePistonHeadBlock::new, false);
        OxidizableUtil.registerOxidizableFamily(Blocks.MOVING_PISTON, "moving_piston", OxidizablePistonExtensionBlock::new, false);
    }
}
