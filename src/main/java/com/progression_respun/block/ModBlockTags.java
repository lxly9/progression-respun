package com.progression_respun.block;

import com.progression_respun.ProgressionRespun;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;

public class ModBlockTags {
    public static final TagKey<Block> INCORRECT_FOR_FLINT_TOOL = block("incorrect_for_flint_tool");

    private static TagKey<Block> block(String name) {
        return TagKey.of(RegistryKeys.BLOCK, ProgressionRespun.id(name));
    }
}
