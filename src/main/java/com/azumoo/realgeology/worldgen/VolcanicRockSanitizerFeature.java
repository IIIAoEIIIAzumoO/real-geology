package com.azumoo.realgeology.worldgen;

import com.azumoo.realgeology.RealGeologyConfig;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/**
 * Final Overworld pass for terrain packs that add Nether blackstone after the
 * normal rock pass.  It deliberately leaves magma blocks alone: they are the
 * hot floor/ceiling surface of a lava feature, while the surrounding wall is
 * naturally basalt (or deeper diabase), not blackstone.
 */
public final class VolcanicRockSanitizerFeature extends Feature<NoneFeatureConfiguration> {
    public VolcanicRockSanitizerFeature(Codec<NoneFeatureConfiguration> codec) { super(codec); }

    @Override public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> ctx) {
        WorldGenLevel level = ctx.level();
        RealGeologyConfig.WorldgenDebugMode debugMode = RealGeologyConfig.worldgenDebugMode();
        BlockPos origin = ctx.origin();
        int minX = origin.getX() & ~15, minZ = origin.getZ() & ~15;
        int minY = Math.max(level.getMinBuildHeight(), -64);
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int x = minX; x < minX + 16; x++) for (int z = minZ; z < minZ + 16; z++) {
            if (debugMode.cutsAt(x, z)) {
                // Final worldgen pass: strip every terrain block and fluid from
                // the inspection corridor after springs/aquifers/features have
                // run. Bedrock remains as the visible floor. Ores mode keeps
                // only ore blocks floating in their true generated geometry.
                int top = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z) - 1;
                for (int y = minY; y <= top; y++) {
                    pos.set(x, y, z);
                    Block block = level.getBlockState(pos).getBlock();
                    if (block == Blocks.BEDROCK || (debugMode == RealGeologyConfig.WorldgenDebugMode.ORES && isOre(block))) continue;
                    level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
                }
                continue;
            }
            // Run to the natural terrain surface so exposed Terralith lava
            // cliffs cannot leave a strip of Minecraft blackstone/basalt above
            // the otherwise coherent GeoStrata volcanic host rock.
            int top = level.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, x, z) - 1;
            for (int y = minY; y <= top; y++) {
                pos.set(x, y, z);
                Block block = level.getBlockState(pos).getBlock();
                if (block != Blocks.BLACKSTONE && block != Blocks.BASALT && block != Blocks.SMOOTH_BASALT) continue;
                level.setBlock(pos, rock(y < -12 ? "diabase" : "basalt"), 2);
            }
        }
        return true;
    }

    private static boolean isOre(Block block) {
        ResourceLocation id = BuiltInRegistries.BLOCK.getKey(block);
        return id.getPath().endsWith("_ore") || id.getPath().contains("_ore_");
    }

    private static net.minecraft.world.level.block.state.BlockState rock(String name) {
        Block block = BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath("geostrata", name));
        return (block == Blocks.AIR ? Blocks.STONE : block).defaultBlockState();
    }
}
