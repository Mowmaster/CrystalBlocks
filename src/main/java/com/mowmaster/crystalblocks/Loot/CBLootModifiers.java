package com.mowmaster.crystalblocks.Loot;

import com.mojang.serialization.MapCodec;
import com.mowmaster.crystalblocks.CBReferences;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

// Global loot modifier types. The modifiers themselves (which tables they change) are data, made by
// CBLootModifierProvider.
public class CBLootModifiers
{
    public static final DeferredRegister<MapCodec<? extends IGlobalLootModifier>> LOOT_MODIFIERS =
            DeferredRegister.create(NeoForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, CBReferences.MODID);

    static
    {
        LOOT_MODIFIERS.register("dungeon_book", CBDungeonBookModifier.CODEC);
    }

    public static void register(IEventBus eventBus)
    {
        LOOT_MODIFIERS.register(eventBus);
    }
}
