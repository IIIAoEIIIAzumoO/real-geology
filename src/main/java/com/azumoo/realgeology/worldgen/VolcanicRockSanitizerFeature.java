package com.azumoo.realgeology.worldgen;

import com.azumoo.realgeology.compat.GameCompat;
import com.azumoo.realgeology.compat.RockPalette;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/**
 * Overworld pass for terrain packs that add Nether blackstone after the
 * normal rock pass.  It deliberately leaves magma blocks alone: they are the
 * hot floor/ceiling surface of a lava feature, while the surrounding wall is
 * naturally basalt (or deeper diabase), not blackstone.
 * <p>
 * Debug cutaway clearing and fluid stripping are handled by
 * {@link DebugCutawaySanitizerFeature}, which runs later in the same step.
 */
public final class VolcanicRockSanitizerFeature extends Feature<NoneFeatureConfiguration> {
    public VolcanicRockSanitizerFeature(Codec<NoneFeatureConfiguration> codec) { super(codec); }

    @Override public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> ctx) {
        WorldGenLevel level = ctx.level();
        BlockPos origin = ctx.origin();
        int minX = origin.getX() & ~15, minZ = origin.getZ() & ~15;
        int minY = Math.max(GameCompat.minY(level), -64);
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int x = minX; x < minX + 16; x++) for (int z = minZ; z < minZ + 16; z++) {
            int top = level.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, x, z) - 1;
            for (int y = minY; y <= top; y++) {
                pos.set(x, y, z);
                Block block = level.getBlockState(pos).getBlock();
                if (block != Blocks.BLACKSTONE && block != Blocks.BASALT && block != Blocks.SMOOTH_BASALT) continue;
                level.setBlock(pos, RockPalette.rockState(y < -12 ? "diabase" : "basalt"), 2);
            }
        }
        return true;
    }
}
