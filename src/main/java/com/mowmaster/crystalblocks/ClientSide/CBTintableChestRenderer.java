package com.mowmaster.crystalblocks.ClientSide;

import com.mowmaster.mowlib.util.CBPixelBox;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.mowmaster.crystalblocks.Blocks.Colorable.CBTintableChestBlockEntity;
import com.mowmaster.crystalblocks.CBReferences;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.model.standalone.StandaloneModelKey;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

// Draws the tintable chest's lid, hinged at the back, swinging open with the vanilla chest's easing.
// The lid models are loaded as standalone models (registered in CrystalBlocksClient); the bottom is a normal block model.
public class CBTintableChestRenderer implements BlockEntityRenderer<CBTintableChestBlockEntity, CBTintableChestRenderer.State>
{
    // Hinge line, in blocks: the top of the bottom part (y 12) at the back edge (z 16), as modelled facing north
    private static final float HINGE_Y = 12 / 16f;
    private static final float HINGE_Z = 1f;

    private static final Map<ChestType, StandaloneModelKey<BlockStateModelPart>> LID_KEYS = new EnumMap<>(ChestType.class);

    static
    {
        for (ChestType type : ChestType.values())
        {
            LID_KEYS.put(type, new StandaloneModelKey<>(() -> lidModel(type).toString()));
        }
    }

    public CBTintableChestRenderer(BlockEntityRendererProvider.Context context)
    {
    }

    // The datagen'd (textured) lid model for each chest type
    public static Identifier lidModel(ChestType type)
    {
        String part = switch (type)
        {
            case SINGLE -> "";
            case RIGHT -> "_double_right";
            case LEFT -> "_double_left";
        };
        return Identifier.fromNamespaceAndPath(CBReferences.MODID, "block/tintable_chest" + part + "_lid_textured");
    }

    public static StandaloneModelKey<BlockStateModelPart> lidKey(ChestType type)
    {
        return LID_KEYS.get(type);
    }

    @Override
    public State createRenderState()
    {
        return new State();
    }

    // Runs every frame: copy what submit needs out of the block entity
    @Override
    public void extractRenderState(CBTintableChestBlockEntity chest, State state, float partialTick, Vec3 cameraPos,
                                   ModelFeatureRenderer.@Nullable CrumblingOverlay crumblingOverlay)
    {
        BlockEntityRenderer.super.extractRenderState(chest, state, partialTick, cameraPos, crumblingOverlay);
        BlockState blockState = chest.getBlockState();
        state.facing = blockState.getValue(ChestBlock.FACING);
        state.type = blockState.getValue(ChestBlock.TYPE);
        state.open = chest.getOpenNess(partialTick);
        for (int tintIndex = 0; tintIndex < state.tints.length; tintIndex++)
        {
            state.tints[tintIndex] = 0xFF000000 | chest.getTint(tintIndex);
        }
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera)
    {
        BlockStateModelPart lid = Minecraft.getInstance().getModelManager().getStandaloneModel(lidKey(state.type));
        if (lid == null) return;

        // Same easing as vanilla chests: quick at first, slowing as it opens
        float open = 1f - state.open;
        open = 1f - open * open * open;

        poseStack.pushPose();
        // Turn for the facing (the model faces north), around the block's centre
        poseStack.translate(0.5, 0, 0.5);
        poseStack.rotate(Axis.YP.rotationDegrees(-90f * CBPixelBox.quarterTurns(state.facing)));
        poseStack.translate(-0.5, 0, -0.5);
        // Swing the front up around the hinge
        poseStack.translate(0, HINGE_Y, HINGE_Z);
        poseStack.rotate(Axis.XP.rotation(open * Mth.HALF_PI));
        poseStack.translate(0, -HINGE_Y, -HINGE_Z);

        submitNodeCollector.submitMultiLayerBlockModel(poseStack, List.of(lid), false, state.tints,
                state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
        poseStack.popPose();
    }

    // The open lid reaches past the block, so it must still be drawn when only its edge is on screen
    @Override
    public AABB getRenderBoundingBox(CBTintableChestBlockEntity chest)
    {
        BlockPos pos = chest.getBlockPos();
        return AABB.encapsulatingFullBlocks(pos.offset(-1, 0, -1), pos.offset(1, 1, 1));
    }

    public static class State extends BlockEntityRenderState
    {
        Direction facing = Direction.NORTH;
        ChestType type = ChestType.SINGLE;
        float open;
        // One ARGB colour per tintindex: inner, bands, knob
        final int[] tints = new int[3];
    }
}
