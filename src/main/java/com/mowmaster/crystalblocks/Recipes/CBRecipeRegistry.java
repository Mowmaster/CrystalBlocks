package com.mowmaster.crystalblocks.Recipes;

import com.mojang.serialization.MapCodec;
import com.mowmaster.crystalblocks.CBReferences;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class CBRecipeRegistry
{
    public static final DeferredRegister<RecipeSerializer<?>> CBRECIPESERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, CBReferences.MODID);

    // The recipe has no settings, so both codecs just produce the single instance
    public static final Supplier<RecipeSerializer<CBPickedDyeRecipe>> PICKED_DYE = CBRECIPESERIALIZERS.register("picked_dye",
            () -> new RecipeSerializer<>(MapCodec.unit(CBPickedDyeRecipe.INSTANCE), StreamCodec.unit(CBPickedDyeRecipe.INSTANCE)));

    public static void register(IEventBus eventBus)
    {
        CBRECIPESERIALIZERS.register(eventBus);
    }
}