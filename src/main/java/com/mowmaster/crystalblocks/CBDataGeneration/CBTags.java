package com.mowmaster.crystalblocks.CBDataGeneration;

import com.mowmaster.crystalblocks.CBReferences;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class CBTags
{
    public static class Items {
        public static final TagKey<Item> CB_CHISELS = createTag("chisels");

        public static TagKey<Item> createTag(String name)
        {
            return ItemTags.create(Identifier.fromNamespaceAndPath(CBReferences.MODID, name));
        }
    }

    public static class Blocks {
        public static final TagKey<Block> CB_BLOCKS_DYABLE = createTag("cb_dyable_blocks");
        public static final TagKey<Block> CB_BLOCKS_STONE = createTag("cb_block_stone");
        public static final TagKey<Block> CB_BLOCKS_BRICK_LARGE = createTag("cb_block_brick_large");
        public static final TagKey<Block> CB_BLOCKS_BRICK_MEDIUM = createTag("cb_block_brick_medium");
        public static final TagKey<Block> CB_BLOCKS_BRICK_SMALL = createTag("cb_block_brick_small");
        public static final TagKey<Block> CB_BLOCKS_LOG = createTag("cb_block_log");
        public static final TagKey<Block> CB_BLOCKS_METAL = createTag("cb_block_metal");

        public static TagKey<Block> createTag(String name)
        {
            return BlockTags.create(Identifier.fromNamespaceAndPath(CBReferences.MODID, name));
        }
    }
}
