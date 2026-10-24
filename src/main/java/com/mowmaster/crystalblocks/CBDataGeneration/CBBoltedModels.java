package com.mowmaster.crystalblocks.CBDataGeneration;

import com.mowmaster.crystalblocks.Blocks.CBBlockRegistry;
import com.mowmaster.crystalblocks.Blocks.Colorable.CBAccentedPillarCap;
import com.mowmaster.crystalblocks.Blocks.Colorable.CBBoltedBeamBlock;
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
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.model.generators.template.ExtendedModelTemplateBuilder;
import net.neoforged.neoforge.common.util.TransformationHelper;
import com.mowmaster.crystalblocks.Blocks.Colorable.CBBoltedSupportBeam;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

// Models and blockstates for the bolted supports (fixed metal + stone / metal + log textures)
public class CBBoltedModels
{
    public static void generate(BlockModelGenerators blockModels)
    {
        TextureMapping metalAndStone = textures("tintable_metal", "tintable_stone");
        TextureMapping metalAndLog = textures("tintable_metal", "tintable_log_one");

        cornerSupportCap(blockModels, (CBAccentedPillarCap) CBBlockRegistry.BOLTED_CORNER_SUPPORT_CAP.get(), metalAndStone);
        beam(blockModels, (CBBoltedBeamBlock) CBBlockRegistry.BOLTED_I_BEAM.get(), model("bolted_i_beam"), metalAndStone);
        beam(blockModels, (CBBoltedBeamBlock) CBBlockRegistry.BOLTED_I_BEAM_CAP.get(), model("bolted_i_beam_cap"), metalAndStone);
        beam(blockModels, (CBBoltedBeamBlock) CBBlockRegistry.BOLTED_SUPPORT_BEAM.get(), model("bolted_support_beam"), metalAndLog);
        beam(blockModels, (CBBoltedBeamBlock) CBBlockRegistry.BOLTED_SUPPORT_BEAM_CAP.get(), model("bolted_support_beam_cap"), metalAndLog);
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

    // One copy of the hand-made model per position and turn, moved by a NeoForge root transform,
    // then one multipart entry per block state that picks the copy and rotates it for the facing
    public static void beam(BlockModelGenerators blockModels, CBBoltedBeamBlock beam, Identifier parent, TextureMapping textures)
    {
        Map<String, Identifier> models = new HashMap<>();

        for (boolean turned : beam.isTurnable() ? new boolean[] { false, true } : new boolean[] { false })
        {
            for (int x = 0; x <= beam.maxOffsetX(); x++)
            {
                for (int z = 0; z <= beam.maxOffsetZ(); z++)
                {
                    Vec3 translation = beam.modelTranslation(x, z, turned);
                    ModelTemplate template = ExtendedModelTemplateBuilder.builder()
                            .parent(parent)
                            .requiredTextureSlot(TextureSlot.PARTICLE)
                            .requiredTextureSlot(CBModelTemplates.TEXTURE_ONE)
                            .requiredTextureSlot(CBModelTemplates.TEXTURE_TWO)
                            .rootTransforms(transform -> transform
                                    // Root transforms are in block units: 1 pixel = 1/16
                                    .translation((float) translation.x / 16, 0, (float) translation.z / 16)
                                    // -90 = clockwise seen from above, the same direction as CBPixelBox.rotateY(1)
                                    .rotation(0, turned ? -90 : 0, 0, true)
                                    .origin(TransformationHelper.TransformOrigin.CENTER))
                            .build();
                    String suffix = suffix(x, z, turned);
                    models.put(suffix, template.create(ModelLocationUtils.getModelLocation(beam, suffix), textures, blockModels.modelOutput));
                }
            }
        }

        MultiPartGenerator blockState = MultiPartGenerator.multiPart(beam);
        for (BlockState state : beam.getStateDefinition().getPossibleStates())
        {
            var condition = BlockModelGenerators.condition()
                    .term(beam.offsetXProperty(), beam.offsetX(state))
                    .term(beam.offsetZProperty(), beam.offsetZ(state));
            if (beam.isCap()) condition = condition.term(CBBoltedBeamBlock.FACING, beam.facing(state));
            else condition = condition.term(CBBoltedBeamBlock.AXIS, beam.facing(state).getAxis());
            if (beam.isTurnable()) condition = condition.term(CBBoltedBeamBlock.TURNED, beam.isTurned(state));

            Identifier model = models.get(suffix(beam.offsetX(state), beam.offsetZ(state), beam.isTurned(state)));
            blockState.with(condition, rotated(model, beam.xQuarterTurns(state), beam.yQuarterTurns(state)));
        }
        blockModels.blockStateOutput.accept(blockState);

        // The item shows the beam in its default (centred, upright, unturned) state
        BlockState item = beam.defaultBlockState();
        Identifier itemModel = models.get(suffix(beam.offsetX(item), beam.offsetZ(item), false));
        tintedItem(blockModels, beam, itemModel, beam.getTintRegionCount());
    }

    private static String suffix(int x, int z, boolean turned)
    {
        return "_x" + x + "_z" + z + (turned ? "_turned" : "");
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