package com.mowmaster.crystalblocks.Loot;

import com.google.common.base.Suppliers;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mowmaster.crystalblocks.Enchantments.CBEnchantments;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;

import java.util.Optional;
import java.util.function.Supplier;

// A global loot modifier: when a loot table it applies to rolls (dungeon chests, see CBLootModifierProvider), there's
// a 20% chance of an extra Path Runner book at a random level 1-5. It's the only way to get Path Runner.
public class CBDungeonBookModifier extends LootModifier
{
    private static final float BOOK_CHANCE = 0.2f;

    // How it's saved in data/crystalblocks/loot_modifiers/*.json: just the conditions and priority every modifier has
    public static final Supplier<MapCodec<CBDungeonBookModifier>> CODEC = Suppliers.memoize(() ->
            RecordCodecBuilder.mapCodec(instance -> codecStart(instance).apply(instance, CBDungeonBookModifier::new)));

    public CBDungeonBookModifier(Optional<Holder<LootItemCondition>> conditions, int priority)
    {
        super(conditions, priority);
    }

    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context)
    {
        if (context.getRandom().nextFloat() < BOOK_CHANCE)
        {
            context.getLevel().registryAccess().lookupOrThrow(Registries.ENCHANTMENT).get(CBEnchantments.PATH_RUNNER).ifPresent(enchantment ->
            {
                // On an enchanted book, enchant() stores it as a book enchantment (applied with an anvil)
                ItemStack book = new ItemStack(Items.ENCHANTED_BOOK);
                book.enchant(enchantment, 1 + context.getRandom().nextInt(enchantment.value().getMaxLevel()));
                generatedLoot.add(book);
            });
        }
        return generatedLoot;
    }

    @Override
    public MapCodec<? extends IGlobalLootModifier> codec()
    {
        return CODEC.get();
    }
}
