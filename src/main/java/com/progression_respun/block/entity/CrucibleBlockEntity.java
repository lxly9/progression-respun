package com.progression_respun.block.entity;

import com.progression_respun.block.CrucibleBlock;
import com.progression_respun.recipe.CrucibleRecipe;
import com.progression_respun.recipe.CrucibleRecipeInput;
import com.progression_respun.recipe.ModRecipes;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class CrucibleBlockEntity extends BlockEntity implements ImplementedInventory {
    private final DefaultedList<ItemStack> inventory = DefaultedList.ofSize(2, ItemStack.EMPTY);

    private static final int INPUT_SLOT = 0;
    private static final int OUTPUT_SLOT = 1;

    protected final PropertyDelegate propertyDelegate;
    private int progress= 0;
    private int maxProgress= 600;
    public float storedExperience = 0f;


    public CrucibleBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CRUCIBLE_BLOCK_ENTITY, pos, state);

        this.propertyDelegate = new PropertyDelegate() {
            @Override
            public int get(int index) {
                return switch (index) {
                    case 0 -> CrucibleBlockEntity.this.progress;
                    case 1 -> CrucibleBlockEntity.this.maxProgress;
                    default -> 0;
                };
            }

            @Override
            public void set(int index, int value) {
                switch (index) {
                    case 0:
                        CrucibleBlockEntity.this.progress = value;
                        break;
                    case 1:
                        CrucibleBlockEntity.this.maxProgress = value;
                }
            }

            @Override
            public int size() {return 2;}
        };
    }

    @Override
    public DefaultedList<ItemStack> getItems() {
        return inventory;
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.writeNbt(nbt, registryLookup);
        Inventories.writeNbt(nbt, inventory, registryLookup);
        nbt.putInt("CrucibleProgress", progress);
        nbt.putInt("CrucibleMaxProgress", maxProgress);
        nbt.putFloat("StoredExperience", storedExperience);
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        Inventories.readNbt(nbt, inventory, registryLookup);
        progress = nbt.getInt("CrucibleProgress");
        maxProgress = nbt.getInt("CrucibleMaxProgress");
        storedExperience = nbt.getFloat("StoredExperience");
        super.readNbt(nbt, registryLookup);
    }

    @Nullable
    @Override
    public Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registryLookup) {
        return createNbt(registryLookup);
    }

    public void tick(World world, BlockPos pos, BlockState state) {
        if (hasCrucibleRecipe() && state.get(CrucibleBlock.HEATED)) {
            increaseSmeltingProgress();
            markDirty(world, pos, state);
            if (hasSmeltingFinished()) {
                smeltItem();
                resetProgress();
            }
        } else {
            resetProgress();
        }
    }

    private void smeltItem() {
        Optional<RecipeEntry<CrucibleRecipe>> recipe = getCurrentRecipe();

        ItemStack output = recipe.get().value().output();
        this.removeStack(INPUT_SLOT, 1);
        this.setStack(OUTPUT_SLOT, new ItemStack(output.getItem(), output.getCount() + this.getStack(OUTPUT_SLOT).getCount()));
        this.storedExperience += recipe.get().value().experience();
    }

    private void resetProgress() {
        this.progress = 0;
        this.maxProgress = 600;
    }

    private boolean hasSmeltingFinished() {
        return this.progress >= this.maxProgress;
    }

    private void increaseSmeltingProgress() {
        this.progress++;
    }

    private boolean hasCrucibleRecipe() {
        Optional<RecipeEntry<CrucibleRecipe>> recipe = getCurrentRecipe();
        if (recipe.isEmpty()) {
            return false;
        }

        ItemStack output = recipe.get().value().output();
        return canInsertAmountIntoOutputSlot(output.getCount()) && canInsertItemIntoOutputSlot(output);
    }

    private Optional<RecipeEntry<CrucibleRecipe>> getCurrentRecipe() {
        assert this.getWorld() != null;
        return this.getWorld().getRecipeManager().getFirstMatch(ModRecipes.CRUCIBLE_RECIPE_TYPE, new CrucibleRecipeInput(inventory.getFirst()), this.getWorld());
    }

    private boolean canInsertItemIntoOutputSlot(ItemStack output) {
        return this.getStack(OUTPUT_SLOT).isEmpty() || this.getStack(OUTPUT_SLOT).getItem() == output.getItem();
    }

    private boolean canInsertAmountIntoOutputSlot(int count) {
        int maxCount = this.getStack(OUTPUT_SLOT).isEmpty() ? 64 : this.getStack(OUTPUT_SLOT).getMaxCount();
        int currentCount = this.getStack(OUTPUT_SLOT).getCount();

        return maxCount >= currentCount + count;
    }

    public float getStoredExperience() {
        return storedExperience;
    }

}
