package com.azumoo.realgeology.worldgen;

import com.azumoo.realgeology.RealGeologyConfig;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;

/**
 * Re-strips fluids (and any stray blocks) one tick after chunk load so flowing
 * water/lava from neighbours cannot re-enter the cut zone after worldgen.
 */
public final class DebugCutawayPostGen {
    private static final int FLUID_RESTRIP_DELAY_TICKS = 2;
    private static final Deque<Pending> PENDING = new ArrayDeque<>();

    private DebugCutawayPostGen() { }

    public static void onChunkLoad(ChunkEvent.Load event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        if (!level.dimension().equals(Level.OVERWORLD)) return;
        if (!RealGeologyConfig.worldgenDebugMode().isActive()) return;
        LevelChunk chunk = (LevelChunk) event.getChunk();
        int dueTick = level.getServer().getTickCount() + FLUID_RESTRIP_DELAY_TICKS;
        PENDING.addLast(new Pending(level, chunk.getPos().getMinBlockX(), chunk.getPos().getMinBlockZ(), dueTick));
    }

    public static void onServerTick(ServerTickEvent.Post event) {
        if (PENDING.isEmpty()) return;
        int now = event.getServer().getTickCount();
        Iterator<Pending> iterator = PENDING.iterator();
        while (iterator.hasNext()) {
            Pending pending = iterator.next();
            if (now < pending.dueTick) continue;
            if (!pending.level.hasChunk(pending.minX >> 4, pending.minZ >> 4)) {
                iterator.remove();
                continue;
            }
            DebugCutawaySanitizer.sanitizeChunk(pending.level, pending.origin(), 3);
            iterator.remove();
        }
    }

    private record Pending(ServerLevel level, int minX, int minZ, int dueTick) {
        net.minecraft.core.BlockPos origin() {
            return new net.minecraft.core.BlockPos(minX, 0, minZ);
        }
    }
}
