package com.mowmaster.crystalblocks.CBDataGeneration;

import com.mowmaster.crystalblocks.Blocks.Colorable.CBPixelBox;
import com.mowmaster.crystalblocks.Blocks.Colorable.CBTintableWindow;
import com.mowmaster.crystalblocks.CBReferences;
import net.minecraft.client.color.item.Dye;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.MultiVariant;
import net.minecraft.client.data.models.blockstates.MultiPartGenerator;
import net.minecraft.client.data.models.model.ItemModelUtils;
import net.minecraft.client.data.models.model.ModelLocationUtils;
import net.minecraft.client.data.models.model.ModelTemplate;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.neoforged.neoforge.client.model.generators.template.ExtendedModelTemplateBuilder;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

// Multipart blockstate and models for CBTintableWindow: one of the six hand-made models (rotated in the pane's plane)
// for the joined edges, plus a small corner piece wherever a corner of the frame needs filling
public class CBWindowModels
{
    // Joined edges as bit flags, as the model sees them (before any rotation): left = the model's north edge, right = its south edge
    private static final int UP = 1;
    private static final int DOWN = 2;
    private static final int LEFT = 4;
    private static final int RIGHT = 8;

    // Each hand-made model and the edges it has no frame on (= joined), as modelled
    private enum Shape
    {
        FULL(CBModelTemplates.WINDOW_FULL, "full", 0),
        ONE(CBModelTemplates.WINDOW_ONE, "one", DOWN),
        TWO(CBModelTemplates.WINDOW_TWO, "two", UP | DOWN),
        THREE(CBModelTemplates.WINDOW_THREE, "three", UP | LEFT),
        FOUR(CBModelTemplates.WINDOW_FOUR, "four", UP | DOWN | RIGHT),
        FIVE(CBModelTemplates.WINDOW_FIVE, "five", UP | DOWN | LEFT | RIGHT);

        final ModelTemplate template;
        final String name;
        final int joined;

        Shape(ModelTemplate template, String name, int joined)
        {
            this.template = template;
            this.name = name;
            this.joined = joined;
        }
    }

    // A model plus how many blockstate "x" quarter turns it needs
    private record Choice(Shape shape, int xQuarterTurns) {}

    // A corner piece: its block state property, model name, and box
    private record Corner(BooleanProperty property, String name, CBPixelBox box) {}

    private static final Corner[] CORNERS = {
            new Corner(CBTintableWindow.UP_LEFT, "up_left", CBTintableWindow.CORNER_UP_LEFT),
            new Corner(CBTintableWindow.UP_RIGHT, "up_right", CBTintableWindow.CORNER_UP_RIGHT),
            new Corner(CBTintableWindow.DOWN_LEFT, "down_left", CBTintableWindow.CORNER_DOWN_LEFT),
            new Corner(CBTintableWindow.DOWN_RIGHT, "down_right", CBTintableWindow.CORNER_DOWN_RIGHT)
    };

    public static void generate(BlockModelGenerators blockModels, Block window)
    {
        // Two copies of each window model: clear cb_glass, and tintable_glass for once the glass has been dyed
        Map<Shape, Identifier> clearGlass = new EnumMap<>(Shape.class);
        Map<Shape, Identifier> tintedGlass = new EnumMap<>(Shape.class);
        for (Shape shape : Shape.values())
        {
            clearGlass.put(shape, shape.template.create(
                    ModelLocationUtils.getModelLocation(window, "_" + shape.name),
                    textures("cb_glass"), blockModels.modelOutput));
            tintedGlass.put(shape, shape.template.create(
                    ModelLocationUtils.getModelLocation(window, "_" + shape.name + "_tinted"),
                    textures("tintable_glass"), blockModels.modelOutput));
        }

        // Corner piece models: one per corner, shared by both facings (only the rotation differs)
        Map<Corner, Identifier> cornerModels = new HashMap<>();
        for (Corner corner : CORNERS)
        {
            cornerModels.put(corner, cornerModel(blockModels, window, corner));
        }

        MultiPartGenerator blockState = MultiPartGenerator.multiPart(window);
        for (Direction facing : new Direction[] { Direction.NORTH, Direction.EAST })
        {
            int yTurns = CBTintableWindow.yTurns(facing);

            // The window itself: exactly one of these parts matches any given state
            for (int joined = 0; joined < 16; joined++)
            {
                Choice choice = choose(joined);
                for (boolean tinted : new boolean[] { false, true })
                {
                    Identifier model = (tinted ? tintedGlass : clearGlass).get(choice.shape());
                    blockState.with(
                            BlockModelGenerators.condition()
                                    .term(CBTintableWindow.FACING, facing)
                                    .term(CBTintableWindow.UP, (joined & UP) != 0)
                                    .term(CBTintableWindow.DOWN, (joined & DOWN) != 0)
                                    .term(CBTintableWindow.LEFT, (joined & LEFT) != 0)
                                    .term(CBTintableWindow.RIGHT, (joined & RIGHT) != 0)
                                    .term(CBTintableWindow.TINTED_GLASS, tinted),
                            rotated(model, choice.xQuarterTurns(), yTurns));
                }
            }

            // Corner pieces, drawn on top of the window whenever their property is on
            for (Corner corner : CORNERS)
            {
                blockState.with(
                        BlockModelGenerators.condition()
                                .term(CBTintableWindow.FACING, facing)
                                .term(corner.property(), true),
                        rotated(cornerModels.get(corner), 0, yTurns));
            }
        }
        blockModels.blockStateOutput.accept(blockState);

        // The item is a single window with clear glass; both tint indexes start white
        Dye undyed = new Dye(0xFFFFFFFF);
        blockModels.itemModelOutput.accept(window.asItem(),
                ItemModelUtils.tintedModel(clearGlass.get(Shape.FULL), undyed, undyed));
    }

    // A 2x2 frame piece in one corner, using the frame texture and the frame's tint
    private static Identifier cornerModel(BlockModelGenerators blockModels, Block window, Corner corner)
    {
        CBPixelBox box = corner.box();
        ModelTemplate template = ExtendedModelTemplateBuilder.builder()
                .parent(Identifier.withDefaultNamespace("block/block"))
                .requiredTextureSlot(TextureSlot.PARTICLE)
                .requiredTextureSlot(CBModelTemplates.TEXTURE_ONE)
                .element(element -> element
                        .from((float) box.minX(), (float) box.minY(), (float) box.minZ())
                        .to((float) box.maxX(), (float) box.maxY(), (float) box.maxZ())
                        .allFaces((direction, face) -> face
                                .texture(CBModelTemplates.TEXTURE_ONE)
                                .tintindex(CBTintableWindow.FRAME_REGION)))
                .build();
        return template.create(ModelLocationUtils.getModelLocation(window, "_corner_" + corner.name()),
                textures("cb_glass"), blockModels.modelOutput);
    }

    // Finds the model and in-plane rotation whose open edges match the joined edges exactly
    private static Choice choose(int joined)
    {
        for (Shape shape : Shape.values())
        {
            int rotated = shape.joined;
            for (int turns = 0; turns < 4; turns++)
            {
                if (rotated == joined) return new Choice(shape, turns);
                rotated = rotateOnce(rotated);
            }
        }
        throw new IllegalStateException("No window model for joined edges " + joined);
    }

    // What one blockstate "x" quarter turn does to the model's edges: up -> left, left -> down, down -> right, right -> up
    private static int rotateOnce(int edges)
    {
        int result = 0;
        if ((edges & UP) != 0) result |= LEFT;
        if ((edges & LEFT) != 0) result |= DOWN;
        if ((edges & DOWN) != 0) result |= RIGHT;
        if ((edges & RIGHT) != 0) result |= UP;
        return result;
    }

    // x first, then y, the same order the game applies them
    private static MultiVariant rotated(Identifier model, int xQuarterTurns, int yQuarterTurns)
    {
        MultiVariant variant = BlockModelGenerators.plainVariant(model);
        variant = switch (xQuarterTurns)
        {
            case 1 -> variant.with(BlockModelGenerators.X_ROT_90);
            case 2 -> variant.with(BlockModelGenerators.X_ROT_180);
            case 3 -> variant.with(BlockModelGenerators.X_ROT_270);
            default -> variant;
        };
        return yQuarterTurns == 1 ? variant.with(BlockModelGenerators.Y_ROT_90) : variant;
    }

    // texture_one is always the stone frame; texture_two is the given glass texture
    private static TextureMapping textures(String glassTexture)
    {
        Material frame = new Material(Identifier.fromNamespaceAndPath(CBReferences.MODID, "block/tintable_stone"));
        Material glass = new Material(Identifier.fromNamespaceAndPath(CBReferences.MODID, "block/" + glassTexture));
        return TextureMapping.cube(frame)
                .put(CBModelTemplates.TEXTURE_ONE, frame)
                .put(CBModelTemplates.TEXTURE_TWO, glass);
    }
}