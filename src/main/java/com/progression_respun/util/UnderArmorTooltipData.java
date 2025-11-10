package com.progression_respun.util;

import com.progression_respun.component.type.UnderArmorContentsComponent;
import net.minecraft.item.tooltip.TooltipData;

public record UnderArmorTooltipData(UnderArmorContentsComponent contents) implements TooltipData
{
}