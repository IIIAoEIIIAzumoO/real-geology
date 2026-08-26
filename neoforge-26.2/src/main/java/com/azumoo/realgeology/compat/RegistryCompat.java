package com.azumoo.realgeology.compat;

import com.azumoo.realgeology.RealGeology;
import com.azumoo.realgeology.block.HostedOreBlock;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.LinkedHashMap;
import java.util.Map;

/** NeoForge 26.2 block/item registration (setId required on properties). */
public final class RegistryCompat {
    private RegistryCompat() { }

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(RealGeology.MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(RealGeology.MODID);

    public static final DeferredBlock<Block> KIMBERLITE = BLOCKS.registerSimpleBlock(
            "kimberlite",
            props -> props.strength(3.4f, 3.2f).requiresCorrectToolForDrops());

    private static final String[] HOSTED_ORE_MATERIALS = {
            "coal", "iron", "copper", "gold", "diamond", "lapis", "emerald", "redstone",
            "tin", "lead", "zinc", "nickel", "lithium", "uranium", "osmium", "fluorite",
            "silver", "sulfur", "saltpeter", "galena", "bauxite", "lignite"
    };

    private static final Map<String, DeferredBlock<HostedOreBlock>> HOSTED_ORES = new LinkedHashMap<>();

    static {
        for (String material : HOSTED_ORE_MATERIALS) {
            HOSTED_ORES.put(material, registerHostedOre(material));
        }
        ITEMS.registerSimpleBlockItem(KIMBERLITE, props -> props);
    }

    private static DeferredBlock<HostedOreBlock> registerHostedOre(String material) {
        DeferredBlock<HostedOreBlock> block = BLOCKS.registerBlock(
                material + "_ore",
                HostedOreBlock::new,
                props -> props.strength(3.2f, 3.0f).requiresCorrectToolForDrops());
        ITEMS.registerSimpleBlockItem(block, props -> props);
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
