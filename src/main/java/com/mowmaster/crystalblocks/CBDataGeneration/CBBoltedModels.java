package com.mowmaster.crystalblocks.CBDataGeneration;

import com.mowmaster.crystalblocks.Blocks.CBBlockRegistry;
import com.mowmaster.crystalblocks.Blocks.Colorable.CBAccentedPillarCap;
import com.mowmaster.crystalblocks.Blocks.Colorable.CBBoltedBeamBlock;
import com.mowmaster.crystalblocks.Blocks.Colorable.CBBoltedSupportBeam;
import com.mowmaster.crystalblocks.Blocks.Colorable.CBChainedSupportBeam;
import com.mowmaster.crystalblocks.Blocks.Colorable.CBPixelBox;
import com.mowmaster.crystalblocks.CBReferences;
import net.minecraft.client.color.item.Dye;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.MultiVariant;
import net.minecraft.client.data.models.blockstates.MultiPartGenerator;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.blockstates.PropertyDispatch;
import net.minecraft.client.data.models.model.*;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.model.generators.template.ExtendedModelTemplateBuilder;
import net.neoforged.neoforge.common.util.TransformationHelper;
import org.joml.Quaternionf;

import java.util.*;
import java.util.function.Function;

// Models and blockstates for the bolted supports and chains (fixed metal + stone / metal + log textures)
public class CBBoltedModels
{
    // Which hand-made model a state uses, a name for its copies, and any extra quarter turns around the beam's
    // length (e.g. half a turn to show a one-sided part on the other side)
    public record ModelChoice(String name, Identifier parent, int extraTurns) {}

    public static void generate(BlockModelGenerators blockModels)
    {
        TextureMapping metalAndStone = textures("tintable_metal", "tintable_stone");
        TextureMapping metalAndLog = textures("tintable_metal", "tintable_log_one");
        TextureMapping metalOnly = textures("tintable_metal", "tintable_metal");

        cornerSupportCap(blockModels, (CBAccentedPillarCap) CBBlockRegistry.BOLTED_CORNER_SUPPORT_CAP.get(), metalAndStone);
        beam(blockModels, (CBBoltedBeamBlock) CBBlockRegistry.BOLTED_I_BEAM.get(), model("bolted_i_beam"), metalAndStone);
        beam(blockModels, (CBBoltedBeamBlock) CBBlockRegistry.BOLTED_I_BEAM_CAP.get(), model("bolted_i_beam_cap"), metalAndStone);
        beam(blockModels, (CBBoltedBeamBlock) CBBlockRegistry.BOLTED_SUPPORT_BEAM.get(), model("bolted_support_beam"), metalAndLog);
        beam(blockModels, (CBBoltedBeamBlock) CBBlockRegistry.BOLTED_SUPPORT_BEAM_CAP.get(), model("bolted_support_beam_cap"), metalAndLog);
        beam(blockModels, (CBBoltedBeamBlock) CBBlockRegistry.CHAIN_BEAM.get(), model("chain_beam"), metalOnly);
        beam(blockModels, (CBBoltedBeamBlock) CBBlockRegistry.CHAIN_BEAM_CAP.get(), model("chain_beam_cap"), metalOnly);

        beam(blockModels, (CBBoltedBeamBlock) CBBlockRegistry.CHAIN_BEAM_MEDIUM.get(), model("chain_beam_medium"), metalOnly);
        beam(blockModels, (CBBoltedBeamBlock) CBBlockRegistry.CHAIN_BEAM_MEDIUM_CAP.get(), model("chain_beam_medium_cap"), metalOnly);
        beam(blockModels, (CBBoltedBeamBlock) CBBlockRegistry.CHAIN_BEAM_LARGE.get(), model("chain_beam_large"), metalOnly);
        beam(blockModels, (CBBoltedBeamBlock) CBBlockRegistry.CHAIN_BEAM_LARGE_CAP.get(), model("chain_beam_large_cap"), metalOnly);
        chainedSupportBeam(blockModels, (CBChainedSupportBeam) CBBlockRegistry.CHAINED_SUPPORT_BEAM_MEDIUM.get(),
                model("bolted_support_beam"), model("chain_beam_medium_half"), metalAndLog);

        // The chained support beam: the plain support beam plus half a chain on each joined side
        chainedSupportBeam(blockModels, (CBChainedSupportBeam) CBBlockRegistry.CHAINED_SUPPORT_BEAM.get(),
                model("bolted_support_beam"), model("chain_beam_half"), metalAndLog);

    }

    // Same blockstate as the accented cap, asking the block for its rotations (this one has an extra half turn)
    private static void cornerSupportCap(BlockModelGenerators blockModels, CBAccentedPillarCap cap, TextureMapping textures)
    {
        // A suffix keeps the generated model from having the same name as the hand-made one it's built from
        Identifier model = CBModelTemplates.BOLTED_CORNER_SUPPORT_CAP.create(
                ModelLocationUtils.getModelLocation(cap, "_textured"), textures, blockModels.modelOutput);
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(cap)
                .with(PropertyDispatch.initial(CBAccentedPillarCap.FACING, CBAccentedPillarCap.HALF)
                        .generate((facing, half) -> {
                            BlockState state = cap.defaultBlockState()
                                    .setValue(CBAccentedPillarCap.FACING, facing)
                                    .setValue(CBAccentedPillarCap.HALF, half);
                            return rotated(model, cap.xQuarterTurns(state), cap.yQuarterTurns(state));
                        })));
        tintedItem(blockModels, cap, model, cap.getTintRegionCount());
    }

    // A beam that always uses the same hand-made model
    public static void beam(BlockModelGenerators blockModels, CBBoltedBeamBlock beam, Identifier parent, TextureMapping textures)
    {
        beam(blockModels, beam, state -> new ModelChoice("", parent, 0), textures);
    }

    // One multipart entry per block state, each using a copy of its hand-made model moved and turned into place
    // by a NeoForge root transform, then rotated for the facing by the blockstate.
    public static void beam(BlockModelGenerators blockModels, CBBoltedBeamBlock beam, Function<BlockState, ModelChoice> modelFor,
                            TextureMapping textures)
    {
        // Model copies, created the first time a state needs them so each file is only written once
        Map<String, Identifier> copies = new HashMap<>();

        MultiPartGenerator blockState = MultiPartGenerator.multiPart(beam);
        for (BlockState state : beam.getStateDefinition().getPossibleStates())
        {
            var condition = BlockModelGenerators.condition()
                    .term(beam.offsetXProperty(), beam.offsetX(state))
                    .term(beam.offsetZProperty(), beam.offsetZ(state));
            if (beam.isCap()) condition = condition.term(CBBoltedBeamBlock.FACING, beam.facing(state));
            else condition = condition.term(CBBoltedBeamBlock.AXIS, beam.facing(state).getAxis());
            if (beam.turnProperty() != null) condition = condition.term(beam.turnProperty(), beam.turn(state));

            Identifier model = modelCopy(blockModels, beam, state, modelFor.apply(state), textures, copies);
            blockState.with(condition, rotated(model, beam.xQuarterTurns(state), beam.yQuarterTurns(state)));
        }
        blockModels.blockStateOutput.accept(blockState);

        // The item shows the beam in its default (centred, upright, unturned) state
        BlockState item = beam.defaultBlockState();
        Identifier itemModel = modelCopy(blockModels, beam, item, modelFor.apply(item), textures, copies);
        tintedItem(blockModels, beam, itemModel, beam.getTintRegionCount());
    }

    // The beam part is the plain support beam, added once per position, turn and axis (the chain flags don't
    // move it). Each side then gets a half-chain part, drawn whenever that side's chain flag is on.
    private static void chainedSupportBeam(BlockModelGenerators blockModels, CBChainedSupportBeam beam,
                                           Identifier beamModel, Identifier chainModel, TextureMapping textures)
    {
        Map<String, Identifier> copies = new HashMap<>();
        Set<String> added = new HashSet<>();
        MultiPartGenerator blockState = MultiPartGenerator.multiPart(beam);

        for (BlockState state : beam.getStateDefinition().getPossibleStates())
        {
            int x = beam.offsetX(state);
            int z = beam.offsetZ(state);
            int turn = beam.turn(state);
            Direction.Axis axis = beam.facing(state).getAxis();
            if (!added.add(x + "_" + z + "_" + turn + "_" + axis)) continue;

            int xTurns = beam.xQuarterTurns(state);
            int yTurns = beam.yQuarterTurns(state);

            // The beam
            Identifier beamCopy = modelCopy(blockModels, beam, state, new ModelChoice("", beamModel, 0), textures, copies);
            blockState.with(
                    BlockModelGenerators.condition()
                            .term(beam.offsetXProperty(), x)
                            .term(beam.offsetZProperty(), z)
                            .term(CBBoltedBeamBlock.AXIS, axis)
                            .term(beam.turnProperty(), turn),
                    rotated(beamCopy, xTurns, yTurns));

            // A half-chain per side, only when a chain joins that side
            for (Direction side : CBChainedSupportBeam.CHAIN_SIDES)
            {
                Vec3 translation = beam.chainModelTranslation(state, side);
                String key = "_chain_" + side.getSerializedName()
                        + "_x" + pixels(translation.x) + "_y" + pixels(translation.y) + "_z" + pixels(translation.z);
                Identifier chainCopy = copies.computeIfAbsent(key, name -> chainTemplate(chainModel, translation, CBChainedSupportBeam.chainTurns(side))
                        .create(ModelLocationUtils.getModelLocation(beam, name), textures, blockModels.modelOutput));

                blockState.with(
                        BlockModelGenerators.condition()
                                .term(beam.offsetXProperty(), x)
                                .term(beam.offsetZProperty(), z)
                                .term(CBBoltedBeamBlock.AXIS, axis)
                                .term(beam.turnProperty(), turn)
                                .term(CBChainedSupportBeam.chainProperty(side), true),
                        rotated(chainCopy, xTurns, yTurns));
            }
        }
        blockModels.blockStateOutput.accept(blockState);

        // The item is the plain beam, centred and upright
        BlockState item = beam.defaultBlockState();
        Identifier itemModel = modelCopy(blockModels, beam, item, new ModelChoice("", beamModel, 0), textures, copies);
        tintedItem(blockModels, beam, itemModel, beam.getTintRegionCount());
    }

    // The south half-chain, turned round to its side and moved across with the beam (same maths as chainBox)
    private static ModelTemplate chainTemplate(Identifier parent, Vec3 translation, int quarterTurns)
    {
        return ExtendedModelTemplateBuilder.builder()
                .parent(parent)
                .requiredTextureSlot(TextureSlot.PARTICLE)
                .requiredTextureSlot(CBModelTemplates.TEXTURE_ONE)
                .requiredTextureSlot(CBModelTemplates.TEXTURE_TWO)
                .rootTransforms(transform -> transform
                        .translation((float) translation.x / 16, (float) translation.y / 16, (float) translation.z / 16)
                        // -90 = clockwise seen from above, the same direction as CBPixelBox.rotateY(1)
                        .rotation(0, -90f * quarterTurns, 0, true)
                        .origin(TransformationHelper.TransformOrigin.CENTER))
                .build();
    }

    // A pixel amount for a model file name: 2 -> "2", -2 -> "m2", 0.5 -> "0p5"
    private static String pixels(double value)
    {
        String text = value == Math.rint(value) ? String.valueOf((int) value) : String.valueOf(value);
        return text.replace("-", "m").replace(".", "p");
    }

    // The copy of the chosen model for this state's position and turn (made if it doesn't exist yet)
    private static Identifier modelCopy(BlockModelGenerators blockModels, CBBoltedBeamBlock beam, BlockState state,
                                        ModelChoice choice, TextureMapping textures, Map<String, Identifier> copies)
    {
        int x = beam.offsetX(state);
        int z = beam.offsetZ(state);
        int turn = beam.turn(state);
        String suffix = choice.name() + "_x" + x + "_z" + z + "_t" + turn + (choice.extraTurns() != 0 ? "_r" + choice.extraTurns() : "");

        return copies.computeIfAbsent(suffix, key -> {
            // The extra turns only spin the model; the position still follows the beam's own turn
            Vec3 translation = beam.modelTranslation(x, z, turn);
            Quaternionf rotation = beam.rootRotation(turn + choice.extraTurns());
            ModelTemplate template = ExtendedModelTemplateBuilder.builder()
                    .parent(choice.parent())
                    .requiredTextureSlot(TextureSlot.PARTICLE)
                    .requiredTextureSlot(CBModelTemplates.TEXTURE_ONE)
                    .requiredTextureSlot(CBModelTemplates.TEXTURE_TWO)
                    .rootTransforms(transform -> transform
                            // Root transforms are in block units: 1 pixel = 1/16
                            .translation((float) translation.x / 16, (float) translation.y / 16, (float) translation.z / 16)
                            .rotation(rotation)
                            .origin(TransformationHelper.TransformOrigin.CENTER))
                    .build();
            return template.create(ModelLocationUtils.getModelLocation(beam, key), textures, blockModels.modelOutput);
        });
    }

    // Item with every tint index starting undyed
    private static void tintedItem(BlockModelGenerators blockModels, Block block, Identifier model, int tintCount)
    {
        ItemTintSource[] tints = new ItemTintSource[tintCount];
        Arrays.fill(tints, new Dye(0xFFFFFFFF));
        blockModels.itemModelOutput.accept(block.asItem(), ItemModelUtils.tintedModel(model, tints));
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

    // texture_one = metal, texture_two = the beam's main material
    public static TextureMapping textures(String one, String two)
    {
        Material first = new Material(Identifier.fromNamespaceAndPath(CBReferences.MODID, "block/" + one));
        Material second = new Material(Identifier.fromNamespaceAndPath(CBReferences.MODID, "block/" + two));
        return TextureMapping.cube(first)
                .put(CBModelTemplates.TEXTURE_ONE, first)
                .put(CBModelTemplates.TEXTURE_TWO, second);
    }

    private static Identifier model(String name)
    {
        return Identifier.fromNamespaceAndPath(CBReferences.MODID, "block/" + name);
    }

    // A plain centred 8x8 column using #texture_one with tint 0. Generated once and shared by every set's column copies.
    public static Identifier columnTemplate(BlockModelGenerators blockModels)
    {
        CBPixelBox box = CBBoltedSupportBeam.FOOTPRINT;
        ModelTemplate template = ExtendedModelTemplateBuilder.builder()
                .parent(Identifier.withDefaultNamespace("block/block"))
                .element(element -> element
                        .from((float) box.minX(), (float) box.minY(), (float) box.minZ())
                        .to((float) box.maxX(), (float) box.maxY(), (float) box.maxZ())
                        .allFaces((direction, face) -> face
                                .texture(CBModelTemplates.TEXTURE_ONE)
                                .tintindex(0)
                                // The ends always reach the block's edge; the sides only do at some positions
                                .cullface(direction.getAxis().isVertical() ? direction : null)))
                .build();
        Identifier id = model("column_template");
        template.create(id, TextureMapping.cube(new Material(id)), blockModels.modelOutput);
        return id;
    }
}