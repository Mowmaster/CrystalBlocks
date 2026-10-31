package com.mowmaster.crystalblocks.CBDataGeneration;

import com.mowmaster.mowlib.util.CBPixelBox;

import com.mowmaster.crystalblocks.Blocks.Colorable.CBTintableWindow;
import com.mowmaster.crystalblocks.Blocks.Colorable.CBTintableWindow.BendType;
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
import net.neoforged.neoforge.client.model.generators.template.FaceBuilder;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

// Multipart blockstate and models for CBTintableWindow:
// - straight windows: one of the six hand-made models (rotated in the pane's plane), plus 2x2 corner fill pieces
// - bends (L-shaped corner windows): generated models for the NORTH_WEST bend, turned for the other three, plus arm-end pieces
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

    // A small frame piece drawn when its block state property is on: property, model name, box
    private record FramePiece(BooleanProperty property, String name, CBPixelBox box) {}

    // Straight windows: fill pieces for a frame corner whose diagonal window is missing
    private static final FramePiece[] STRAIGHT_PIECES = {
            new FramePiece(CBTintableWindow.UP_LEFT, "corner_up_left", CBTintableWindow.CORNER_UP_LEFT),
            new FramePiece(CBTintableWindow.UP_RIGHT, "corner_up_right", CBTintableWindow.CORNER_UP_RIGHT),
            new FramePiece(CBTintableWindow.DOWN_LEFT, "corner_down_left", CBTintableWindow.CORNER_DOWN_LEFT),
            new FramePiece(CBTintableWindow.DOWN_RIGHT, "corner_down_right", CBTintableWindow.CORNER_DOWN_RIGHT)
    };

    // Bends: the same kind of fill piece at the far end of each arm (left = first arm, right = second arm)
    private static final FramePiece[] BEND_PIECES = {
            new FramePiece(CBTintableWindow.UP_LEFT, "bend_first_up", CBTintableWindow.BEND_FIRST_UP),
            new FramePiece(CBTintableWindow.UP_RIGHT, "bend_second_up", CBTintableWindow.BEND_SECOND_UP),
            new FramePiece(CBTintableWindow.DOWN_LEFT, "bend_first_down", CBTintableWindow.BEND_FIRST_DOWN),
            new FramePiece(CBTintableWindow.DOWN_RIGHT, "bend_second_down", CBTintableWindow.BEND_SECOND_DOWN)
    };

    public static void generate(BlockModelGenerators blockModels, Block window, String frameTexture)
    {
        // ---- Models (each created exactly once) ----

        // Straight windows: two copies of each hand-made model, clear cb_glass and tintable_glass
        Map<Shape, Identifier> clearGlass = new EnumMap<>(Shape.class);
        Map<Shape, Identifier> tintedGlass = new EnumMap<>(Shape.class);
        for (Shape shape : Shape.values())
        {
            clearGlass.put(shape, shape.template.create(
                    ModelLocationUtils.getModelLocation(window, "_" + shape.name),
                    textures(frameTexture,"cb_glass"), blockModels.modelOutput));
            tintedGlass.put(shape, shape.template.create(
                    ModelLocationUtils.getModelLocation(window, "_" + shape.name + "_tinted"),
                    textures(frameTexture,"tintable_glass"), blockModels.modelOutput));
        }

        // Fill pieces for both straight windows and bends
        Map<FramePiece, Identifier> pieceModels = new HashMap<>();
        for (FramePiece piece : STRAIGHT_PIECES) pieceModels.put(piece, frameBoxModel(blockModels, window, frameTexture, piece));
        for (FramePiece piece : BEND_PIECES) pieceModels.put(piece, frameBoxModel(blockModels, window, frameTexture, piece));

        // Bends: [joined above][joined below][tinted glass], 1 = yes
        Identifier[][][] bendModels = new Identifier[2][2][2];
        for (int up = 0; up < 2; up++)
        {
            for (int down = 0; down < 2; down++)
            {
                for (int tinted = 0; tinted < 2; tinted++)
                {
                    String suffix = "_bend" + (up == 1 ? "_up" : "") + (down == 1 ? "_down" : "") + (tinted == 1 ? "_tinted" : "");
                    bendModels[up][down][tinted] = bendTemplate(up == 1, down == 1).create(
                            ModelLocationUtils.getModelLocation(window, suffix),
                            textures(frameTexture, tinted == 1 ? "tintable_glass" : "cb_glass"), blockModels.modelOutput);
                }
            }
        }

        // ---- Blockstate ----

        MultiPartGenerator blockState = MultiPartGenerator.multiPart(window);

        // Straight windows: exactly one window part matches any straight state
        for (Direction facing : new Direction[] { Direction.NORTH, Direction.EAST })
        {
            int yTurns = CBTintableWindow.yTurns(facing);
            for (int joined = 0; joined < 16; joined++)
            {
                Choice choice = choose(joined);
                for (boolean tinted : new boolean[] { false, true })
                {
                    Identifier model = (tinted ? tintedGlass : clearGlass).get(choice.shape());
                    blockState.with(
                            BlockModelGenerators.condition()
                                    .term(CBTintableWindow.BEND, BendType.NONE)
                                    .term(CBTintableWindow.FACING, facing)
                                    .term(CBTintableWindow.UP, (joined & UP) != 0)
                                    .term(CBTintableWindow.DOWN, (joined & DOWN) != 0)
                                    .term(CBTintableWindow.LEFT, (joined & LEFT) != 0)
                                    .term(CBTintableWindow.RIGHT, (joined & RIGHT) != 0)
                                    .term(CBTintableWindow.TINTED_GLASS, tinted),
                            rotated(model, choice.xQuarterTurns(), yTurns));
                }
            }
            for (FramePiece piece : STRAIGHT_PIECES)
            {
                blockState.with(
                        BlockModelGenerators.condition()
                                .term(CBTintableWindow.BEND, BendType.NONE)
                                .term(CBTintableWindow.FACING, facing)
                                .term(piece.property(), true),
                        rotated(pieceModels.get(piece), 0, yTurns));
            }
        }

        // Bends: the L model for its joined-above/below and glass, plus any arm-end pieces, turned to the bend's direction
        for (BendType bend : BendType.values())
        {
            if (bend == BendType.NONE) continue;
            for (int up = 0; up < 2; up++)
            {
                for (int down = 0; down < 2; down++)
                {
                    for (int tinted = 0; tinted < 2; tinted++)
                    {
                        blockState.with(
                                BlockModelGenerators.condition()
                                        .term(CBTintableWindow.BEND, bend)
                                        .term(CBTintableWindow.UP, up == 1)
                                        .term(CBTintableWindow.DOWN, down == 1)
                                        .term(CBTintableWindow.TINTED_GLASS, tinted == 1),
                                rotated(bendModels[up][down][tinted], 0, bend.yTurns()));
                    }
                }
            }
            for (FramePiece piece : BEND_PIECES)
            {
                blockState.with(
                        BlockModelGenerators.condition()
                                .term(CBTintableWindow.BEND, bend)
                                .term(piece.property(), true),
                        rotated(pieceModels.get(piece), 0, bend.yTurns()));
            }
        }
        blockModels.blockStateOutput.accept(blockState);

        // The item is a single straight window with clear glass; both tint indexes start white
        Dye undyed = new Dye(0xFFFFFFFF);
        blockModels.itemModelOutput.accept(window.asItem(),
                ItemModelUtils.tintedModel(clearGlass.get(Shape.FULL), undyed, undyed));
    }

    // ---- Generated models ----

    // The L-shaped bend, as NORTH_WEST: a west arm and a north arm meeting in the middle, with no frame where they meet.
    // Top/bottom frames only where the bend isn't joined to another bend above/below. Arm ends are always joined.
    private static ModelTemplate bendTemplate(boolean joinedUp, boolean joinedDown)
    {
        ExtendedModelTemplateBuilder builder = ExtendedModelTemplateBuilder.builder()
                .parent(Identifier.withDefaultNamespace("block/block"))
                .requiredTextureSlot(TextureSlot.PARTICLE)
                .requiredTextureSlot(CBModelTemplates.TEXTURE_ONE)
                .requiredTextureSlot(CBModelTemplates.TEXTURE_TWO);

        if (!joinedUp)
        {
            addFrame(builder, CBTintableWindow.BEND_TOP_WEST);
            addFrame(builder, CBTintableWindow.BEND_TOP_NORTH);
        }
        if (!joinedDown)
        {
            addFrame(builder, CBTintableWindow.BEND_BOTTOM_WEST);
            addFrame(builder, CBTintableWindow.BEND_BOTTOM_NORTH);
        }

        // Glass on each arm's centre plane, running to the edge of the frame (or the block edge when joined)
        float glassBottom = joinedDown ? 0 : 2;
        float glassTop = joinedUp ? 16 : 14;
        Consumer<FaceBuilder> glass = face -> face.texture(CBModelTemplates.TEXTURE_TWO).tintindex(CBTintableWindow.GLASS_REGION);

        // West arm: plane z = 8, from the west edge to the centre
        builder.element(element -> element
                .from(0, glassBottom, 8).to(8, glassTop, 8)
                .face(Direction.NORTH, glass)
                .face(Direction.SOUTH, glass));
        // North arm: plane x = 8, from the north edge to the centre
        builder.element(element -> element
                .from(8, glassBottom, 0).to(8, glassTop, 8)
                .face(Direction.WEST, glass)
                .face(Direction.EAST, glass));

        return builder.build();
    }

    // A model containing a single frame box, using the frame texture and the frame's tint
    private static Identifier frameBoxModel(BlockModelGenerators blockModels, Block window, String frameTexture, FramePiece piece)
    {
        ExtendedModelTemplateBuilder builder = ExtendedModelTemplateBuilder.builder()
                .parent(Identifier.withDefaultNamespace("block/block"))
                .requiredTextureSlot(TextureSlot.PARTICLE)
                .requiredTextureSlot(CBModelTemplates.TEXTURE_ONE);
        addFrame(builder, piece.box());
        return builder.build().create(ModelLocationUtils.getModelLocation(window, "_" + piece.name()),
                textures(frameTexture, "cb_glass"), blockModels.modelOutput);
    }

    private static void addFrame(ExtendedModelTemplateBuilder builder, CBPixelBox box)
    {
        builder.element(element -> element
                .from((float) box.minX(), (float) box.minY(), (float) box.minZ())
                .to((float) box.maxX(), (float) box.maxY(), (float) box.maxZ())
                .allFaces((direction, face) -> face
                        .texture(CBModelTemplates.TEXTURE_ONE)
                        .tintindex(CBTintableWindow.FRAME_REGION)));
    }

    // ---- Straight window model choice ----

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

    // ---- Helpers ----

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

    // texture_one is the set's frame texture; texture_two is the given glass texture
    private static TextureMapping textures(String frameTexture, String glassTexture)
    {
        Material frame = new Material(Identifier.fromNamespaceAndPath(CBReferences.MODID, "block/" + frameTexture));
        Material glass = new Material(Identifier.fromNamespaceAndPath(CBReferences.MODID, "block/" + glassTexture));
        return TextureMapping.cube(frame)
                .put(CBModelTemplates.TEXTURE_ONE, frame)
                .put(CBModelTemplates.TEXTURE_TWO, glass);
    }
}