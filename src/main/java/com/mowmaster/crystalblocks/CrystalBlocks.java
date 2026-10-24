package com.mowmaster.crystalblocks;

import com.mowmaster.crystalblocks.Blocks.CBBlockEntityRegistry;
import com.mowmaster.crystalblocks.Blocks.CBBlockRegistry;
import com.mowmaster.crystalblocks.Items.CBItemRegistry;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import com.mowmaster.crystalblocks.Blocks.CBBlockSet;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.DeferredBlock;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(CBReferences.MODID)
public class CrystalBlocks {
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();

    public CrystalBlocks(IEventBus modEventBus, ModContainer modContainer) {
        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);

        CBItemRegistry.register(modEventBus);
        CBBlockRegistry.register(modEventBus);
        CBBlockEntityRegistry.register(modEventBus);

        NeoForge.EVENT_BUS.register(this);
        modEventBus.addListener(this::addCreative);
        modContainer.registerConfig(ModConfig.Type.LOCAL, Config.SPEC);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        // Some common setup code
        LOGGER.info("HELLO FROM COMMON SETUP");


        LOGGER.info("{}{}", Config.MAGIC_NUMBER_INTRODUCTION.get(), Config.MAGIC_NUMBER.getAsInt());

        Config.ITEM_STRINGS.get().forEach((item) -> LOGGER.info("ITEM >> {}", item));
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS) {
            for (CBBlockSet set : CBBlockRegistry.SETS) {
                for (DeferredBlock<Block> block : set.blocks()) {
                    event.accept(block);
                }
            }
        }
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        // Do something when the server starts
        //LOGGER.info("HELLO from server starting");
    }
}
