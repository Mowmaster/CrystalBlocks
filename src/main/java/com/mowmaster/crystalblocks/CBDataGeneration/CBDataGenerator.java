package com.mowmaster.crystalblocks.CBDataGeneration;

import com.mowmaster.crystalblocks.CBReferences;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.data.event.GatherDataRegistryEntriesEvent;

@EventBusSubscriber(modid = CBReferences.MODID)
public class CBDataGenerator
{
    @SubscribeEvent
    public static void gatherClientDate(GatherDataEvent.Client event)
    {
        DataGenerator generator = event.getGenerator();
        PackOutput packOutput = generator.getPackOutput();
        var lookupProvider = event.getReloadableLookupProvider();

        generator.addProvider(true, new CBBasicModelProvider(packOutput));
        generator.addProvider(true, new CBTagProviderItem(packOutput,lookupProvider));
        generator.addProvider(true, new CBTagProviderBlock(packOutput,lookupProvider));

    }

    @SubscribeEvent
    public static void gatherDataRegistryEntries(GatherDataRegistryEntriesEvent event) {
        event.gatherFor(CBReferences.MODID, Identifier.DEFAULT_NAMESPACE)
                .add(CBBasicRecipeProvider.create())
                .lootTable(new LootTableProvider.SubProviderEntry(CBBlockLootProvider::new, LootContextParamSets.BLOCK));
    }

}
