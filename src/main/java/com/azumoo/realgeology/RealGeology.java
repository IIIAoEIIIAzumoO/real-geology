package com.azumoo.realgeology;

import com.azumoo.realgeology.block.HostedOreBlock;
import com.azumoo.realgeology.debug.GeologyDebugCommands;
import com.azumoo.realgeology.tools.GeologyPreviewExport;
import com.azumoo.realgeology.worldgen.GeologicalProvincesFeature;
import com.azumoo.realgeology.worldgen.OptionalOreRemovalModifier;
import com.azumoo.realgeology.worldgen.TestWorldLandSpawn;
import com.azumoo.realgeology.worldgen.VolcanicRockSanitizerFeature;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
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
import java.util.LinkedHashMap;
import java.util.Map;

@Mod(RealGeology.MODID)
public final class RealGeology {
    public static final String MODID = "realgeology";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(Registries.FEATURE, MODID);
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(Registries.BLOCK, MODID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, MODID);
    public static final DeferredRegister<MapCodec<? extends BiomeModifier>> BIOME_MODIFIERS =
            DeferredRegister.create(NeoForgeRegistries.Keys.BIOME_MODIFIER_SERIALIZERS, MODID);
    public static final DeferredHolder<Feature<?>, GeologicalProvincesFeature> GEOLOGICAL_PROVINCES = FEATURES.register(
            "geological_provinces", () -> new GeologicalProvincesFeature(NoneFeatureConfiguration.CODEC));
    public static final DeferredHolder<Feature<?>, VolcanicRockSanitizerFeature> VOLCANIC_ROCK_SANITIZER = FEATURES.register(
            "volcanic_rock_sanitizer", () -> new VolcanicRockSanitizerFeature(NoneFeatureConfiguration.CODEC));
    public static final Supplier<MapCodec<OptionalOreRemovalModifier>> OPTIONAL_ORE_REMOVER =
            BIOME_MODIFIERS.register("optional_ore_remover", () -> OptionalOreRemovalModifier.CODEC);
    private static final Map<String, DeferredHolder<Block, HostedOreBlock>> HOSTED_ORES = new LinkedHashMap<>();
    public static final DeferredHolder<Block, Block> KIMBERLITE = BLOCKS.register("kimberlite", () ->
            new Block(BlockBehaviour.Properties.of().strength(3.4f, 3.2f).requiresCorrectToolForDrops()));

    static {
        for (String material : new String[]{
                // These IDs intentionally remain the industrial material
                // names used by recipes and c: tags.  Their displayed names
                // identify the real mineral species instead.
                "coal", "iron", "copper", "gold", "diamond", "lapis", "emerald", "redstone",
                "tin", "lead", "zinc", "nickel", "lithium", "uranium", "osmium", "fluorite",
                "silver", "sulfur", "saltpeter", "galena", "bauxite", "lignite"
        }) HOSTED_ORES.put(material, registerHostedOre(material));
    }

    public RealGeology(IEventBus modBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.COMMON, RealGeologyConfig.SPEC);
        FEATURES.register(modBus);
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        BIOME_MODIFIERS.register(modBus);
        NeoForge.EVENT_BUS.addListener(GeologyDebugCommands::register);
        NeoForge.EVENT_BUS.addListener(GeologyDebugCommands::restoreOnShutdown);
        NeoForge.EVENT_BUS.addListener(GeologyDebugCommands::advanceJobs);
        NeoForge.EVENT_BUS.addListener(TestWorldLandSpawn::chooseLandSpawn);
        NeoForge.EVENT_BUS.addListener(GeologyPreviewExport::onServerStarted);
        LOGGER.info("Real Geology registered");
    }

    private static DeferredHolder<Block, HostedOreBlock> registerHostedOre(String material) {
        DeferredHolder<Block, HostedOreBlock> block = BLOCKS.register(material + "_ore", () ->
                new HostedOreBlock(BlockBehaviour.Properties.of().strength(3.2f, 3.0f).requiresCorrectToolForDrops()));
        ITEMS.register(material + "_ore", () -> new BlockItem(block.get(), new Item.Properties()));
        return block;
    }

    static {
        ITEMS.register("kimberlite", () -> new BlockItem(KIMBERLITE.get(), new Item.Properties()));
    }

    /** Canonical worldgen block: its state keeps the real host rock visible. */
    public static Block hostedOre(String material) {
        DeferredHolder<Block, HostedOreBlock> block = HOSTED_ORES.get(material);
        return block == null ? null : block.get();
    }
}
