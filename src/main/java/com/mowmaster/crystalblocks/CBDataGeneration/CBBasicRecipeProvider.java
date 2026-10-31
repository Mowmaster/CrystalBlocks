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
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;

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

        shapeless(RecipeCategory.TOOLS, CBItemRegistry.COLOR_PICKER.get(),1)
                .requires(Items.GLASS_BOTTLE)
                .requires(CBBlockRegistry.STONE.base().get())
                .unlockedBy(getHasName(CBBlockRegistry.STONE.base().get()), has(CBBlockRegistry.STONE.base().get()))
                .save(output, CBReferences.MODID + ".color_picker");

        shaped(RecipeCategory.BUILDING_BLOCKS, CBItemRegistry.COLOR_PICKER.get(), 1)
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
                .save(output, CBReferences.MODID + ".crystal_metal_from_stone");

        shapeless(RecipeCategory.DECORATIONS, CBBlockRegistry.SCONCE_TORCH.get(),1)
                .requires(Items.TORCH)
                .requires(CBBlockRegistry.STONE.base().get())
                .unlockedBy(getHasName(CBBlockRegistry.STONE.base().get()), has(CBBlockRegistry.STONE.base().get()))
                .save(output, CBReferences.MODID + ".sconce_torch");

        chainAndLanternRecipes();
        storageRecipes();





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

    // Tinted chest: the vanilla chest recipe with crystal stone in the middle.
    // In the stonecutter it turns into the barrel or either pile, and each of those turns back into the chest.
    private void storageRecipes() {
        Block chest = CBBlockRegistry.TINTABLE_CHEST.get();
        Block crystalStone = CBBlockRegistry.STONE.base().get();

        shaped(RecipeCategory.MISC, chest, 1)
                .pattern("PPP")
                .pattern("PSP")
                .pattern("PPP")
                .define('P', ItemTags.PLANKS)
                .define('S', crystalStone)
                .unlockedBy(getHasName(crystalStone), has(crystalStone))
                .save(output, CBReferences.MODID + ".tintable_chest");

        stonecutterPair(chest, CBBlockRegistry.TINTABLE_BARREL.get());
        stonecutterPair(chest, CBBlockRegistry.TINTABLE_LOG_PILE.get());
        stonecutterPair(chest, CBBlockRegistry.TINTABLE_INGOT_PILE.get());
    }

    // Chains and lanterns: crafted (shapeless), then cut between the plain and _cap versions in a stonecutter
    private void chainAndLanternRecipes() {
        Block metal = CBBlockRegistry.METAL.base().get();
        Block chain = CBBlockRegistry.CHAIN_BEAM.get();
        Block mediumChain = CBBlockRegistry.CHAIN_BEAM_MEDIUM.get();
        Block largeChain = CBBlockRegistry.CHAIN_BEAM_LARGE.get();
        Block lantern = CBBlockRegistry.CHAIN_LANTERN.get();
        Block mediumLantern = CBBlockRegistry.CHAIN_LANTERN_MEDIUM.get();

        // Any copper chain or copper lantern: every weathering stage, waxed or not
        Ingredient anyCopperChain = Ingredient.of(Items.COPPER_CHAIN.asList().toArray(ItemLike[]::new));
        Ingredient anyCopperLantern = Ingredient.of(Items.COPPER_LANTERN.asList().toArray(ItemLike[]::new));

        // Chain: crystal metal block + any copper chain
        shapeless(RecipeCategory.DECORATIONS, chain, 1)
                .requires(metal)
                .requires(anyCopperChain)
                .unlockedBy(getHasName(metal), has(metal))
                .save(output, CBReferences.MODID + ".chain_beam");

        // Medium chain: crystal metal block + iron chain
        shapeless(RecipeCategory.DECORATIONS, mediumChain, 1)
                .requires(metal)
                .requires(Items.IRON_CHAIN)
                .unlockedBy(getHasName(metal), has(metal))
                .save(output, CBReferences.MODID + ".chain_beam_medium");

        // Large chain: a chain + a medium chain make two
        shapeless(RecipeCategory.DECORATIONS, largeChain, 2)
                .requires(chain)
                .requires(mediumChain)
                .unlockedBy(getHasName(mediumChain), has(mediumChain))
                .save(output, CBReferences.MODID + ".chain_beam_large");

        // Lantern: any copper lantern + a chain
        shapeless(RecipeCategory.DECORATIONS, lantern, 1)
                .requires(anyCopperLantern)
                .requires(chain)
                .unlockedBy(getHasName(chain), has(chain))
                .save(output, CBReferences.MODID + ".chain_lantern");

        // Medium lantern: an iron lantern + a medium chain
        shapeless(RecipeCategory.DECORATIONS, mediumLantern, 1)
                .requires(Items.LANTERN)
                .requires(mediumChain)
                .unlockedBy(getHasName(mediumChain), has(mediumChain))
                .save(output, CBReferences.MODID + ".chain_lantern_medium");

        // Plain <-> _cap in the stonecutter, one for one each way
        stonecutterPair(chain, CBBlockRegistry.CHAIN_BEAM_CAP.get());
        stonecutterPair(mediumChain, CBBlockRegistry.CHAIN_BEAM_MEDIUM_CAP.get());
        stonecutterPair(largeChain, CBBlockRegistry.CHAIN_BEAM_LARGE_CAP.get());
        stonecutterPair(lantern, CBBlockRegistry.CHAIN_LANTERN_CAP.get());
        stonecutterPair(mediumLantern, CBBlockRegistry.CHAIN_LANTERN_MEDIUM_CAP.get());
    }

    // Two stonecutter recipes: a -> b and b -> a, one each
    private void stonecutterPair(ItemLike a, ItemLike b) {
        stonecutOne(a, b);
        stonecutOne(b, a);
    }

    private void stonecutOne(ItemLike input, ItemLike result) {
        SingleItemRecipeBuilder.stonecutting(Ingredient.of(input), RecipeCategory.DECORATIONS, result, 1)
                .unlockedBy(getHasName(input), has(input))
                .save(output, CBReferences.MODID + "." + getItemName(result) + "_from_" + getItemName(input) + "_stonecutting");
    }
}
