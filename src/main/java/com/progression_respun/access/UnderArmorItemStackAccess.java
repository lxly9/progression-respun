package com.progression_respun.access;

import net.minecraft.item.ItemStack;

public interface UnderArmorItemStackAccess {
    ItemStack progression_respun$getHeadUnderArmor();
    ItemStack progression_respun$getChestUnderArmor();
    ItemStack progression_respun$getLegUnderArmor();
    ItemStack progression_respun$getFeetUnderArmor();
    void progression_respun$setHeadUnderArmor(ItemStack stack);
    void progression_respun$setChestUnderArmor(ItemStack stack);
    void progression_respun$setLegUnderArmor(ItemStack stack);
    void progression_respun$setFeetUnderArmor(ItemStack stack);
}
