package com.mowmaster.crystalblocks;

import com.mowmaster.crystalblocks.Blocks.CBBlockRegistry;
import com.mowmaster.crystalblocks.CrystalBlocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockTintSources;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

import java.util.List;

// This class will not load on dedicated servers. Accessing client side code from here is safe.
@Mod(value = CBReferences.MODID, dist = Dist.CLIENT)
// You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
@EventBusSubscriber(modid = CBReferences.MODID, value = Dist.CLIENT)
public class CrystalBlocksClient {
    public CrystalBlocksClient(ModContainer container) {
        // Allows NeoForge to create a config screen for this mod's configs.
        // The config screen is accessed by going to the Mods screen > clicking on your mod > clicking on config.
        // Do not forget to add translations for your config options to the en_us.json file.
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        // Some client setup code
        CrystalBlocks.LOGGER.info("HELLO FROM CLIENT SETUP");
        CrystalBlocks.LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
    }

    @SubscribeEvent
    public static void registerTintBlocks(RegisterColorHandlersEvent.BlockTintSources event)
    {
        event.register(List.of(BlockTintSources.constant(16711680)), CBBlockRegistry.CRYSTAL_STONE_RED.get());
        event.register(List.of(BlockTintSources.constant(16711680)), CBBlockRegistry.CRYSTAL_STONE_RED_STAIRS.get());
        event.register(List.of(BlockTintSources.constant(16711680)), CBBlockRegistry.CRYSTAL_STONE_RED_SLAB.get());
        event.register(List.of(BlockTintSources.constant(16711680)), CBBlockRegistry.CRYSTAL_STONE_RED_WALL.get());
        event.register(List.of(BlockTintSources.constant(16711680)), CBBlockRegistry.CRYSTAL_STONE_RED_BUTTON.get());
        event.register(List.of(BlockTintSources.constant(16711680)), CBBlockRegistry.CRYSTAL_STONE_RED_PRESSUREPLATE.get());

        event.register(List.of(BlockTintSources.constant(65280)), CBBlockRegistry.CRYSTAL_STONE_GREEN.get());
        event.register(List.of(BlockTintSources.constant(65280)), CBBlockRegistry.CRYSTAL_STONE_GREEN_STAIRS.get());
        event.register(List.of(BlockTintSources.constant(65280)), CBBlockRegistry.CRYSTAL_STONE_GREEN_SLAB.get());
        event.register(List.of(BlockTintSources.constant(65280)), CBBlockRegistry.CRYSTAL_STONE_GREEN_WALL.get());
        event.register(List.of(BlockTintSources.constant(65280)), CBBlockRegistry.CRYSTAL_STONE_GREEN_BUTTON.get());
        event.register(List.of(BlockTintSources.constant(65280)), CBBlockRegistry.CRYSTAL_STONE_GREEN_PRESSUREPLATE.get());

        event.register(List.of(BlockTintSources.constant(255)), CBBlockRegistry.CRYSTAL_STONE_BLUE.get());
        event.register(List.of(BlockTintSources.constant(255)), CBBlockRegistry.CRYSTAL_STONE_BLUE_STAIRS.get());
        event.register(List.of(BlockTintSources.constant(255)), CBBlockRegistry.CRYSTAL_STONE_BLUE_SLAB.get());
        event.register(List.of(BlockTintSources.constant(255)), CBBlockRegistry.CRYSTAL_STONE_BLUE_WALL.get());
        event.register(List.of(BlockTintSources.constant(255)), CBBlockRegistry.CRYSTAL_STONE_BLUE_BUTTON.get());
        event.register(List.of(BlockTintSources.constant(255)), CBBlockRegistry.CRYSTAL_STONE_BLUE_PRESSUREPLATE.get());

        event.register(List.of(BlockTintSources.constant(16777215)), CBBlockRegistry.CRYSTAL_STONE_WHITE.get());
        event.register(List.of(BlockTintSources.constant(16777215)), CBBlockRegistry.CRYSTAL_STONE_WHITE_STAIRS.get());
        event.register(List.of(BlockTintSources.constant(16777215)), CBBlockRegistry.CRYSTAL_STONE_WHITE_SLAB.get());
        event.register(List.of(BlockTintSources.constant(16777215)), CBBlockRegistry.CRYSTAL_STONE_WHITE_WALL.get());
        event.register(List.of(BlockTintSources.constant(16777215)), CBBlockRegistry.CRYSTAL_STONE_WHITE_BUTTON.get());
        event.register(List.of(BlockTintSources.constant(16777215)), CBBlockRegistry.CRYSTAL_STONE_WHITE_PRESSUREPLATE.get());

        event.register(List.of(BlockTintSources.constant(2763306)), CBBlockRegistry.CRYSTAL_STONE_BLACK.get());
        event.register(List.of(BlockTintSources.constant(2763306)), CBBlockRegistry.CRYSTAL_STONE_BLACK_STAIRS.get());
        event.register(List.of(BlockTintSources.constant(2763306)), CBBlockRegistry.CRYSTAL_STONE_BLACK_SLAB.get());
        event.register(List.of(BlockTintSources.constant(2763306)), CBBlockRegistry.CRYSTAL_STONE_BLACK_WALL.get());
        event.register(List.of(BlockTintSources.constant(2763306)), CBBlockRegistry.CRYSTAL_STONE_BLACK_BUTTON.get());
        event.register(List.of(BlockTintSources.constant(2763306)), CBBlockRegistry.CRYSTAL_STONE_BLACK_PRESSUREPLATE.get());
    }
}
