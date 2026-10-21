package com.mowmaster.crystalblocks;

import com.mowmaster.crystalblocks.Blocks.CBBlockRegistry;
import com.mowmaster.crystalblocks.Items.CBItemRegistry;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

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
            event.accept(CBBlockRegistry.CRYSTAL_STONE_RED);
            event.accept(CBBlockRegistry.CRYSTAL_STONE_RED_STAIRS);
            event.accept(CBBlockRegistry.CRYSTAL_STONE_RED_SLAB);
            event.accept(CBBlockRegistry.CRYSTAL_STONE_RED_WALL);
            event.accept(CBBlockRegistry.CRYSTAL_STONE_RED_BUTTON);
            event.accept(CBBlockRegistry.CRYSTAL_STONE_RED_PRESSUREPLATE);

            event.accept(CBBlockRegistry.CRYSTAL_STONE_GREEN);
            event.accept(CBBlockRegistry.CRYSTAL_STONE_GREEN_STAIRS);
            event.accept(CBBlockRegistry.CRYSTAL_STONE_GREEN_SLAB);
            event.accept(CBBlockRegistry.CRYSTAL_STONE_GREEN_WALL);
            event.accept(CBBlockRegistry.CRYSTAL_STONE_GREEN_BUTTON);
            event.accept(CBBlockRegistry.CRYSTAL_STONE_GREEN_PRESSUREPLATE);

            event.accept(CBBlockRegistry.CRYSTAL_STONE_BLUE);
            event.accept(CBBlockRegistry.CRYSTAL_STONE_BLUE_STAIRS);
            event.accept(CBBlockRegistry.CRYSTAL_STONE_BLUE_SLAB);
            event.accept(CBBlockRegistry.CRYSTAL_STONE_BLUE_WALL);
            event.accept(CBBlockRegistry.CRYSTAL_STONE_BLUE_BUTTON);
            event.accept(CBBlockRegistry.CRYSTAL_STONE_BLUE_PRESSUREPLATE);

            event.accept(CBBlockRegistry.CRYSTAL_STONE_WHITE);
            event.accept(CBBlockRegistry.CRYSTAL_STONE_WHITE_STAIRS);
            event.accept(CBBlockRegistry.CRYSTAL_STONE_WHITE_SLAB);
            event.accept(CBBlockRegistry.CRYSTAL_STONE_WHITE_WALL);
            event.accept(CBBlockRegistry.CRYSTAL_STONE_WHITE_BUTTON);
            event.accept(CBBlockRegistry.CRYSTAL_STONE_WHITE_PRESSUREPLATE);

            event.accept(CBBlockRegistry.CRYSTAL_STONE_BLACK);
            event.accept(CBBlockRegistry.CRYSTAL_STONE_BLACK_STAIRS);
            event.accept(CBBlockRegistry.CRYSTAL_STONE_BLACK_SLAB);
            event.accept(CBBlockRegistry.CRYSTAL_STONE_BLACK_WALL);
            event.accept(CBBlockRegistry.CRYSTAL_STONE_BLACK_BUTTON);
            event.accept(CBBlockRegistry.CRYSTAL_STONE_BLACK_PRESSUREPLATE);
        }
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        // Do something when the server starts
        //LOGGER.info("HELLO from server starting");
    }
}
