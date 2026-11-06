package com.mowmaster.crystalblocks;

import com.mowmaster.crystalblocks.Blocks.CBBlockRegistry;
import com.mowmaster.crystalblocks.Blocks.CBBlockSet;
import com.mowmaster.crystalblocks.Items.CBItemRegistry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

// The mod's two creative tabs. Creative tabs are a registry like blocks and items, so they're registered the same way.
// displayItems runs whenever the tab's contents are (re)built, and lists what goes in it, in order.
public class CBCreativeTabs
{
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, CBReferences.MODID);

    // Every block: each set's shapes, then the supports, chains and lanterns, storage, rails and paths
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> BLOCKS = TABS.register("blocks", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.crystalblocks.blocks"))
            // A supplier (() -> ...): the icon is made later, once the block's item exists
            .icon(() -> new ItemStack(CBBlockRegistry.TINTABLE_INGOT_PILE.get()))
            .displayItems((parameters, output) -> {
                for (CBBlockSet set : CBBlockRegistry.SETS)
                {
                    for (DeferredBlock<Block> block : set.blocks()) output.accept(block);
                }
                for (DeferredBlock<Block> block : CBBlockRegistry.BOLTED) output.accept(block);
                for (DeferredBlock<Block> block : CBBlockRegistry.STORAGE) output.accept(block);
                output.accept(CBBlockRegistry.TINTABLE_RAIL);
                for (DeferredBlock<Block> block : CBBlockRegistry.PATHS) output.accept(block);
            })
            .build());

    // Tools and other items
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> ITEMS = TABS.register("items", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.crystalblocks.items"))
            .icon(() -> new ItemStack(CBItemRegistry.TAMPER.get()))
            // Placed right after the blocks tab in the creative menu
            .withTabsBefore(BLOCKS.key())
            .displayItems((parameters, output) -> {
                output.accept(CBItemRegistry.CRYSTAL_CHISEL_BASE);
                output.accept(CBItemRegistry.TAMPER);
                output.accept(CBItemRegistry.WRENCH);
            })
            .build());

    public static void register(IEventBus eventBus)
    {
        TABS.register(eventBus);
    }
}
