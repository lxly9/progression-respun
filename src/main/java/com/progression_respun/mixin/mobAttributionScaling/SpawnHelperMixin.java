package com.progression_respun.mixin.mobAttributionScaling;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.SpawnHelper;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


import static com.progression_respun.ProgressionRespun.registerMobAttributes;

@Mixin(SpawnHelper.class)
public class SpawnHelperMixin {

    @Inject(
            method = "spawnEntitiesInChunk(Lnet/minecraft/entity/SpawnGroup;Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/world/chunk/Chunk;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/world/SpawnHelper$Checker;Lnet/minecraft/world/SpawnHelper$Runner;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/SpawnHelper;isValidSpawn(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/entity/mob/MobEntity;D)Z",
                    shift = At.Shift.AFTER
            )
    )
    private static <T extends MobEntity> void modifyAttributes(SpawnGroup group, ServerWorld world, Chunk chunk, BlockPos pos, SpawnHelper.Checker checker, SpawnHelper.Runner runner, CallbackInfo ci, @Local T mobEntity) {

        if (mobEntity != null) {
            registerMobAttributes(world, pos, mobEntity);
        }
    }

    @Inject(
            method = "populateEntities",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/mob/MobEntity;canSpawn(Lnet/minecraft/world/WorldAccess;Lnet/minecraft/entity/SpawnReason;)Z",
                    opcode = Opcodes.PUTFIELD,
                    shift = At.Shift.BEFORE
            )
    )
    private static <T extends MobEntity> void modifyAttributes(ServerWorldAccess worldAccess, RegistryEntry<Biome> biomeEntry, ChunkPos chunkPos, Random random, CallbackInfo ci, @Local T mobEntity) {

        if (mobEntity != null && worldAccess instanceof ServerWorld serverWorld) {
            registerMobAttributes(serverWorld, chunkPos.getStartPos(), mobEntity);
        }
    }
}
