package com.mowmaster.crystalblocks.Blocks;

import com.mowmaster.crystalblocks.Blocks.Colorable.CBTintBlockEntity;
import com.mowmaster.crystalblocks.Blocks.Colorable.IColorableBlock;
import com.mowmaster.crystalblocks.CBReferences;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class CBBlockEntityRegistry
{
    public static final DeferredRegister<BlockEntityType<?>> CBBLOCKENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, CBReferences.MODID);

    public static final Supplier<BlockEntityType<CBTintBlockEntity>> TINT_BLOCK_ENTITY = CBBLOCKENTITIES.register("tint_block_entity",
            () -> new BlockEntityType<>(CBTintBlockEntity::new, colorableBlocks()));

    // Every block in our register that implements IColorableBlock
    private static Block[] colorableBlocks()
    {
        List<Block> blocks = new ArrayList<>();
        for (var holder : CBBlockRegistry.BCBLOCKS.getEntries())
        {
            if (holder.get() instanceof IColorableBlock) blocks.add(holder.get());
        }
        return blocks.toArray(new Block[0]);
    }

    public static void register(IEventBus eventBus)
    {
        CBBLOCKENTITIES.register(eventBus);
    }
}