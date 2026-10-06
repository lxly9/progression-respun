package com.gayasslily.progression_respun.mixin;

import com.gayasslily.progression_respun.block.*;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.block.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Blocks.class)
public class BlocksMixin {

    @WrapOperation(method = "<clinit>", at = @At(value = "NEW", target = "(Lnet/minecraft/block/AbstractBlock$Settings;)Lnet/minecraft/block/DispenserBlock;"))
    private static DispenserBlock thumbandthicket$changeDispenserConstructor(AbstractBlock.Settings settings, Operation<Block> original) {
        return new OxidizableDispenserBlock(Oxidizable.OxidationLevel.UNAFFECTED, settings);
    }

    @WrapOperation(method = "<clinit>", at = @At(value = "NEW", target = "(Lnet/minecraft/block/AbstractBlock$Settings;)Lnet/minecraft/block/DropperBlock;"))
    private static DropperBlock thumbandthicket$changeDropperConstructor(AbstractBlock.Settings settings, Operation<Block> original) {
        return new OxidizableDropperBlock(Oxidizable.OxidationLevel.UNAFFECTED, settings);
    }

    @WrapOperation(method = "<clinit>", at = @At(value = "NEW", target = "(Lnet/minecraft/block/AbstractBlock$Settings;)Lnet/minecraft/block/ObserverBlock;"))
    private static ObserverBlock thumbandthicket$changeObserverConstructor(AbstractBlock.Settings settings, Operation<Block> original) {
        return new OxidizableObserverBlock(Oxidizable.OxidationLevel.UNAFFECTED, settings);
    }

    @WrapOperation(method = "<clinit>", at = @At(value = "NEW", target = "(Lnet/minecraft/block/AbstractBlock$Settings;)Lnet/minecraft/block/CrafterBlock;"))
    private static CrafterBlock thumbandthicket$changeCrafterConstructor(AbstractBlock.Settings settings, Operation<Block> original) {
        return new OxidizableCrafterBlock(Oxidizable.OxidationLevel.UNAFFECTED, settings);
    }

    @WrapOperation(method = "<clinit>", at = @At(value = "NEW", target = "(Lnet/minecraft/block/AbstractBlock$Settings;)Lnet/minecraft/block/PistonHeadBlock;"))
    private static PistonHeadBlock thumbandthicket$changePistonConstructor(AbstractBlock.Settings settings, Operation<Block> original) {
        return new OxidizablePistonHeadBlock(Oxidizable.OxidationLevel.UNAFFECTED, settings);
    }

    @WrapOperation(method = "createPistonBlock", at = @At(value = "NEW", target = "(ZLnet/minecraft/block/AbstractBlock$Settings;)Lnet/minecraft/block/PistonBlock;"))
    private static PistonBlock thumbandthicket$changePistonMethodConstructor(boolean sticky, AbstractBlock.Settings settings, Operation<PistonBlock> original) {
        return new OxidizablePistonBlock(Oxidizable.OxidationLevel.UNAFFECTED, sticky, settings);
    }

    @WrapOperation(method = "<clinit>", at = @At(value = "NEW", target = "(Lnet/minecraft/block/AbstractBlock$Settings;)Lnet/minecraft/block/PistonExtensionBlock;"))
    private static PistonExtensionBlock thumbandthicket$changePistonExtensionMethodConstructor(AbstractBlock.Settings settings, Operation<Block> original) {
        return new OxidizablePistonExtensionBlock(Oxidizable.OxidationLevel.UNAFFECTED, settings);
    }
}
