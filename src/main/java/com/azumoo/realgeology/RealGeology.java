package com.azumoo.realgeology;

import com.azumoo.realgeology.compat.RegistryCompat;
import com.azumoo.realgeology.debug.GeologyDebugCommands;
import com.azumoo.realgeology.tools.GeologyPreviewExport;
import com.azumoo.realgeology.worldgen.DebugCutawayPostGen;
import com.azumoo.realgeology.worldgen.DebugCutawaySanitizerFeature;
import com.azumoo.realgeology.worldgen.GeologicalProvincesFeature;
import com.azumoo.realgeology.worldgen.OptionalOreRemovalModifier;
import com.azumoo.realgeology.worldgen.TestWorldLandSpawn;
import com.azumoo.realgeology.worldgen.VolcanicRockSanitizerFeature;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.slf4j.Logger;

import java.util.function.Supplier;

@Mod(RealGeology.MODID)
public final class RealGeology {
    public static final String MODID = "realgeology";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(Registries.FEATURE, MODID);
    public static final DeferredRegister<MapCodec<? extends BiomeModifier>> BIOME_MODIFIERS =
            DeferredRegister.create(NeoForgeRegistries.Keys.BIOME_MODIFIER_SERIALIZERS, MODID);
    public static final DeferredHolder<Feature<?>, GeologicalProvincesFeature> GEOLOGICAL_PROVINCES = FEATURES.register(
            "geological_provinces", () -> new GeologicalProvincesFeature(NoneFeatureConfiguration.CODEC));
    public static final DeferredHolder<Feature<?>, VolcanicRockSanitizerFeature> VOLCANIC_ROCK_SANITIZER = FEATURES.register(
            "volcanic_rock_sanitizer", () -> new VolcanicRockSanitizerFeature(NoneFeatureConfiguration.CODEC));
    public static final DeferredHolder<Feature<?>, DebugCutawaySanitizerFeature> DEBUG_CUTAWAY_SANITIZER = FEATURES.register(
            "debug_cutaway_sanitizer", () -> new DebugCutawaySanitizerFeature(NoneFeatureConfiguration.CODEC));
    public static final Supplier<MapCodec<OptionalOreRemovalModifier>> OPTIONAL_ORE_REMOVER =
            BIOME_MODIFIERS.register("optional_ore_remover", () -> OptionalOreRemovalModifier.CODEC);
    public static final DeferredHolder<Block, Block> KIMBERLITE = RegistryCompat.KIMBERLITE;

    public RealGeology(IEventBus modBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.COMMON, RealGeologyConfig.SPEC);
        FEATURES.register(modBus);
        RegistryCompat.register(modBus);
        BIOME_MODIFIERS.register(modBus);
        NeoForge.EVENT_BUS.addListener(GeologyDebugCommands::register);
        NeoForge.EVENT_BUS.addListener(GeologyDebugCommands::restoreOnShutdown);
        NeoForge.EVENT_BUS.addListener(GeologyDebugCommands::advanceJobs);
        NeoForge.EVENT_BUS.addListener(DebugCutawayPostGen::onChunkLoad);
        NeoForge.EVENT_BUS.addListener(DebugCutawayPostGen::onServerTick);
        NeoForge.EVENT_BUS.addListener(TestWorldLandSpawn::chooseLandSpawn);
        NeoForge.EVENT_BUS.addListener(GeologyPreviewExport::onServerStarted);
        LOGGER.info("Real Geology registered");
    }

    /** Canonical worldgen block: its state keeps the real host rock visible. */
    public static Block hostedOre(String material) {
        return RegistryCompat.hostedOre(material);
    }
}
