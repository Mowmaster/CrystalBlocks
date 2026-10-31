package com.mowmaster.crystalblocks;

import com.mowmaster.crystalblocks.Blocks.CBBlockEntityRegistry;
import com.mowmaster.crystalblocks.ClientSide.CBTintableChestRenderer;
import com.mowmaster.crystalblocks.CrystalBlocks;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.client.model.standalone.SimpleUnbakedStandaloneModel;

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
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event)
    {
        event.registerBlockEntityRenderer(CBBlockEntityRegistry.TINTABLE_CHEST.get(), CBTintableChestRenderer::new);
    }

    // The chest lids aren't used by any blockstate, so they're loaded as standalone models for the lid renderer
    @SubscribeEvent
    public static void registerStandaloneModels(ModelEvent.RegisterStandalone event)
    {
        for (ChestType type : ChestType.values())
        {
            event.register(CBTintableChestRenderer.lidKey(type),
                    SimpleUnbakedStandaloneModel.simpleModelWrapper(CBTintableChestRenderer.lidModel(type)));
        }
    }
}
