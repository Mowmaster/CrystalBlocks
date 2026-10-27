package com.mowmaster.crystalblocks.CBDataGeneration;

import com.mowmaster.crystalblocks.Blocks.CBBlockRegistry;
import com.mowmaster.crystalblocks.Blocks.Colorable.CBPixelBox;
import com.mowmaster.crystalblocks.Blocks.Colorable.CBTintableBarrel;
import com.mowmaster.crystalblocks.Blocks.Colorable.CBTintableChest;
import com.mowmaster.crystalblocks.CBReferences;
import net.minecraft.client.color.item.Dye;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.MultiVariant;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.blockstates.PropertyDispatch;
import net.minecraft.client.data.models.model.ItemModelUtils;
import net.minecraft.client.data.models.model.ModelLocationUtils;
import net.minecraft.client.data.models.model.ModelTemplate;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.state.properties.ChestType;

import java.util.EnumMap;
import java.util.Map;

// Models and blockstates for the tintable barrel and chest
public class CBStorageModels
{
    private static final ItemTintSource UNDYED = new Dye(0xFFFFFFFF);

    public static void generate(BlockModelGenerators blockModels)
    {
        barrel(blockModels, CBBlockRegistry.TINTABLE_BARREL.get());
        chest(blockModels, CBBlockRegistry.TINTABLE_CHEST.get());
        // texture_one / texture_two: pallet + ingots, and bark + end grain
        pile(blockModels, CBBlockRegistry.TINTABLE_INGOT_PILE.get(), CBModelTemplates.INGOT_PILE, "tintable_plank", "tintable_metal");
        pile(blockModels, CBBlockRegistry.TINTABLE_LOG_PILE.get(), CBModelTemplates.LOG_PILE, "tintable_log_one", "tintable_plank");
    }

    // One model, no rotations
    private static void pile(BlockModelGenerators blockModels, Block pile, ModelTemplate template, String textureOne, String textureTwo)
    {
        // texture_one = plank pallet, texture_two = metal ingots (the third slot isn't used by this model)
        TextureMapping textures = textures(textureOne, textureTwo, textureTwo);
        Identifier model = template.create(ModelLocationUtils.getModelLocation(pile, "_textured"), textures, blockModels.modelOutput);
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(pile, BlockModelGenerators.plainVariant(model)));
        blockModels.itemModelOutput.accept(pile.asItem(), ItemModelUtils.tintedModel(model, UNDYED, UNDYED));
    }
    
    // Closed and open models, turned for the 6-way facing like vanilla's barrel
    private static void barrel(BlockModelGenerators blockModels, Block barrel)
    {
        // texture_one = log sides, texture_two = metal straps, texture_three = plank inside
        TextureMapping textures = textures("tintable_log_one", "tintable_metal", "tintable_plank");
        Identifier closed = CBModelTemplates.BARREL_CLOSED.create(ModelLocationUtils.getModelLocation(barrel, "_closed"), textures, blockModels.modelOutput);
        Identifier open = CBModelTemplates.BARREL_OPEN.create(ModelLocationUtils.getModelLocation(barrel, "_open"), textures, blockModels.modelOutput);

        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(barrel)
                .with(PropertyDispatch.initial(CBTintableBarrel.FACING, CBTintableBarrel.OPEN, CBTintableBarrel.POWERED)
                        .generate((facing, isOpen, powered) -> rotated(isOpen || powered ? open : closed,
                                CBTintableBarrel.xQuarterTurns(facing), CBTintableBarrel.yQuarterTurns(facing)))));

        blockModels.itemModelOutput.accept(barrel.asItem(), ItemModelUtils.tintedModel(closed, UNDYED, UNDYED, UNDYED));
    }

    // The blockstate only shows the bottom; the lid models are drawn by CBTintableChestRenderer
    private static void chest(BlockModelGenerators blockModels, Block chest)
    {
        // texture_one = plank inside, texture_two = metal bands, texture_three = knob
        TextureMapping textures = textures("tintable_plank", "tintable_metal", "tintable_stone");

        Map<ChestType, Identifier> bottoms = new EnumMap<>(ChestType.class);
        Map<ChestType, Identifier> lids = new EnumMap<>(ChestType.class);
        for (ChestType type : ChestType.values())
        {
            String part = switch (type)
            {
                case SINGLE -> "";
                case RIGHT -> "_double_right";
                case LEFT -> "_double_left";
            };
            ModelTemplate bottom = switch (type)
            {
                case SINGLE -> CBModelTemplates.CHEST_BOTTOM;
                case RIGHT -> CBModelTemplates.CHEST_DOUBLE_RIGHT_BOTTOM;
                case LEFT -> CBModelTemplates.CHEST_DOUBLE_LEFT_BOTTOM;
            };
            ModelTemplate lid = switch (type)
            {
                case SINGLE -> CBModelTemplates.CHEST_LID;
                case RIGHT -> CBModelTemplates.CHEST_DOUBLE_RIGHT_LID;
                case LEFT -> CBModelTemplates.CHEST_DOUBLE_LEFT_LID;
            };
            // "_textured" keeps the names different from the hand-made parents
            bottoms.put(type, bottom.create(ModelLocationUtils.getModelLocation(chest, part + "_bottom_textured"), textures, blockModels.modelOutput));
            lids.put(type, lid.create(ModelLocationUtils.getModelLocation(chest, part + "_lid_textured"), textures, blockModels.modelOutput));
        }

        // waterlogged isn't listed: it doesn't change the model
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(chest)
                .with(PropertyDispatch.initial(ChestBlock.FACING, ChestBlock.TYPE)
                        .generate((facing, type) -> rotated(bottoms.get(type), 0, CBPixelBox.quarterTurns(facing)))));

        // The item is the single chest's bottom and lid together; the knob starts light grey
        ItemTintSource knob = new Dye(0xFF000000 | CBTintableChest.KNOB_DEFAULT);
        blockModels.itemModelOutput.accept(chest.asItem(), ItemModelUtils.composite(
                ItemModelUtils.tintedModel(bottoms.get(ChestType.SINGLE), UNDYED, UNDYED, knob),
                ItemModelUtils.tintedModel(lids.get(ChestType.SINGLE), UNDYED, UNDYED, knob)));
    }

    private static TextureMapping textures(String one, String two, String three)
    {
        Material first = material(one);
        return TextureMapping.cube(first)
                .put(CBModelTemplates.TEXTURE_ONE, first)
                .put(CBModelTemplates.TEXTURE_TWO, material(two))
                .put(CBModelTemplates.TEXTURE_THREE, material(three));
    }

    private static Material material(String texture)
    {
        return new Material(Identifier.fromNamespaceAndPath(CBReferences.MODID, "block/" + texture));
    }

    // x first, then y, the same order the game applies them
    private static MultiVariant rotated(Identifier model, int xQuarterTurns, int yQuarterTurns)
    {
        MultiVariant variant = BlockModelGenerators.plainVariant(model);
        variant = switch (xQuarterTurns % 4)
        {
            case 1 -> variant.with(BlockModelGenerators.X_ROT_90);
            case 2 -> variant.with(BlockModelGenerators.X_ROT_180);
            case 3 -> variant.with(BlockModelGenerators.X_ROT_270);
            default -> variant;
        };
        return switch (yQuarterTurns % 4)
        {
            case 1 -> variant.with(BlockModelGenerators.Y_ROT_90);
            case 2 -> variant.with(BlockModelGenerators.Y_ROT_180);
            case 3 -> variant.with(BlockModelGenerators.Y_ROT_270);
            default -> variant;
        };
    }
}
