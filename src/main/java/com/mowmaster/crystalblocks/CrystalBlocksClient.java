package com.mowmaster.crystalblocks;

import com.mowmaster.crystalblocks.Blocks.CBBlockRegistry;
import com.mowmaster.crystalblocks.ClientSide.CBBlockEntityTintSource;
import com.mowmaster.crystalblocks.CrystalBlocks;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import com.mowmaster.crystalblocks.Blocks.Colorable.IColorableBlock;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.world.level.block.Block;
import java.util.ArrayList;

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

    // Every colourable block gets one tint source per tint region (region N = model tintindex N)
    @SubscribeEvent
    public static void registerTintBlocks(RegisterColorHandlersEvent.BlockTintSources event)
    {
        for (var holder : CBBlockRegistry.BCBLOCKS.getEntries())
        {
            Block block = holder.get();
            if (block instanceof IColorableBlock colorable)
            {
                List<BlockTintSource> sources = new ArrayList<>();
                for (int region = 0; region < colorable.getTintRegionCount(); region++)
                {
                    sources.add(new CBBlockEntityTintSource(region));
                }
                event.register(sources, block);
            }
        }
    }
}
