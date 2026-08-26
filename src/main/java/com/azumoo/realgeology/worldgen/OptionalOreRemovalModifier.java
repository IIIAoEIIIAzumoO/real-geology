package com.azumoo.realgeology.worldgen;

import com.azumoo.realgeology.RealGeology;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.common.world.ModifiableBiomeInfo;

import java.util.Set;

/** Removes only placed features that are actually registered by the active mod set. */
public record OptionalOreRemovalModifier(HolderSet<Biome> biomes) implements BiomeModifier {
    public static final MapCodec<OptionalOreRemovalModifier> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Biome.LIST_CODEC.fieldOf("biomes").forGetter(OptionalOreRemovalModifier::biomes)
    ).apply(instance, OptionalOreRemovalModifier::new));

    private static final Set<ResourceLocation> COMPETING_OVERWORLD_ORES = Set.of(
            id("mekanism:ore_fluorite_buried"), id("mekanism:ore_fluorite_normal"), id("mekanism:ore_lead_normal"),
            id("mekanism:ore_osmium_upper"), id("mekanism:ore_osmium_middle"), id("mekanism:ore_osmium_small"),
            id("mekanism:ore_tin_large"), id("mekanism:ore_tin_small"), id("mekanism:ore_uranium_buried"), id("mekanism:ore_uranium_small"),
            id("mekanism:salt"),
            id("create:zinc_ore"), id("create:striated_ores_overworld"),
            id("tfmg:lead_ore"), id("tfmg:lithium_ore"), id("tfmg:nickel_ore"), id("tfmg:tfmg_striated_ores_overworld"),
            id("railcraft:lead_ore"), id("railcraft:nickel_ore_middle"), id("railcraft:nickel_ore_small"), id("railcraft:nickel_ore_upper"),
            id("railcraft:saltpeter"), id("railcraft:silver_ore"), id("railcraft:silver_ore_lower"),
            id("railcraft:sulfur_ore_lower"), id("railcraft:sulfur_ore_upper"), id("railcraft:tin_ore_large"),
            id("railcraft:tin_ore_small"), id("railcraft:zinc_ore"),
            // Railcraft injects a generic stone body into forest biomes. It
            // breaks the province layers just as much as a foreign ore vein,
            // so the geology overhaul owns this placement too.
            id("railcraft:quarried_stone"), id("railcraft:firestone"),
            // Immersive Engineering's deposits are useful in a normal pack,
            // but duplicate materials and bypass the canonical strata here.
            id("immersiveengineering:bauxite"), id("immersiveengineering:deep_nickel"),
            id("immersiveengineering:lead"), id("immersiveengineering:mineral_veins"),
            id("immersiveengineering:nickel"), id("immersiveengineering:silver"),
            id("immersiveengineering:uranium")
    );

    @Override
    public void modify(Holder<Biome> biome, Phase phase, ModifiableBiomeInfo.BiomeInfo.Builder builder) {
        if (phase != Phase.REMOVE || !biomes.contains(biome)) return;
        builder.getGenerationSettings().getFeatures(GenerationStep.Decoration.UNDERGROUND_ORES).removeIf(feature ->
                feature.unwrapKey().map(key -> isCompetingFeature(key.location())).orElse(false));
    }

    @Override
    public MapCodec<? extends BiomeModifier> codec() {
        return RealGeology.OPTIONAL_ORE_REMOVER.get();
    }

    private static ResourceLocation id(String value) {
        return ResourceLocation.parse(value);
    }

    private static boolean isCompetingFeature(ResourceLocation id) {
        if (COMPETING_OVERWORLD_ORES.contains(id)) return true;
        // GeoStrata's own worldgen is excellent in a normal pack, but every
        // one of its stone/ore features would cut a later blob through our
        // ordered formations.  Retain GeoStrata as the block library while
        // Real Geology becomes the sole authority for Overworld placement.
        if (id.getNamespace().equals("geostrata")
                && (id.getPath().endsWith("_stone") || id.getPath().endsWith("_ore"))) return true;
        // Vanilla's placed ore features use the ore_* naming family. Keep
        // caves, geodes, structures, and Terralith's vegetation/surface work
        // intact, but remove generic blobs that would fracture the formations.
        return id.getNamespace().equals("minecraft") && id.getPath().startsWith("ore_");
    }
}
