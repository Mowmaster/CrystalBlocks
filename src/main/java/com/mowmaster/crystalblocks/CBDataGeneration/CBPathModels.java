package com.mowmaster.crystalblocks.CBDataGeneration;

import com.mowmaster.crystalblocks.Blocks.CBBlockRegistry;
import com.mowmaster.crystalblocks.Blocks.Path.CBPathBlock;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.model.ItemModelUtils;
import net.minecraft.client.data.models.model.ModelLocationUtils;
import net.minecraft.client.data.models.model.ModelTemplate;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.DeferredBlock;

import java.util.Optional;

// Each path uses vanilla's dirt path model (a block one pixel short) with its source block's texture on every side
public class CBPathModels
{
    private static final ModelTemplate DIRT_PATH = new ModelTemplate(Optional.of(Identifier.withDefaultNamespace("block/dirt_path")),
            Optional.empty(), TextureSlot.TOP, TextureSlot.SIDE, TextureSlot.BOTTOM, TextureSlot.PARTICLE);

    public static void generate(BlockModelGenerators blockModels)
    {
        for (DeferredBlock<Block> holder : CBBlockRegistry.PATHS)
        {
            CBPathBlock path = (CBPathBlock) holder.get();
            // e.g. minecraft:block/gravel
            Identifier sourceId = BuiltInRegistries.BLOCK.getKey(path.source());
            Material texture = new Material(sourceId.withPrefix("block/"));
            TextureMapping textures = new TextureMapping()
                    .put(TextureSlot.TOP, texture)
                    .put(TextureSlot.SIDE, texture)
                    .put(TextureSlot.BOTTOM, texture)
                    .put(TextureSlot.PARTICLE, texture);

            Identifier model = DIRT_PATH.create(ModelLocationUtils.getModelLocation(path), textures, blockModels.modelOutput);
            blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(path, BlockModelGenerators.plainVariant(model)));
            blockModels.itemModelOutput.accept(path.asItem(), ItemModelUtils.plainModel(model));
        }
    }
}
