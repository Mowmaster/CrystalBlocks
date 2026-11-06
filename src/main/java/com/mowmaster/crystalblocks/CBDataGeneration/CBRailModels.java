package com.mowmaster.crystalblocks.CBDataGeneration;

import com.mowmaster.crystalblocks.Blocks.CBBlockRegistry;
import com.mowmaster.crystalblocks.Blocks.Colorable.CBTintableRail;
import com.mowmaster.crystalblocks.CBReferences;
import net.minecraft.client.color.item.Dye;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.MultiVariant;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.blockstates.PropertyDispatch;
import net.minecraft.client.data.models.model.ItemModelUtils;
import net.minecraft.client.data.models.model.ModelLocationUtils;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RailBlock;

// The Crystal Rail's four models (flat, raised towards north or south, corner) and its blockstate, laid out like
// vanilla's rail: east-west and the corners are the same models turned.
public class CBRailModels
{
    public static void generate(BlockModelGenerators blockModels)
    {
        Block rail = CBBlockRegistry.TINTABLE_RAIL.get();
        // texture_one = metal (rails and nails), texture_two = the 32px wood (ties)
        Material metal = material("tintable_metal");
        TextureMapping textures = TextureMapping.cube(metal)
                .put(CBModelTemplates.TEXTURE_ONE, metal)
                .put(CBModelTemplates.TEXTURE_TWO, material("tintable_log_large"));

        Identifier flat = CBModelTemplates.RAIL_FLAT.create(ModelLocationUtils.getModelLocation(rail, "_flat"), textures, blockModels.modelOutput);
        Identifier raisedNE = CBModelTemplates.RAIL_RAISED_NE.create(ModelLocationUtils.getModelLocation(rail, "_raised_ne"), textures, blockModels.modelOutput);
        Identifier raisedSW = CBModelTemplates.RAIL_RAISED_SW.create(ModelLocationUtils.getModelLocation(rail, "_raised_sw"), textures, blockModels.modelOutput);
        Identifier corner = CBModelTemplates.RAIL_CORNER.create(ModelLocationUtils.getModelLocation(rail, "_corner"), textures, blockModels.modelOutput);

        // waterlogged isn't listed: it doesn't change the model.
        // Keep these turns in step with CBTintableRail#getTintRegion.
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(rail)
                .with(PropertyDispatch.initial(RailBlock.SHAPE).generate(shape -> switch (shape)
                {
                    case NORTH_SOUTH -> turned(flat, 0);
                    case EAST_WEST -> turned(flat, 1);
                    case ASCENDING_NORTH -> turned(raisedNE, 0);
                    case ASCENDING_SOUTH -> turned(raisedSW, 0);
                    case ASCENDING_EAST -> turned(raisedNE, 1);
                    case ASCENDING_WEST -> turned(raisedSW, 1);
                    // The corner is modelled joining north and west
                    case NORTH_WEST -> turned(corner, 0);
                    case NORTH_EAST -> turned(corner, 1);
                    case SOUTH_EAST -> turned(corner, 2);
                    case SOUTH_WEST -> turned(corner, 3);
                })));

        // The item is the flat rail; the nails start grey
        Dye undyed = new Dye(0xFFFFFFFF);
        Dye grey = new Dye(0xFF000000 | DyeColor.GRAY.getTextureDiffuseColor());
        blockModels.itemModelOutput.accept(rail.asItem(), ItemModelUtils.tintedModel(flat, undyed, undyed, grey));
    }

    private static MultiVariant turned(Identifier model, int yQuarterTurns)
    {
        MultiVariant variant = BlockModelGenerators.plainVariant(model);
        return switch (yQuarterTurns)
        {
            case 1 -> variant.with(BlockModelGenerators.Y_ROT_90);
            case 2 -> variant.with(BlockModelGenerators.Y_ROT_180);
            case 3 -> variant.with(BlockModelGenerators.Y_ROT_270);
            default -> variant;
        };
    }

    private static Material material(String texture)
    {
        return new Material(Identifier.fromNamespaceAndPath(CBReferences.MODID, "block/" + texture));
    }
}
