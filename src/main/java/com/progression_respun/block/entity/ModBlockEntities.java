package com.progression_respun.block.entity;

import com.progression_respun.block.ModBlocks;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModBlockEntities {
    public static BlockEntityType<CrucibleBlockEntity> CRUCIBLE_BLOCK_ENTITY;

    public static void register() {
        CRUCIBLE_BLOCK_ENTITY = Registry.register(
            Registries.BLOCK_ENTITY_TYPE,
            Identifier.of("progression_respun", "crucible"),
            BlockEntityType.Builder.create(
                CrucibleBlockEntity::new,
                ModBlocks.CRUCIBLE_BLOCK
            ).build()
        );
    }
}