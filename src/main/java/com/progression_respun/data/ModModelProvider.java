package com.progression_respun.data;

import com.progression_respun.ProgressionRespun;
import com.progression_respun.block.ModBlocks;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricModelProvider;
import net.minecraft.block.Block;
import net.minecraft.block.Waterloggable;
import net.minecraft.data.client.*;
import net.minecraft.item.Item;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.progression_respun.item.ModItems.*;
import static com.progression_respun.compat.GalosphereItems.*;
import static com.progression_respun.compat.EnderscapeItems.*;

public class ModModelProvider extends FabricModelProvider {
    public static final Map<Model, List<Item>> ITEM_MODEL_LISTS = Map.ofEntries(
            Map.entry(Models.GENERATED, List.of(
                    FLINT_BAR, COPPER_BAR, IRON_BAR, GOLD_BAR, DIAMOND_BAR, RAW_COPPER_BAR, RAW_IRON_BAR, RAW_GOLD_BAR
            )),
            Map.entry(Models.HANDHELD, List.of(
                    FLINT_SWORD, FLINT_AXE, FLINT_PICKAXE,
                    COPPER_SWORD, COPPER_AXE, COPPER_PICKAXE
            ))
    );
    public static final Map<Model, List<Identifier>> COMPAT_ITEM_MODEL_LISTS = Map.ofEntries(
            Map.entry(Models.GENERATED, List.of(
                    SILVER_BAR_ID, RAW_SILVER_BAR_ID,
                    SHADOLINE_BAR_ID, RAW_SHADOLINE_BAR_ID
            ))
    );
    public static final Map<Model, List<Block>> BLOCK_MODEL_LISTS = Map.ofEntries(
            Map.entry(Models.CUBE_ALL, List.of(
            ))
    );

    public ModModelProvider(FabricDataOutput output) {
        super(output);
    }

    @Override
    public void generateBlockStateModels(BlockStateModelGenerator generator) {
    }

    @Override
    public void generateItemModels(ItemModelGenerator generator) {
        for (var items : ITEM_MODEL_LISTS.entrySet()) {
            Model model = items.getKey();
            for (Item item : items.getValue()) {
                generator.register(item, model);
            }
        }

        for (var items : COMPAT_ITEM_MODEL_LISTS.entrySet()) {
            Model model = items.getKey();
            for (Identifier item : items.getValue()) {
                Identifier itemModelId = item.withPrefixedPath("item/");
                model.upload(itemModelId, TextureMap.layer0(itemModelId), generator.writer);
            }
        }
    }
}
