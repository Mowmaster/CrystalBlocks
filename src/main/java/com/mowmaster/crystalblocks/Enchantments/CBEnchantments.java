package com.mowmaster.crystalblocks.Enchantments;

import com.mowmaster.crystalblocks.CBReferences;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.item.enchantment.Enchantment;

// Enchantments are data (a datapack registry): datagen writes data/crystalblocks/enchantment/path_runner.json from
// bootstrap below. What it does is in CBPathRunner.
public class CBEnchantments
{
    // Soul Speed for paths: boots only, levels 1-5, only found in dungeon chests
    public static final ResourceKey<Enchantment> PATH_RUNNER = ResourceKey.create(Registries.ENCHANTMENT, CBReferences.id("path_runner"));

    public static void bootstrap(BootstrapContext<Enchantment> context)
    {
        context.register(PATH_RUNNER, new Enchantment.Builder(Enchantment.definition(
                        context.lookup(Registries.ITEM).getOrThrow(ItemTags.FOOT_ARMOR_ENCHANTABLE),
                        1,                                  // weight: as rare as Soul Speed
                        5,                                  // max level
                        Enchantment.dynamicCost(10, 10),    // min cost: Soul Speed's
                        Enchantment.dynamicCost(25, 10),    // max cost
                        8,                                  // anvil cost
                        EquipmentSlotGroup.FEET))
                .build(PATH_RUNNER.identifier()));
    }
}
