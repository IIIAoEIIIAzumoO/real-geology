package com.azumoo.realgeology.worldgen;

import com.azumoo.realgeology.RealGeologyConfig;
import com.azumoo.realgeology.compat.GameCompat;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.block.CreateFluidSourceEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

/**
 * Keeps ponds and lakes visually intact in debug screenshot worlds by stopping
 * all fluid spread ticks. Fluids remain as static source blocks instead of
 * cascading over cut faces.
 */
public final class DebugFluidFreeze {
    private static final BoundingBox WORLD_FLUID_TICK_BOX = new BoundingBox(
            -30_000_000, -64, -30_000_000, 30_000_000, 319, 30_000_000);

    private DebugFluidFreeze() { }

    public static void onServerStarted(ServerStartedEvent event) {
        if (!RealGeologyConfig.worldgenDebugMode().isActive()) return;
        for (ServerLevel level : event.getServer().getAllLevels()) {
            if (!level.dimension().equals(Level.OVERWORLD)) continue;
            level.getGameRules().getRule(GameRules.RULE_RANDOMTICKING).set(0, event.getServer());
        }
    }

    public static void onLevelTickPre(LevelTickEvent.Pre event) {
        if (!RealGeologyConfig.worldgenDebugMode().isActive()) return;
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        if (!level.dimension().equals(Level.OVERWORLD)) return;
        level.getFluidTicks().clearArea(WORLD_FLUID_TICK_BOX);
    }

    public static void onFluidPlaceBlock(BlockEvent.FluidPlaceBlockEvent event) {
        if (!RealGeologyConfig.worldgenDebugMode().isActive()) return;
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        if (!level.dimension().equals(Level.OVERWORLD)) return;
        event.setCanceled(true);
    }

    public static void onCreateFluidSource(CreateFluidSourceEvent event) {
        if (!RealGeologyConfig.worldgenDebugMode().isActive()) return;
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        if (!level.dimension().equals(Level.OVERWORLD)) return;
        event.setCanConvert(false);
    }

    /** Convert flowing water/lava in a chunk to full source blocks for a stable look. */
    public static void stabilizeFluidsInChunk(Level level, net.minecraft.core.BlockPos origin) {
        if (!RealGeologyConfig.worldgenDebugMode().isActive()) return;
        int minX = origin.getX() & ~15;
        int minZ = origin.getZ() & ~15;
        int minY = GameCompat.minY(level);
        int maxY = GameCompat.maxY(level);
        net.minecraft.core.BlockPos.MutableBlockPos pos = new net.minecraft.core.BlockPos.MutableBlockPos();
        for (int x = minX; x <= minX + 15; x++) {
            for (int z = minZ; z <= minZ + 15; z++) {
                for (int y = minY; y <= maxY; y++) {
                    pos.set(x, y, z);
                    BlockState state = level.getBlockState(pos);
                    if (state.isAir() || state.getFluidState().isEmpty() || state.getFluidState().isSource()) continue;
                    BlockState stabilized = stabilize(state);
                    if (stabilized != null && !stabilized.equals(state)) {
                        level.setBlock(pos, stabilized, 2);
                    }
                }
            }
        }
    }

    private static BlockState stabilize(BlockState state) {
        if (state.getFluidState().is(Fluids.FLOWING_WATER)) {
            return Blocks.WATER.defaultBlockState();
        }
        if (state.getFluidState().is(Fluids.FLOWING_LAVA)) {
            return Blocks.LAVA.defaultBlockState();
        }
        return null;
    }
}
