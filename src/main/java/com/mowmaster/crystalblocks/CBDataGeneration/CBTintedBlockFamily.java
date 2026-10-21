package com.mowmaster.crystalblocks.CBDataGeneration;

import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.model.ItemModelUtils;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;

public class CBTintedBlockFamily
{
    private final BlockModelGenerators blockModels;
    private final int tint;
    private final TextureMapping textures;
    private final Identifier fullBlockModel;

    // Generates the tinted full cube for baseBlock. Every variant added after this reuses baseBlock's texture.
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
    }

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

    // Writes items/<name>.json pointing at the given model, with a constant tint on tint index 0
    private void tintedItem(Block block, Identifier model)
    {
        blockModels.itemModelOutput.accept(block.asItem(),
                ItemModelUtils.tintedModel(model, ItemModelUtils.constantTint(tint)));
    }
}