package com.mowmaster.crystalblocks.CBDataGeneration;

import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.model.ItemModelUtils;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import com.mowmaster.crystalblocks.Blocks.Colorable.CBTintableColumn;
import net.minecraft.client.data.models.blockstates.PropertyDispatch;
import net.minecraft.client.data.models.model.ModelTemplate;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.client.model.generators.template.ExtendedModelTemplateBuilder;
import com.mowmaster.crystalblocks.Blocks.Colorable.CBTintableHorizontalColumn;

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

    // One model per position (81 for an 8px column), selected by the block's x and z properties
    public CBTintedBlockFamily column(Block column)
    {
        int maxOffset = CBTintableColumn.MAX_OFFSET;
        Identifier[][] models = new Identifier[maxOffset + 1][maxOffset + 1];
        for (int x = 0; x <= maxOffset; x++)
        {
            for (int z = 0; z <= maxOffset; z++)
            {
                models[x][z] = columnTemplate(x, z).create(column, textures, blockModels.modelOutput);
            }
        }

        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(column)
                .with(PropertyDispatch.initial(CBTintableColumn.X, CBTintableColumn.Z)
                        .generate((x, z) -> BlockModelGenerators.plainVariant(models[x][z]))));
        // The item shows the centred column
        tintedItem(column, models[maxOffset / 2][maxOffset / 2]);
        return this;
    }

    // One model per axis and position (2 x 81), selected by the block's axis, side and y properties
    public CBTintedBlockFamily horizontalColumn(Block column)
    {
        int maxOffset = CBTintableHorizontalColumn.MAX_OFFSET;
        // [0] = running along X, [1] = running along Z
        Identifier[][][] models = new Identifier[2][maxOffset + 1][maxOffset + 1];
        for (Direction.Axis axis : new Direction.Axis[] { Direction.Axis.X, Direction.Axis.Z })
        {
            for (int side = 0; side <= maxOffset; side++)
            {
                for (int y = 0; y <= maxOffset; y++)
                {
                    models[axisIndex(axis)][side][y] =
                            horizontalColumnTemplate(axis, side, y).create(column, textures, blockModels.modelOutput);
                }
            }
        }

        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(column)
                .with(PropertyDispatch.initial(CBTintableHorizontalColumn.AXIS, CBTintableHorizontalColumn.SIDE, CBTintableHorizontalColumn.Y)
                        .generate((axis, side, y) -> BlockModelGenerators.plainVariant(models[axisIndex(axis)][side][y]))));
        // The item shows a centred north-south beam
        tintedItem(column, models[axisIndex(Direction.Axis.Z)][maxOffset / 2][maxOffset / 2]);
        return this;
    }

    private static int axisIndex(Direction.Axis axis)
    {
        return axis == Direction.Axis.X ? 0 : 1;
    }

    private static ModelTemplate horizontalColumnTemplate(Direction.Axis axis, int side, int y)
    {
        int size = CBTintableHorizontalColumn.SIZE;
        boolean alongX = axis == Direction.Axis.X;
        int minX = alongX ? 0 : side;
        int maxX = alongX ? 16 : side + size;
        int minZ = alongX ? side : 0;
        int maxZ = alongX ? side + size : 16;

        return ExtendedModelTemplateBuilder.builder()
                .parent(Identifier.withDefaultNamespace("block/block"))
                .suffix("_" + axis.getSerializedName() + "_s" + side + "_y" + y)
                .requiredTextureSlot(TextureSlot.PARTICLE)
                .requiredTextureSlot(TextureSlot.TOP)
                .requiredTextureSlot(TextureSlot.SIDE)
                .element(element -> element
                        .from(minX, y, minZ)
                        .to(maxX, y + size, maxZ)
                        .allFaces((direction, face) -> face
                                // The two ends use the "top" texture, like a log's end grain
                                .texture(direction.getAxis() == axis ? TextureSlot.TOP : TextureSlot.SIDE)
                                .tintindex(0)
                                .cullface(isOnBlockEdge(direction, minX, y, minZ, maxX, y + size, maxZ) ? direction : null)))
                .build();
    }

    // True when the element's face in this direction lies on the outside of the block space
    private static boolean isOnBlockEdge(Direction direction, int minX, int minY, int minZ, int maxX, int maxY, int maxZ)
    {
        return switch (direction)
        {
            case DOWN -> minY == 0;
            case UP -> maxY == 16;
            case WEST -> minX == 0;
            case EAST -> maxX == 16;
            case NORTH -> minZ == 0;
            case SOUTH -> maxZ == 16;
        };
    }

    // Builds the model for a column whose west/north edge sits x/z pixels from the block's corner
    private static ModelTemplate columnTemplate(int x, int z)
    {
        int size = CBTintableColumn.SIZE;
        return ExtendedModelTemplateBuilder.builder()
                .parent(Identifier.withDefaultNamespace("block/block"))
                .suffix("_x" + x + "_z" + z)
                .requiredTextureSlot(TextureSlot.PARTICLE)
                .requiredTextureSlot(TextureSlot.TOP)
                .requiredTextureSlot(TextureSlot.BOTTOM)
                .requiredTextureSlot(TextureSlot.SIDE)
                .element(element -> element
                        .from(x, 0, z)
                        .to(x + size, 16, z + size)
                        .allFaces((direction, face) -> face
                                .texture(switch (direction)
                                {
                                    case UP -> TextureSlot.TOP;
                                    case DOWN -> TextureSlot.BOTTOM;
                                    default -> TextureSlot.SIDE;
                                })
                                .tintindex(0)
                                // Only cull faces that lie on the block's edge; inner faces are always visible
                                .cullface(touchesBlockEdge(direction, x, z, size) ? direction : null)))
                .build();
    }

    private static boolean touchesBlockEdge(Direction direction, int x, int z, int size)
    {
        return switch (direction)
        {
            case DOWN, UP -> true;
            case WEST -> x == 0;
            case EAST -> x + size == 16;
            case NORTH -> z == 0;
            case SOUTH -> z + size == 16;
        };
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