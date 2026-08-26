package com.azumoo.realgeology.debug;

import com.azumoo.realgeology.worldgen.GeologicalProvincesFeature;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Temporary in-world geological inspection tools.  The commands only replace
 * natural terrain blocks and retain the original state in memory; an orderly
 * Save & Quit restores every changed block before the world closes.
 */
public final class GeologyDebugCommands {
    private static final int DEFAULT_WIDTH = 50;
    private static final int TRENCH_DEPTH = 6;
    // Deliberately modest: block updates are the expensive part. A default
    // 50 m section takes several seconds to appear, but never becomes a
    // WorldEdit-style half-million-block single-tick freeze.
    private static final int BLOCKS_PER_TICK = 384;
    private static final Map<ServerLevel, LinkedHashMap<BlockPos, BlockState>> ORIGINALS = new IdentityHashMap<>();
    private static final Deque<DebugJob> JOBS = new ArrayDeque<>();

    private GeologyDebugCommands() { }

    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("realgeology")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("debug")
                        .then(Commands.literal("section")
                                .executes(context -> makeSection(context.getSource(), DEFAULT_WIDTH, false))
                                .then(Commands.argument("width", IntegerArgumentType.integer(12, 50))
                                        .executes(context -> makeSection(context.getSource(), IntegerArgumentType.getInteger(context, "width"), false))))
                        .then(Commands.literal("ores")
                                .executes(context -> makeSection(context.getSource(), DEFAULT_WIDTH, true))
                                .then(Commands.argument("width", IntegerArgumentType.integer(12, 50))
                                        .executes(context -> makeSection(context.getSource(), IntegerArgumentType.getInteger(context, "width"), true))))
                        .then(Commands.literal("clear")
                                .executes(context -> clear(context.getSource())))));
    }

    /** Restores active temporary cutaways on an orderly integrated/dedicated server shutdown. */
    public static void restoreOnShutdown(ServerStoppingEvent event) {
        JOBS.clear();
        for (ServerLevel level : event.getServer().getAllLevels()) restore(level);
        ORIGINALS.clear();
    }

    /** Performs a limited amount of cutaway work after normal server work. */
    public static void advanceJobs(ServerTickEvent.Post event) {
        if (!event.hasTime() || JOBS.isEmpty()) return;
        int remaining = BLOCKS_PER_TICK;
        Iterator<DebugJob> iterator = JOBS.iterator();
        while (iterator.hasNext() && remaining > 0) {
            DebugJob job = iterator.next();
            remaining -= job.process(remaining);
            if (job.complete()) {
                job.tellComplete();
                iterator.remove();
            }
        }
    }

    private static int makeSection(CommandSourceStack source, int width, boolean oreOnly) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        ServerLevel level = player.serverLevel();
        BlockPos playerPos = player.blockPosition();
        int normalX = player.getDirection().getStepX();
        int normalZ = player.getDirection().getStepZ();
        // Start a few blocks in front of the player. The long side is always
        // perpendicular to the viewing direction, leaving a readable rock face.
        int centreX = playerPos.getX() + normalX * 8;
        int centreZ = playerPos.getZ() + normalZ * 8;
        boolean wideAlongX = normalZ != 0;
        String mode = oreOnly ? "ore-only slice" : "geological cross-section";
        JOBS.addLast(new DebugJob(level, player.getUUID(), centreX, centreZ, normalX, normalZ, wideAlongX, width, oreOnly));
        source.sendSuccess(() -> Component.literal("Queued temporary " + width + " m " + mode
                + ". It is being cut gradually to avoid lag; use /realgeology debug clear, or Save & Quit to restore it."), false);
        return 1;
    }

    private static int clear(CommandSourceStack source) {
        ServerLevel level = source.getLevel();
        JOBS.removeIf(job -> job.level == level);
        int restored = restore(level);
        source.sendSuccess(() -> Component.literal("Restored " + restored + " temporary geology-debug blocks."), false);
        return restored;
    }

    private static int restore(ServerLevel level) {
        LinkedHashMap<BlockPos, BlockState> originals = ORIGINALS.remove(level);
        if (originals == null) return 0;
        for (Map.Entry<BlockPos, BlockState> entry : originals.entrySet()) level.setBlock(entry.getKey(), entry.getValue(), 2);
        return originals.size();
    }

    private static boolean isNaturalTerrain(BlockState state) {
        Block block = state.getBlock();
        if (block == Blocks.STONE || block == Blocks.DEEPSLATE || block == Blocks.GRANITE || block == Blocks.DIORITE
                || block == Blocks.ANDESITE || block == Blocks.TUFF || block == Blocks.BLACKSTONE || block == Blocks.BASALT
                || block == Blocks.SMOOTH_BASALT || block == Blocks.DIRT || block == Blocks.GRASS_BLOCK || block == Blocks.COARSE_DIRT
                || block == Blocks.ROOTED_DIRT || block == Blocks.GRAVEL || block == Blocks.SAND || block == Blocks.RED_SAND
                || block == Blocks.TERRACOTTA || block == Blocks.SANDSTONE || block == Blocks.RED_SANDSTONE) return true;
        ResourceLocation id = BuiltInRegistries.BLOCK.getKey(block);
        return id.getNamespace().equals("geostrata") && !isOre(state);
    }

    private static boolean isOre(BlockState state) {
        ResourceLocation id = BuiltInRegistries.BLOCK.getKey(state.getBlock());
        return id.getPath().endsWith("_ore") || id.getPath().contains("_ore_");
    }

    /** Stateful cursor: scans a full trench incrementally, never in one tick. */
    private static final class DebugJob {
        private final ServerLevel level;
        private final UUID playerId;
        private final int centreX, centreZ, normalX, normalZ, width;
        private final boolean wideAlongX, oreOnly;
        private final LinkedHashMap<BlockPos, BlockState> originals;
        private int lateral, inward, y;
        private int changed;
        private boolean complete;

        private DebugJob(ServerLevel level, UUID playerId, int centreX, int centreZ, int normalX, int normalZ,
                         boolean wideAlongX, int width, boolean oreOnly) {
            this.level = level;
            this.playerId = playerId;
            this.centreX = centreX;
            this.centreZ = centreZ;
            this.normalX = normalX;
            this.normalZ = normalZ;
            this.wideAlongX = wideAlongX;
            this.width = width;
            this.oreOnly = oreOnly;
            this.originals = ORIGINALS.computeIfAbsent(level, ignored -> new LinkedHashMap<>());
            this.lateral = -width / 2;
            this.y = level.getMinBuildHeight();
        }

        private int process(int budget) {
            int examined = 0;
            BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
            while (examined < budget && !complete) {
                int x = centreX + (wideAlongX ? lateral : normalX * inward);
                int z = centreZ + (wideAlongX ? normalZ * inward : lateral);
                int top = level.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, x, z) - 1;
                if (y <= top) {
                    pos.set(x, y++, z);
                    BlockState state = level.getBlockState(pos);
                    if (isNaturalTerrain(state) && (!oreOnly || !isOre(state))) {
                        originals.putIfAbsent(pos.immutable(), state);
                        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
                        changed++;
                    }
                    examined++;
                    continue;
                }
                y = level.getMinBuildHeight();
                if (++inward >= TRENCH_DEPTH) {
                    inward = 0;
                    if (++lateral >= (width + 1) / 2) complete = true;
                }
            }
            return examined;
        }

        private boolean complete() { return complete; }

        private void tellComplete() {
            ServerPlayer player = level.getServer().getPlayerList().getPlayer(playerId);
            if (player != null) player.sendSystemMessage(Component.literal("Real Geology debug "
                    + (oreOnly ? "ore-only slice" : "cross-section") + " complete (" + changed + " blocks hidden)."));
        }
    }
}
