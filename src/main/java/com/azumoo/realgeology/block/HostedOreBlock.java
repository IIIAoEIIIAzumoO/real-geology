package com.azumoo.realgeology.block;

import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;

/**
 * One ore block ID with a geological-host state.  The state selects a model
 * that layers the mineral overlay over the actual GeoStrata rock texture.
 */
public final class HostedOreBlock extends Block {
    public static final EnumProperty<Host> HOST = EnumProperty.create("host", Host.class);

    public HostedOreBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(HOST, Host.GNEISS));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(HOST);
    }

    public enum Host implements StringRepresentable {
        AMPHIBOLITE, ANDESITE, BASALT, BASALTIC_GLASS, CONGLOMERATE, DIABASE,
        DIORITE, DOLOMITE, GABBRO, GNEISS, GRANITE, HORNFELS, LIMESTONE,
        KIMBERLITE, MARBLE, NOVACULITE, PEGMATITE, PERIDOTITE, PHYLLITE, QUARTZITE,
        RHYOLITE, ROCK_SALT, SCHIST, SCORIA, SHALE, SILTSTONE, SLATE, TUFF;

        public static Host fromRock(String rock) {
            return Host.valueOf(rock.toUpperCase(java.util.Locale.ROOT));
        }

        @Override
        public String getSerializedName() {
            return name().toLowerCase(java.util.Locale.ROOT);
        }
    }
}
