package com.mowmaster.crystalblocks.Blocks;

import com.mowmaster.crystalblocks.CBReferences;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.DoubleBlockCombiner;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.transfer.CombinedResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.VanillaContainerWrapper;
import org.jetbrains.annotations.Nullable;

// Lets other mods' pipes and similar reach our storage blocks' inventories (vanilla hoppers use the Container
// directly and don't need this). Same as NeoForge does for vanilla chests and barrels.
@EventBusSubscriber(modid = CBReferences.MODID)
public class CBCapabilities
{
    // A double chest's two halves become one handler; a diamond-locked chest gives none (see CBTintableChest#combine)
    private static final DoubleBlockCombiner.Combiner<ChestBlockEntity, ResourceHandler<ItemResource>> CHEST_HANDLER = new DoubleBlockCombiner.Combiner<>()
    {
        @Override
        public ResourceHandler<ItemResource> acceptDouble(ChestBlockEntity first, ChestBlockEntity second)
        {
            return new CombinedResourceHandler<>(VanillaContainerWrapper.of(first), VanillaContainerWrapper.of(second));
        }

        @Override
        public ResourceHandler<ItemResource> acceptSingle(ChestBlockEntity chest)
        {
            return VanillaContainerWrapper.of(chest);
        }

        @Override
        public @Nullable ResourceHandler<ItemResource> acceptNone()
        {
            return null;
        }
    };

    @SubscribeEvent
    public static void registerCapabilities(RegisterCapabilitiesEvent event)
    {
        event.registerBlock(Capabilities.Item.BLOCK,
                (level, pos, state, blockEntity, side) -> ((ChestBlock) state.getBlock()).combine(state, level, pos, true).apply(CHEST_HANDLER),
                CBBlockRegistry.TINTABLE_CHEST.get());
        event.registerBlockEntity(Capabilities.Item.BLOCK, CBBlockEntityRegistry.TINTABLE_BARREL.get(),
                (barrel, side) -> VanillaContainerWrapper.of(barrel));
    }
}
