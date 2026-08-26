package com.azumoo.realgeology.worldgen;

import com.azumoo.realgeology.RealGeologyConfig;
import com.mojang.serialization.Codec;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/**
 * Final Overworld pass that airs out debug cut volumes after vegetation,
 * snow, structures, and other late features have been placed.
 */
public final class DebugCutawaySanitizerFeature extends Feature<NoneFeatureConfiguration> {
    public DebugCutawaySanitizerFeature(Codec<NoneFeatureConfiguration> codec) { super(codec); }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> ctx) {
        if (!RealGeologyConfig.worldgenDebugMode().isActive()) return false;
        DebugCutawaySanitizer.sanitizeChunk(ctx.level(), ctx.origin(), 2);
        return true;
    }
}
