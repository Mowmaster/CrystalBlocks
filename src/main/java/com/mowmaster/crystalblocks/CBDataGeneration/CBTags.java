package com.mowmaster.crystalblocks.CBDataGeneration;

import com.mowmaster.crystalblocks.Blocks.CBBlockRegistry;
import com.mowmaster.crystalblocks.Blocks.CBBlockSet;
import com.mowmaster.crystalblocks.CBReferences;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.DeferredBlock;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CBTags
{
    public static class Items {
        public static final TagKey<Item> CB_CHISELS = createTag("chisels");

        // Item versions of the block set tags (recipes use item tags). Same names as the block tags.
        public static final TagKey<Item> CB_ITEMS_STONE = createTag("cb_block_stone");
        public static final TagKey<Item> CB_ITEMS_BRICK_LARGE = createTag("cb_block_brick_large");
        public static final TagKey<Item> CB_ITEMS_BRICK_MEDIUM = createTag("cb_block_brick_medium");
        public static final TagKey<Item> CB_ITEMS_BRICK_SMALL = createTag("cb_block_brick_small");
        public static final TagKey<Item> CB_ITEMS_LOG = createTag("cb_block_log");
        public static final TagKey<Item> CB_ITEMS_METAL = createTag("cb_block_metal");

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

    // ---- Block set tags ----

    // A block set's block tag, item tag, and any blocks outside the set that belong with it (they go in the same
    // tags, so the stonecutter cuts them from and into the set's blocks)
    public record SetTags(TagKey<Block> blocks, TagKey<Item> items, List<DeferredBlock<Block>> extras) {}

    // Keyed by the set's name. Add a line here for each new set.
    private static final Map<String, SetTags> SET_TAGS = Map.of(
            CBBlockRegistry.STONE.name(), new SetTags(Blocks.CB_BLOCKS_STONE, Items.CB_ITEMS_STONE, List.of()),
            CBBlockRegistry.BRICK_LARGE.name(), new SetTags(Blocks.CB_BLOCKS_BRICK_LARGE, Items.CB_ITEMS_BRICK_LARGE, List.of()),
            CBBlockRegistry.BRICK_MEDIUM.name(), new SetTags(Blocks.CB_BLOCKS_BRICK_MEDIUM, Items.CB_ITEMS_BRICK_MEDIUM, List.of()),
            CBBlockRegistry.BRICK_SMALL.name(), new SetTags(Blocks.CB_BLOCKS_BRICK_SMALL, Items.CB_ITEMS_BRICK_SMALL, List.of()),
            CBBlockRegistry.LOG_ONE.name(), new SetTags(Blocks.CB_BLOCKS_LOG, Items.CB_ITEMS_LOG, List.of(
                    CBBlockRegistry.BOLTED_SUPPORT_BEAM,
                    CBBlockRegistry.BOLTED_SUPPORT_BEAM_CAP,
                    CBBlockRegistry.CHAINED_SUPPORT_BEAM,
                    CBBlockRegistry.CHAINED_SUPPORT_BEAM_MEDIUM)),
            CBBlockRegistry.METAL.name(), new SetTags(Blocks.CB_BLOCKS_METAL, Items.CB_ITEMS_METAL, List.of(
                    CBBlockRegistry.BOLTED_CORNER_SUPPORT_CAP,
                    CBBlockRegistry.BOLTED_I_BEAM,
                    CBBlockRegistry.BOLTED_I_BEAM_CAP,
                    CBBlockRegistry.TINTABLE_BLOCK_TECH,
                    CBBlockRegistry.TINTABLE_BLOCK_TECH_3D)));

    public static SetTags forSet(CBBlockSet set)
    {
        SetTags tags = SET_TAGS.get(set.name());
        // A new set without tags fails datagen with this message, rather than silently making no recipes
        if (tags == null) throw new IllegalStateException("No tags for block set " + set.name() + ": add it to CBTags.SET_TAGS");
        return tags;
    }

    // Everything in a set's tags: the set's own blocks, then its extras
    public static List<DeferredBlock<Block>> members(CBBlockSet set)
    {
        List<DeferredBlock<Block>> members = new ArrayList<>(set.blocks());
        members.addAll(forSet(set).extras());
        return members;
    }
}
