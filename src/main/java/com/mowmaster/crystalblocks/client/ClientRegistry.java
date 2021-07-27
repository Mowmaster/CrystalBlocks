package com.mowmaster.crystalblocks.client;

import com.mowmaster.crystalblocks.blocks.CrystalBlock;
import com.mowmaster.crystalblocks.blocks.CrystalSlab;
import com.mowmaster.crystalblocks.blocks.CrystalStair;
import com.mowmaster.crystalblocks.items.ColorApplicator;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = "crystalblocks", bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientRegistry {

    @SubscribeEvent
    public static void onItemColorsReady(ColorHandlerEvent.Item event)
    {
        ColorApplicator.handleItemColors(event);
        CrystalBlock.handleItemColors(event);
        CrystalStair.handleItemColors(event);
        CrystalSlab.handleItemColors(event);
    }

    @SubscribeEvent
    public static void onBlockColorsReady(ColorHandlerEvent.Block event)
    {
        CrystalBlock.handleBlockColors(event);
        CrystalStair.handleBlockColors(event);
        CrystalSlab.handleBlockColors(event);
    }
}
