package com.mowmaster.crystalblocks.Blocks.Colorable;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Base for blocks built from a custom model whose parts are tinted separately.
// Each region is a list of the model's element boxes (as modelled, facing north) whose faces use that tintindex.
// Subclasses give their block state properties, their placement rule, and how the model is rotated in each state.
public abstract class CBAccentedBlock extends Block implements IColorableBlock
{
    public static final int BODY_REGION = 0;
    public static final int ACCENT_REGION = 1;

    // regionBoxes.get(n) = the element boxes tinted by tintindex n
    private final List<List<CBPixelBox>> regionBoxes;
    private final Map<BlockState, VoxelShape> shapes = new HashMap<>();

    // Two regions: a body (tintindex 0) and an accent (tintindex 1)
    protected CBAccentedBlock(Properties properties, List<CBPixelBox> bodyBoxes, List<CBPixelBox> accentBoxes)
    {
        this(properties, List.of(bodyBoxes, accentBoxes));
    }

    // Any number of regions, in tintindex order
    protected CBAccentedBlock(Properties properties, List<List<CBPixelBox>> regionBoxes)
    {
        super(properties);
        this.regionBoxes = regionBoxes;

        // Build the outline/collision shape for every state once, up front
        List<CBPixelBox> allBoxes = regionBoxes.stream().flatMap(List::stream).toList();
        for (BlockState state : stateDefinition.getPossibleStates())
        {
            shapes.put(state, CBPixelBox.union(placeBoxes(allBoxes, state)));
        }
    }

    // The model's rotation in this state, in quarter turns, exactly as the blockstate file rotates it:
    // x is applied first, then y. Datagen uses the same values, so shapes and visuals always match.
    protected abstract int xQuarterTurns(BlockState state);

    protected abstract int yQuarterTurns(BlockState state);

    // Moves north-facing model boxes to where they actually are for this state
    protected List<CBPixelBox> placeBoxes(List<CBPixelBox> boxes, BlockState state)
    {
        int xTurns = xQuarterTurns(state);
        int yTurns = yQuarterTurns(state);
        return boxes.stream().map(box -> box.rotateX(xTurns).rotateY(yTurns)).toList();
    }

    // ---- Tint regions ----

    @Override
    public int getTintRegionCount()
    {
        return regionBoxes.size();
    }

    // Dye whichever part was clicked. Higher regions are checked first, so smaller detail parts
    // win over the parts they sit on.
    @Override
    public int getTintRegion(BlockState state, BlockHitResult hit)
    {
        Vec3 pixel = CBPixelBox.pixelInside(hit);
        for (int region = regionBoxes.size() - 1; region >= 0; region--)
        {
            for (CBPixelBox box : placeBoxes(regionBoxes.get(region), state))
            {
                if (box.contains(pixel)) return region;
            }
        }
        return BODY_REGION;
    }

    // ---- Shape ----

    @Override
    protected @NotNull VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context)
    {
        return shapes.get(state);
    }

    @Override
    protected boolean useShapeForLightOcclusion(BlockState state)
    {
        return true;
    }

    // ---- Dyeing ----

    @Override
    protected @NotNull InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                                   Player player, InteractionHand hand, BlockHitResult hitResult)
    {
        return tryDyeBlock(stack, state, level, pos, player, hitResult);
    }
}