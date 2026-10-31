package com.mowmaster.crystalblocks.Blocks;


import com.mowmaster.crystalblocks.Blocks.Colorable.CBTintableBarrelBlockEntity;
import com.mowmaster.crystalblocks.Blocks.Colorable.CBTintableChestBlockEntity;
import com.mowmaster.crystalblocks.CBReferences;
import com.mowmaster.mowlib.MowLibReferences;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class CBBlockEntityRegistry
{
    public static final DeferredRegister<BlockEntityType<?>> CBBLOCKENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, CBReferences.MODID);

    // The plain tint and bulk storage block entity types now come from MowLib (mowlib:tint_block_entity,
    // mowlib:bulk_storage), which finds their blocks in every mod. Worlds saved before the move refer to the old
    // crystalblocks: ids; these aliases point them at MowLib's, so colours and pile contents keep loading.
    // Aliases must be added before registration, so this runs as soon as the class loads.
    static
    {
        CBBLOCKENTITIES.addAlias(CBReferences.id("tint_block_entity"), MowLibReferences.id("tint_block_entity"));
        CBBLOCKENTITIES.addAlias(CBReferences.id("bulk_storage"), MowLibReferences.id("bulk_storage"));
    }

    public static final Supplier<BlockEntityType<CBTintableBarrelBlockEntity>> TINTABLE_BARREL = CBBLOCKENTITIES.register("tintable_barrel",
            () -> new BlockEntityType<>(CBTintableBarrelBlockEntity::new, CBBlockRegistry.TINTABLE_BARREL.get()));
    public static final Supplier<BlockEntityType<CBTintableChestBlockEntity>> TINTABLE_CHEST = CBBLOCKENTITIES.register("tintable_chest",
            () -> new BlockEntityType<>(CBTintableChestBlockEntity::new, CBBlockRegistry.TINTABLE_CHEST.get()));

    public static void register(IEventBus eventBus)
    {
        CBBLOCKENTITIES.register(eventBus);
    }
}