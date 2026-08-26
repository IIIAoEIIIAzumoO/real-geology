package com.azumoo.realgeology.compat;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.level.biome.Biome;

/** Minecraft 1.21.1 API shims for shared worldgen sources. */
public final class GameCompat {
    private GameCompat() { }

    public static int minY(LevelHeightAccessor level) {
        return level.getMinBuildHeight();
    }

    public static int maxY(LevelHeightAccessor level) {
        return level.getMaxBuildHeight() - 1;
    }

    public static ResourceLocation id(String value) {
        return ResourceLocation.parse(value);
    }

    public static ResourceLocation id(String namespace, String path) {
        return ResourceLocation.fromNamespaceAndPath(namespace, path);
    }

    public static Block block(String id) {
        return BuiltInRegistries.BLOCK.get(ResourceLocation.parse(id));
    }

    public static Block block(String namespace, String path) {
        return BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath(namespace, path));
    }

    public static BlockState blockState(String id) {
        Block block = block(id);
        return block == null || block == Blocks.AIR ? null : block.defaultBlockState();
    }

    public static BlockState blockState(String namespace, String path) {
        Block block = block(namespace, path);
        return block == null || block == Blocks.AIR ? null : block.defaultBlockState();
    }

    public static String blockNamespace(Block block) {
        ResourceLocation key = BuiltInRegistries.BLOCK.getKey(block);
        return key == null ? "" : key.getNamespace();
    }

    public static String blockPath(Block block) {
        ResourceLocation key = BuiltInRegistries.BLOCK.getKey(block);
        return key == null ? "" : key.getPath();
    }

    public static String blockPath(BlockState state) {
        return blockPath(state.getBlock());
    }

    public static TagKey<Biome> biomeTag(String namespace, String path) {
        return TagKey.create(net.minecraft.core.registries.Registries.BIOME, id(namespace, path));
    }

    public static String biomePath(ResourceKey<Biome> key) {
        return key.location().getPath();
    }

    public static String placedFeatureKeyId(net.minecraft.resources.ResourceKey<PlacedFeature> key) {
        return key.location().toString();
    }

    public static boolean isCompetingFeatureId(String id) {
        ResourceLocation location = ResourceLocation.parse(id);
        if (COMPETING_OVERWORLD_ORES.contains(location)) return true;
        if (location.getNamespace().equals("geostrata")
                && (location.getPath().endsWith("_stone") || location.getPath().endsWith("_ore"))) return true;
        return location.getNamespace().equals("minecraft") && location.getPath().startsWith("ore_");
    }

    public static LiteralArgumentBuilder<CommandSourceStack> requiresOperator(
            LiteralArgumentBuilder<CommandSourceStack> command) {
        return command.requires(source -> source.hasPermission(2));
    }

    public static BlockPos sharedSpawnPos(ServerLevel level) {
        return level.getSharedSpawnPos();
    }

    public static Block haliteVanillaRock() {
        return Blocks.WHITE_TERRACOTTA;
    }

    public static void setSharedSpawn(ServerLevel level, BlockPos pos, float yaw) {
        level.setDefaultSpawnPos(pos, yaw);
    }

    private static final java.util.Set<ResourceLocation> COMPETING_OVERWORLD_ORES = java.util.Set.of(
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
            id("railcraft:quarried_stone"), id("railcraft:firestone"),
            id("immersiveengineering:bauxite"), id("immersiveengineering:deep_nickel"),
            id("immersiveengineering:lead"), id("immersiveengineering:mineral_veins"),
            id("immersiveengineering:nickel"), id("immersiveengineering:silver"),
            id("immersiveengineering:uranium")
    );
}
