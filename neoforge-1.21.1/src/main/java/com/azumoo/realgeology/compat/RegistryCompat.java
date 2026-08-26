package com.azumoo.realgeology.compat;

import com.azumoo.realgeology.RealGeology;
import com.azumoo.realgeology.block.HostedOreBlock;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.LinkedHashMap;
import java.util.Map;

/** Minecraft 1.21.1 block/item registration. */
public final class RegistryCompat {
    private RegistryCompat() { }

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(Registries.BLOCK, RealGeology.MODID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, RealGeology.MODID);

    public static final DeferredHolder<Block, Block> KIMBERLITE = BLOCKS.register("kimberlite", () ->
            new Block(BlockBehaviour.Properties.of().strength(3.4f, 3.2f).requiresCorrectToolForDrops()));

    private static final String[] HOSTED_ORE_MATERIALS = {
            "coal", "iron", "copper", "gold", "diamond", "lapis", "emerald", "redstone",
            "tin", "lead", "zinc", "nickel", "lithium", "uranium", "osmium", "fluorite",
            "silver", "sulfur", "saltpeter", "galena", "bauxite", "lignite"
    };

    private static final Map<String, DeferredHolder<Block, HostedOreBlock>> HOSTED_ORES = new LinkedHashMap<>();

    static {
        for (String material : HOSTED_ORE_MATERIALS) {
            HOSTED_ORES.put(material, registerHostedOre(material));
        }
        ITEMS.register("kimberlite", () -> new BlockItem(KIMBERLITE.get(), new Item.Properties()));
    }

    private static DeferredHolder<Block, HostedOreBlock> registerHostedOre(String material) {
        DeferredHolder<Block, HostedOreBlock> block = BLOCKS.register(material + "_ore", () ->
                new HostedOreBlock(BlockBehaviour.Properties.of().strength(3.2f, 3.0f).requiresCorrectToolForDrops()));
        ITEMS.register(material + "_ore", () -> new BlockItem(block.get(), new Item.Properties()));
        return block;
    }

    public static void register(IEventBus modBus) {
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
    }

    public static Block hostedOre(String material) {
        DeferredHolder<Block, HostedOreBlock> block = HOSTED_ORES.get(material);
        return block == null ? null : block.get();
    }
}
