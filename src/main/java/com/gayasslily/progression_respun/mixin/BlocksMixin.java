package com.gayasslily.progression_respun.mixin;

import com.gayasslily.progression_respun.block.OxidizableCrafterBlock;
import com.gayasslily.progression_respun.block.OxidizableDispenserBlock;
import com.gayasslily.progression_respun.block.OxidizableDropperBlock;
import com.gayasslily.progression_respun.block.OxidizableObserverBlock;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.block.*;
import net.minecraft.block.enums.NoteBlockInstrument;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Blocks.class)
public class BlocksMixin {

    @WrapOperation(method = "<clinit>", at = @At(value = "NEW", target = "(Lnet/minecraft/block/AbstractBlock$Settings;)Lnet/minecraft/block/DispenserBlock;"))
    private static DispenserBlock thumbandthicket$changeDispenserConstructor(AbstractBlock.Settings settings, Operation<Block> original) {
        return new OxidizableDispenserBlock(Oxidizable.OxidationLevel.UNAFFECTED, AbstractBlock.Settings.create().mapColor(MapColor.STONE_GRAY).instrument(NoteBlockInstrument.BASEDRUM).requiresTool().strength(3.5f));
    }

    @WrapOperation(method = "<clinit>", at = @At(value = "NEW", target = "(Lnet/minecraft/block/AbstractBlock$Settings;)Lnet/minecraft/block/DropperBlock;"))
    private static DropperBlock thumbandthicket$changeDropperConstructor(AbstractBlock.Settings settings, Operation<Block> original) {
        return new OxidizableDropperBlock(Oxidizable.OxidationLevel.UNAFFECTED, AbstractBlock.Settings.create().mapColor(MapColor.STONE_GRAY).instrument(NoteBlockInstrument.BASEDRUM).requiresTool().strength(3.5f));
    }

    @WrapOperation(method = "<clinit>", at = @At(value = "NEW", target = "(Lnet/minecraft/block/AbstractBlock$Settings;)Lnet/minecraft/block/ObserverBlock;"))
    private static ObserverBlock thumbandthicket$changeObserverConstructor(AbstractBlock.Settings settings, Operation<Block> original) {
        return new OxidizableObserverBlock(Oxidizable.OxidationLevel.UNAFFECTED, AbstractBlock.Settings.create().mapColor(MapColor.STONE_GRAY).instrument(NoteBlockInstrument.BASEDRUM).strength(3.0f).requiresTool().solidBlock(Blocks::never));
    }

    @WrapOperation(method = "<clinit>", at = @At(value = "NEW", target = "(Lnet/minecraft/block/AbstractBlock$Settings;)Lnet/minecraft/block/CrafterBlock;"))
    private static CrafterBlock thumbandthicket$changeCrafterConstructor(AbstractBlock.Settings settings, Operation<Block> original) {
        return new OxidizableCrafterBlock(Oxidizable.OxidationLevel.UNAFFECTED, AbstractBlock.Settings.create().mapColor(MapColor.STONE_GRAY).strength(1.5f, 3.5f));
    }
}
