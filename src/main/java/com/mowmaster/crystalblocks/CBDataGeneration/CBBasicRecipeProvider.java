package com.mowmaster.crystalblocks.CBDataGeneration;

import com.mowmaster.crystalblocks.Blocks.CBBlockRegistry;
import com.mowmaster.crystalblocks.Blocks.CBBlockSet;
import com.mowmaster.crystalblocks.CBReferences;
import com.mowmaster.crystalblocks.Items.CBItemRegistry;
import com.mowmaster.crystalblocks.Recipes.CBPickedDyeRecipe;
import net.minecraft.advancements.Advancement;
import net.minecraft.core.registries.MultiRegistryBootstrap;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.SingleItemRecipeBuilder;
import net.minecraft.data.recipes.SpecialRecipeBuilder;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.data.ItemTagsProvider;
import net.neoforged.neoforge.registries.DeferredBlock;

public class CBBasicRecipeProvider extends RecipeProvider
{

    protected CBBasicRecipeProvider(BootstrapContext<Recipe<?>> recipeOutput, BootstrapContext<Advancement> advancementOutput) {
        super(recipeOutput, advancementOutput);
    }

    public static MultiRegistryBootstrap create()
    {
        return RecipeProvider.asBootstrap(CBBasicRecipeProvider::new);
    }

    @Override
    protected void buildRecipes() {

        shaped(RecipeCategory.BUILDING_BLOCKS, CBItemRegistry.CRYSTAL_CHISEL_BASE.get(), 1)
                .pattern("  S")
                .pattern(" S ")
                .pattern("II ")
                .define('S', Items.STICK)
                .define('I', Items.IRON_INGOT)
                .unlockedBy(getHasName(Items.IRON_INGOT), has(Items.IRON_INGOT))
                .save(output, CBReferences.MODID + ".base_chisel");

        // How each texture's base block is first made (the only crafting-table recipes for the sets).
        // Add one for each new set, or it can only be cut from blocks it already has.
        shaped(RecipeCategory.BUILDING_BLOCKS, CBBlockRegistry.STONE.base().get(), 8)
                .pattern("DDD")
                .pattern("DID")
                .pattern("DDD")
                .define('D', Blocks.STONE)
                .define('I', Items.AMETHYST_SHARD)
                .unlockedBy(getHasName(Items.AMETHYST_SHARD), has(Items.AMETHYST_SHARD))
                .save(output, CBReferences.MODID + ".crystal_stone_from_amethyst");
        shaped(RecipeCategory.BUILDING_BLOCKS, CBBlockRegistry.BRICK_LARGE.base().get(), 8)
                .pattern("DD")
                .pattern("DD")
                .define('D', CBBlockRegistry.STONE.base().get())
                .unlockedBy(getHasName(CBBlockRegistry.STONE.base().get()), has(CBBlockRegistry.STONE.base().get()))
                .save(output, CBReferences.MODID + ".lg_stonebrick_from_stone");
        shaped(RecipeCategory.BUILDING_BLOCKS, CBBlockRegistry.BRICK_MEDIUM.base().get(), 8)
                .pattern("DD")
                .pattern("DD")
                .define('D', CBBlockRegistry.BRICK_LARGE.base().get())
                .unlockedBy(getHasName(CBBlockRegistry.BRICK_LARGE.base().get()), has(CBBlockRegistry.BRICK_LARGE.base().get()))
                .save(output, CBReferences.MODID + ".med_stonebrick_from_lgbrick");
        shaped(RecipeCategory.BUILDING_BLOCKS, CBBlockRegistry.BRICK_SMALL.base().get(), 8)
                .pattern("DD")
                .pattern("DD")
                .define('D', CBBlockRegistry.BRICK_MEDIUM.base().get())
                .unlockedBy(getHasName(CBBlockRegistry.BRICK_MEDIUM.base().get()), has(CBBlockRegistry.BRICK_MEDIUM.base().get()))
                .save(output, CBReferences.MODID + ".sm_stonebrick_from_medbrick");

        shaped(RecipeCategory.BUILDING_BLOCKS, CBBlockRegistry.LOG_ONE.base().get(), 8)
                .pattern("DDD")
                .pattern("DID")
                .pattern("DDD")
                .define('D', ItemTags.LOGS)
                .define('I', Items.AMETHYST_SHARD)
                .unlockedBy(getHasName(Items.AMETHYST_SHARD), has(Items.AMETHYST_SHARD))
                .save(output, CBReferences.MODID + ".crystal_log_from_amethyst");

        shaped(RecipeCategory.BUILDING_BLOCKS, CBBlockRegistry.METAL.base().get(), 8)
                .pattern("DDD")
                .pattern("DID")
                .pattern("DDD")
                .define('I', Items.IRON_INGOT)
                .define('D', CBBlockRegistry.STONE.base().get())
                .unlockedBy(getHasName(CBBlockRegistry.STONE.base().get()), has(CBBlockRegistry.STONE.base().get()))
                .save(output, CBReferences.MODID + ".crystal_log_from_amethyst");

        shapeless(RecipeCategory.DECORATIONS, CBBlockRegistry.SCONCE_TORCH.get(),1)
                .requires(Items.TORCH)
                .requires(CBBlockRegistry.STONE.base().get())
                .unlockedBy(getHasName(CBBlockRegistry.STONE.base().get()), has(CBBlockRegistry.STONE.base().get()))
                .save(output, CBReferences.MODID + ".sconce_torch");






        // Every shape of every set (and the extra blocks in its tag) comes from the stonecutter
        for (CBBlockSet set : CBBlockRegistry.SETS) {
            stonecutterRecipes(set);
        }

        // Colour picker -> custom dye (the picker is returned reset)
        SpecialRecipeBuilder.special(() -> CBPickedDyeRecipe.INSTANCE)
                .save(output, CBReferences.MODID + ".picked_dye");
    }

    // Any block in a set's tag cuts into any other block in that tag, one for one.
    // Keep every count at 1: if anything gave more than one (e.g. 2 slabs), cutting it back into the base block
    // and round again would duplicate blocks.
    private void stonecutterRecipes(CBBlockSet set) {
        TagKey<Item> anyInSet = CBTags.forSet(set).items();
        for (DeferredBlock<Block> block : CBTags.members(set)) {
            SingleItemRecipeBuilder.stonecutting(tag(anyInSet), RecipeCategory.BUILDING_BLOCKS, block.get(), 1)
                    // In the recipe book once the player has any block of the set
                    .unlockedBy("has_" + anyInSet.location().getPath(), has(anyInSet))
                    .save(output, CBReferences.MODID + "." + block.getId().getPath() + "_stonecutting");
        }
    }
}
