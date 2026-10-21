package com.mowmaster.crystalblocks.CBDataGeneration;

import net.minecraft.core.RegistrySetBuilder;

public class CBDataPackProvider
{
    public static final RegistrySetBuilder WORLD_BUILDER = new RegistrySetBuilder();

    public static final RegistrySetBuilder RELOADABLE_BUILDER = new RegistrySetBuilder()
            .add(CBBasicRecipeProvider.create());
}
