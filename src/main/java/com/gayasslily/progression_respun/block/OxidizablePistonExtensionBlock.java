package com.gayasslily.progression_respun.block;

import net.minecraft.block.Oxidizable;
import net.minecraft.block.PistonExtensionBlock;

public class OxidizablePistonExtensionBlock extends PistonExtensionBlock implements Oxidizable {
    private final Oxidizable.OxidationLevel oxidationLevel;

    public OxidizablePistonExtensionBlock(OxidationLevel oxidationLevel, Settings settings) {
        super(settings);
        this.oxidationLevel = oxidationLevel;
    }

    @Override
    public OxidationLevel getDegradationLevel() {
        return oxidationLevel;
    }
}
