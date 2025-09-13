package com.progression_respun.mixin.mobAttributionScaling;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ServerWorldAccess;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.progression_respun.ProgressionRespun.registerMobAttributes;

@Mixin(StructureTemplate.class)
public class StructureTemplateMixin {

    @Inject(method = "spawnEntities",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/mob/MobEntity;initialize(Lnet/minecraft/world/ServerWorldAccess;Lnet/minecraft/world/LocalDifficulty;Lnet/minecraft/entity/SpawnReason;Lnet/minecraft/entity/EntityData;)Lnet/minecraft/entity/EntityData;",
                    opcode = Opcodes.PUTFIELD,
                    shift = At.Shift.BEFORE
            )
    )
    private static <T extends MobEntity> void modifyAttributes(ServerWorldAccess worldAccess, BlockPos pos, BlockMirror mirror, BlockRotation rotation, BlockPos pivot, BlockBox area, boolean initializeMobs, CallbackInfo ci, @Local T mobEntity) {

        if (mobEntity != null && worldAccess instanceof ServerWorld serverWorld) {
            registerMobAttributes(serverWorld, pos, mobEntity);
        }
    }
}
