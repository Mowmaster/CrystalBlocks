package com.mowmaster.crystalblocks.CBDataGeneration;

import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.model.ItemModelUtils;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.blockstates.PropertyDispatch;
import net.minecraft.client.data.models.model.TextureSlot;
import com.mowmaster.crystalblocks.Blocks.Colorable.CBAccentedPillarCap;
import net.minecraft.client.data.models.MultiVariant;
import com.mowmaster.crystalblocks.Blocks.Colorable.CBAccentedPillar;

import java.util.Arrays;

public class CBTintedBlockFamily
{
    private final BlockModelGenerators blockModels;
    private final ItemTintSource itemTint;
    private final TextureMapping textures;
    private final Identifier fullBlockModel;

    // Constant tint (your existing crystal stone colours)
    public CBTintedBlockFamily(BlockModelGenerators blockModels, Block baseBlock, int tint)
    {
        this(blockModels, baseBlock, ItemModelUtils.constantTint(tint));
    }

    // Any item tint source, e.g. new Dye(...) for dyeable blocks
    public CBTintedBlockFamily(BlockModelGenerators blockModels, Block baseBlock, ItemTintSource itemTint)
    {
        this.blockModels = blockModels;
        this.itemTint = itemTint;
        // Texture defaults to <modid>:block/<base block name>, e.g. crystalblocks:block/crystal_stone_red
        this.textures = TextureMapping.cube(baseBlock);

        this.fullBlockModel = CBModelTemplates.TINTED_CUBE_ALL.create(baseBlock, textures, blockModels.modelOutput);
        blockModels.blockStateOutput.accept(
                BlockModelGenerators.createSimpleBlock(baseBlock, BlockModelGenerators.plainVariant(fullBlockModel)));
        tintedItem(baseBlock, fullBlockModel);
    }

    /*// Generates the tinted full cube for baseBlock. Every variant added after this reuses baseBlock's texture.
    public CBTintedBlockFamily(BlockModelGenerators blockModels, Block baseBlock, int tint)
    {
        this.blockModels = blockModels;
        this.tint = tint;
        // Texture defaults to <modid>:block/<base block name>, e.g. crystalblocks:block/crystal_stone_red
        this.textures = TextureMapping.cube(baseBlock);

        this.fullBlockModel = CBModelTemplates.TINTED_CUBE_ALL.create(baseBlock, textures, blockModels.modelOutput);
        blockModels.blockStateOutput.accept(
                BlockModelGenerators.createSimpleBlock(baseBlock, BlockModelGenerators.plainVariant(fullBlockModel)));
        tintedItem(baseBlock, fullBlockModel);
    }*/

    public CBTintedBlockFamily stairs(Block stairs)
    {
        Identifier inner = CBModelTemplates.TINTED_STAIRS_INNER.create(stairs, textures, blockModels.modelOutput);
        Identifier straight = CBModelTemplates.TINTED_STAIRS_STRAIGHT.create(stairs, textures, blockModels.modelOutput);
        Identifier outer = CBModelTemplates.TINTED_STAIRS_OUTER.create(stairs, textures, blockModels.modelOutput);

        blockModels.blockStateOutput.accept(BlockModelGenerators.createStairs(stairs,
                BlockModelGenerators.plainVariant(inner),
                BlockModelGenerators.plainVariant(straight),
                BlockModelGenerators.plainVariant(outer)));
        tintedItem(stairs, straight);
        return this;
    }

    public CBTintedBlockFamily slab(Block slab)
    {
        Identifier bottom = CBModelTemplates.TINTED_SLAB_BOTTOM.create(slab, textures, blockModels.modelOutput);
        Identifier top = CBModelTemplates.TINTED_SLAB_TOP.create(slab, textures, blockModels.modelOutput);

        // A double slab just shows the full block model
        blockModels.blockStateOutput.accept(BlockModelGenerators.createSlab(slab,
                BlockModelGenerators.plainVariant(bottom),
                BlockModelGenerators.plainVariant(top),
                BlockModelGenerators.plainVariant(fullBlockModel)));
        tintedItem(slab, bottom);
        return this;
    }

    // Slab whose double form tints each half separately: tintindex 0 = bottom half, 1 = top half
    public CBTintedBlockFamily twoToneSlab(Block slab)
    {
        Identifier bottom = CBModelTemplates.TINTED_SLAB_BOTTOM.create(slab, textures, blockModels.modelOutput);
        Identifier top = CBModelTemplates.TINTED_SLAB_TOP.create(slab, textures, blockModels.modelOutput);
        Identifier doubleSlab = CBModelTemplates.TINTED_SLAB_DOUBLE.create(slab, textures, blockModels.modelOutput);

        blockModels.blockStateOutput.accept(BlockModelGenerators.createSlab(slab,
                BlockModelGenerators.plainVariant(bottom),
                BlockModelGenerators.plainVariant(top),
                BlockModelGenerators.plainVariant(doubleSlab)));
        tintedItem(slab, bottom);
        return this;
    }

    // One north-facing model, rotated for each FACING value
    public CBTintedBlockFamily verticalSlab(Block verticalSlab)
    {
        Identifier model = CBModelTemplates.TINTED_VERTICAL_SLAB.create(verticalSlab, textures, blockModels.modelOutput);

        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(verticalSlab,
                        BlockModelGenerators.plainVariant(model).with(BlockModelGenerators.UV_LOCK))
                .with(BlockModelGenerators.ROTATION_HORIZONTAL_FACING));
        tintedItem(verticalSlab, model);
        return this;
    }



    public CBTintedBlockFamily accentedPillar(Block pillar)
    {
        Identifier model = CBModelTemplates.ACCENTED_PILLAR.create(pillar, accentTextures(), blockModels.modelOutput);
        pillarBlockState(pillar, model);
        multiToneItem(pillar, model, 2);
        return this;
    }

    public CBTintedBlockFamily accentedPillarCap(Block cap)
    {
        Identifier model = CBModelTemplates.ACCENTED_PILLAR_CAP.create(cap, accentTextures(), blockModels.modelOutput);
        capBlockState(cap, model);
        multiToneItem(cap, model, 2);
        return this;
    }

    public CBTintedBlockFamily steppedPillar(Block pillar)
    {
        Identifier model = CBModelTemplates.STEPPED_PILLAR.create(pillar, accentTextures(), blockModels.modelOutput);
        pillarBlockState(pillar, model);
        multiToneItem(pillar, model, 4);
        return this;
    }

    public CBTintedBlockFamily steppedPillarCap(Block cap)
    {
        Identifier model = CBModelTemplates.STEPPED_PILLAR_CAP.create(cap, accentTextures(), blockModels.modelOutput);
        capBlockState(cap, model);
        multiToneItem(cap, model, 4);
        return this;
    }

    // Wall / floor / ceiling pillar rotations (shared by every CBAccentedPillar)
    private void pillarBlockState(Block pillar, Identifier model)
    {
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(pillar)
                .with(PropertyDispatch.initial(CBAccentedPillar.FACING, CBAccentedPillar.AXIS)
                        .generate((facing, axis) -> rotatedVariant(model,
                                CBAccentedPillar.xTurnsFor(facing),
                                CBAccentedPillar.yTurnsFor(facing, axis)))));
    }

    // Stair-like cap rotations (shared by every CBAccentedPillarCap)
    private void capBlockState(Block cap, Identifier model)
    {
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(cap)
                .with(PropertyDispatch.initial(CBAccentedPillarCap.FACING, CBAccentedPillarCap.HALF)
                        .generate((facing, half) -> rotatedVariant(model,
                                CBAccentedPillarCap.xTurnsFor(half),
                                CBAccentedPillarCap.yTurnsFor(facing, half)))));
    }

    // Item with every tint index using the family's item tint
    private void multiToneItem(Block block, Identifier model, int tintCount)
    {
        ItemTintSource[] tints = new ItemTintSource[tintCount];
        Arrays.fill(tints, itemTint);
        blockModels.itemModelOutput.accept(block.asItem(), ItemModelUtils.tintedModel(model, tints));
    }

    // A north-facing model rotated by x then y quarter turns, the same order the game applies them
    private static MultiVariant rotatedVariant(Identifier model, int xQuarterTurns, int yQuarterTurns)
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

    // Both texture variables use the family's base texture; the two tints are what make them look different
    private TextureMapping accentTextures()
    {
        var texture = textures.get(TextureSlot.ALL);
        return textures
                .copyAndUpdate(CBModelTemplates.TEXTURE_ONE, texture)
                .copyAndUpdate(CBModelTemplates.TEXTURE_TWO, texture);
    }

    public CBTintedBlockFamily wall(Block wall)
    {
        Identifier post = CBModelTemplates.TINTED_WALL_POST.create(wall, textures, blockModels.modelOutput);
        Identifier low = CBModelTemplates.TINTED_WALL_LOW_SIDE.create(wall, textures, blockModels.modelOutput);
        Identifier tall = CBModelTemplates.TINTED_WALL_TALL_SIDE.create(wall, textures, blockModels.modelOutput);
        Identifier inventory = CBModelTemplates.TINTED_WALL_INVENTORY.create(wall, textures, blockModels.modelOutput);

        blockModels.blockStateOutput.accept(BlockModelGenerators.createWall(wall,
                BlockModelGenerators.plainVariant(post),
                BlockModelGenerators.plainVariant(low),
                BlockModelGenerators.plainVariant(tall)));
        tintedItem(wall, inventory);
        return this;
    }

    public CBTintedBlockFamily button(Block button)
    {
        Identifier unpressed = CBModelTemplates.TINTED_BUTTON.create(button, textures, blockModels.modelOutput);
        Identifier pressed = CBModelTemplates.TINTED_BUTTON_PRESSED.create(button, textures, blockModels.modelOutput);
        Identifier inventory = CBModelTemplates.TINTED_BUTTON_INVENTORY.create(button, textures, blockModels.modelOutput);

        blockModels.blockStateOutput.accept(BlockModelGenerators.createButton(button,
                BlockModelGenerators.plainVariant(unpressed),
                BlockModelGenerators.plainVariant(pressed)));
        tintedItem(button, inventory);
        return this;
    }

    public CBTintedBlockFamily pressurePlate(Block pressurePlate)
    {
        Identifier up = CBModelTemplates.TINTED_PRESSURE_PLATE_UP.create(pressurePlate, textures, blockModels.modelOutput);
        Identifier down = CBModelTemplates.TINTED_PRESSURE_PLATE_DOWN.create(pressurePlate, textures, blockModels.modelOutput);

        blockModels.blockStateOutput.accept(BlockModelGenerators.createPressurePlate(pressurePlate,
                BlockModelGenerators.plainVariant(up),
                BlockModelGenerators.plainVariant(down)));
        tintedItem(pressurePlate, up);
        return this;
    }

    // Writes items/<name>.json pointing at the given model, with the family's tint on tint index 0
    private void tintedItem(Block block, Identifier model)
    {
        blockModels.itemModelOutput.accept(block.asItem(), ItemModelUtils.tintedModel(model, itemTint));
    }
}