package com.azumoo.realgeology.worldgen;

import com.azumoo.realgeology.RealGeology;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.common.world.ModifiableBiomeInfo;
import com.azumoo.realgeology.compat.GameCompat;

/** Removes only placed features that are actually registered by the active mod set. */
public record OptionalOreRemovalModifier(HolderSet<Biome> biomes) implements BiomeModifier {
    public static final MapCodec<OptionalOreRemovalModifier> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Biome.LIST_CODEC.fieldOf("biomes").forGetter(OptionalOreRemovalModifier::biomes)
    ).apply(instance, OptionalOreRemovalModifier::new));

    @Override
    public void modify(Holder<Biome> biome, Phase phase, ModifiableBiomeInfo.BiomeInfo.Builder builder) {
        if (phase != Phase.REMOVE || !biomes.contains(biome)) return;
        builder.getGenerationSettings().getFeatures(GenerationStep.Decoration.UNDERGROUND_ORES).removeIf(feature ->
                feature.unwrapKey().map(key -> GameCompat.isCompetingFeatureId(GameCompat.placedFeatureKeyId(key))).orElse(false));
    }

    @Override
    public MapCodec<? extends BiomeModifier> codec() {
        return RealGeology.OPTIONAL_ORE_REMOVER.get();
    }
}
