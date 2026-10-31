package com.mowmaster.crystalblocks.Blocks;

import net.minecraft.resources.Identifier;

public class CBReferences
{
    public static final String MODID = "crystalblocks";
    public static final String MODNAME = "Crystal Blocks";
    public static final String TOOLTIP = "tooltip." + MODID + ".";

    // crystalblocks:<path>
    public static Identifier id(String path)
    {
        return Identifier.fromNamespaceAndPath(MODID, path);
    }

}
