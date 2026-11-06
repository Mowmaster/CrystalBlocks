package com.mowmaster.crystalblocks.CBDataGeneration;

import com.mowmaster.crystalblocks.CBReferences;
import com.mowmaster.crystalblocks.Loot.CBDungeonBookModifier;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.common.data.GlobalLootModifierProvider;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootTableIdCondition;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

// NeoForge global loot modifiers change loot tables we don't own without replacing them.
// Dungeon chests get a chance of a Path Runner book.
public class CBLootModifierProvider extends GlobalLootModifierProvider
{
    public CBLootModifierProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries)
    {
        super(output, registries, CBReferences.MODID);
    }

    @Override
    protected void start()
    {
        add("dungeon_path_runner", new CBDungeonBookModifier(
                Optional.of(Holder.direct(LootTableIdCondition.builder(Identifier.withDefaultNamespace("chests/simple_dungeon")).build())),
                IGlobalLootModifier.DEFAULT_PRIORITY));
    }
}
