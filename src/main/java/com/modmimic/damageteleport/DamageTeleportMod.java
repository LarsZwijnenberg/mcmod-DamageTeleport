package com.modmimic.damageteleport;

import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.block.BlockState;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public final class DamageTeleportMod implements ModInitializer {
    public static final String MOD_ID = "damageteleport";

    @Override
    public void onInitialize() {
        ServerLivingEntityEvents.AFTER_DAMAGE.register((entity, source, baseDamageTaken, damageTaken, blocked) -> {
            if (!(entity instanceof ServerPlayerEntity player)) {
                return;
            }

            if (player.isSpectator() || player.isCreative()) {
                return;
            }

            attemptTeleport(player);
        });
    }

    private static void attemptTeleport(ServerPlayerEntity player) {
        ServerWorld world = player.getServerWorld();
        ThreadLocalRandom random = ThreadLocalRandom.current();

        for (int attempt = 0; attempt < 12; attempt++) {
            double distance = 5.0D + random.nextDouble() * 45.0D;
            double angle = random.nextDouble() * MathHelper.TAU;
            double offsetX = Math.cos(angle) * distance;
            double offsetZ = Math.sin(angle) * distance;
            int yOffset = random.nextInt(-4, 5);

            BlockPos.Mutable targetPos = BlockPos.ofFloored(
                player.getX() + offsetX,
                player.getY() + yOffset,
                player.getZ() + offsetZ
            ).mutableCopy();

            Optional<Vec3d> safePos = findSafeTeleportSpot(world, targetPos);
            if (safePos.isPresent()) {
                Vec3d target = safePos.get();
                player.teleport(world, target.x, target.y, target.z, player.getYaw(), player.getPitch());
                world.playSound(null, target.x, target.y, target.z, SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 1.0F, 1.0F);
                return;
            }
        }
    }

    private static Optional<Vec3d> findSafeTeleportSpot(ServerWorld world, BlockPos.Mutable basePos) {
        if (!world.isChunkLoaded(basePos)) {
            return Optional.empty();
        }

        int minY = world.getBottomY() + 1;
        int maxY = world.getTopY() - 2;
        BlockPos.Mutable cursor = new BlockPos.Mutable();

        for (int yScan = 8; yScan >= -8; yScan--) {
            int candidateY = MathHelper.clamp(basePos.getY() + yScan, minY, maxY);
            cursor.set(basePos.getX(), candidateY, basePos.getZ());

            if (!world.isAir(cursor) || !world.isAir(cursor.up())) {
                continue;
            }

            BlockPos belowPos = cursor.down();
            BlockState below = world.getBlockState(belowPos);
            if (!below.getCollisionShape(world, belowPos).isEmpty()) {
                return Optional.of(new Vec3d(cursor.getX() + 0.5D, cursor.getY(), cursor.getZ() + 0.5D));
            }
        }

        return Optional.empty();
    }
}
