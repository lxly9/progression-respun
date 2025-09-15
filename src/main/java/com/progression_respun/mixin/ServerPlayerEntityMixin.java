package com.progression_respun.mixin;

import com.mojang.datafixers.util.Either;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Unit;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.LightType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin {

    @Shadow public abstract ServerWorld getServerWorld();

    @Inject(method = "trySleep", at = @At("HEAD"), cancellable = true)
    private void isCovered(BlockPos pos, CallbackInfoReturnable<Either<PlayerEntity.SleepFailureReason, Unit>> cir) {
        ServerWorld serverWorld = this.getServerWorld();
        PlayerEntity player = (PlayerEntity)(Object) this;
        int lightLevel = serverWorld.getChunkManager().getLightingProvider().get(LightType.SKY).getLightLevel(pos);
        if (lightLevel >= 13) {
            cir.setReturnValue(Either.left(PlayerEntity.SleepFailureReason.NOT_POSSIBLE_HERE));
            player.sendMessage(Text.translatable("block.minecraft.bed.not_possible_here"), true);
            cir.cancel();
        }
    }
}
