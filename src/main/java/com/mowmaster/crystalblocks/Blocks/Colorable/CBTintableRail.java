package com.mowmaster.crystalblocks.Blocks.Colorable;

import com.mowmaster.mowlib.tint.CBTintBlockEntity;
import com.mowmaster.mowlib.tint.IColorableBlock;
import com.mowmaster.mowlib.util.CBPixelBox;
import com.mojang.datafixers.util.Pair;
import com.mowmaster.mowlib.tint.ITintHolder;
import net.minecraft.core.Vec3i;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import org.jetbrains.annotations.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RailBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

// A tintable rail: placed and connected like a normal rail (straights, slopes and corners), and gives carts a small
// boost (a quarter of a powered rail's) without needing power. A redstone signal on it brakes carts like an unpowered
// powered rail. The boost and braking happen in CBRailMinecarts.
// Tint regions: 0 = the rails (metal), 1 = the ties (wood), 2 = the nails (metal, grey until dyed).
public class CBTintableRail extends RailBlock implements IColorableBlock
{
    public static final int RAIL_REGION = 0;
    public static final int TIE_REGION = 1;
    public static final int NAIL_REGION = 2;

    // Extra speed per tick while on the rail: a quarter of a powered rail's 0.06
    public static final double BOOST = 0.06 / 4;

    public CBTintableRail(Properties properties)
    {
        super(properties);
    }

    // ---- Tinting ----

    @Override
    public int getTintRegionCount()
    {
        return 3;
    }

    @Override
    public int getDefaultColor(int region)
    {
        return region == NAIL_REGION ? DyeColor.GRAY.getTextureDiffuseColor() & 0xFFFFFF : CBTintBlockEntity.DEFAULT_TINT;
    }

    // The corner model's parts seen from above, as modelled (joining the north and west edges): x0, z0, x1, z1
    private static final double[][] CORNER_RAILS = {
            { 2.25, 0, 3.75, 3.75 }, { 0, 2.25, 2.25, 3.75 }, { 0, 12.25, 12.25, 13.75 }, { 12.25, 0, 13.75, 13.75 } };
    private static final double[][] CORNER_NAILS = {
            { 2, 1.25, 2.5, 1.5 }, { 3.5, 1.5, 4, 1.75 }, { 12, 1.5, 12.5, 1.75 }, { 13.5, 1.75, 14, 2 },
            { 12, 6.25, 12.5, 6.5 }, { 13.5, 6.5, 14, 6.75 }, { 12, 11.5, 12.5, 11.75 }, { 13.5, 12.75, 14, 13 },
            { 2, 2, 2.25, 2.5 }, { 1.75, 3.5, 2, 4 }, { 2, 12, 2.25, 12.5 }, { 1.75, 13.5, 2, 14 },
            { 6.5, 12, 6.75, 12.5 }, { 6.25, 13.5, 6.5, 14 }, { 12.75, 13.5, 13, 14 }, { 11.5, 12, 11.75, 12.5 } };

    // Works out which part was clicked from where it is on the track: the clicked point is turned back into the
    // model's own layout (undoing the blockstate's turn), then checked against that model's parts.
    // The outline is vanilla's (a flat box), so only the horizontal position matters.
    @Override
    public int getTintRegion(BlockState state, BlockHitResult hit)
    {
        Vec3 pixel = CBPixelBox.pixelInside(hit);
        double x = pixel.x;
        double z = pixel.z;
        RailShape shape = state.getValue(SHAPE);

        // The blockstate's y quarter turns for each shape (keep in step with CBRailModels)
        int turns = switch (shape)
        {
            case EAST_WEST, ASCENDING_EAST, ASCENDING_WEST, NORTH_EAST -> 1;
            case SOUTH_EAST -> 2;
            case SOUTH_WEST -> 3;
            default -> 0;
        };
        // One turn back: (x, z) -> (z, 16 - x), the opposite of CBPixelBox.rotateY
        for (int i = 0; i < turns; i++)
        {
            double oldX = x;
            x = z;
            z = 16 - oldX;
        }

        boolean corner = shape == RailShape.NORTH_WEST || shape == RailShape.NORTH_EAST
                || shape == RailShape.SOUTH_EAST || shape == RailShape.SOUTH_WEST;
        return corner ? cornerRegionAt(x, z) : straightRegionAt(x, z);
    }

    // The straight model (also the slopes, whose parts line up the same seen from above): rails at x 2.25-3.75 and
    // 12.25-13.75, a nail either side of each rail on each tie (ties centred at z 2, 6, 10, 14), ties everywhere else.
    // Nails are tiny, so they get a little leeway.
    private static int straightRegionAt(double x, double z)
    {
        boolean onTie = false;
        for (double tie : new double[] { 2, 6, 10, 14 })
        {
            if (Math.abs(z - tie) <= 0.5) onTie = true;
        }
        if (onTie)
        {
            for (double nail : new double[] { 2.25, 3.75, 12.25, 13.75 })
            {
                if (Math.abs(x - nail) <= 0.5) return NAIL_REGION;
            }
        }
        if ((x >= 2.25 && x <= 3.75) || (x >= 12.25 && x <= 13.75)) return RAIL_REGION;
        return TIE_REGION;
    }

    private static int cornerRegionAt(double x, double z)
    {
        if (inAny(CORNER_NAILS, x, z, 0.25)) return NAIL_REGION;
        if (inAny(CORNER_RAILS, x, z, 0)) return RAIL_REGION;
        return TIE_REGION;
    }

    private static boolean inAny(double[][] boxes, double x, double z, double leeway)
    {
        for (double[] box : boxes)
        {
            if (x >= box[0] - leeway && x <= box[2] + leeway && z >= box[1] - leeway && z <= box[3] + leeway) return true;
        }
        return false;
    }

    // Dyes colour the clicked part; anything else does what it normally would on a rail
    @Override
    protected @NotNull InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                                   Player player, InteractionHand hand, BlockHitResult hitResult)
    {
        return tryDyeBlock(stack, state, level, pos, player, hitResult);
    }

    // ---- Placing: take on the colours of a Crystal Rail this one connects to ----

    // By now vanilla has turned the rail to connect to its neighbours, so its two exits are the rails it joins.
    // The first connected Crystal Rail that has any colour is copied; a rail placed from a dyed item keeps its own.
    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack)
    {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (level.isClientSide() || !(level.getBlockEntity(pos) instanceof ITintHolder placed)) return;
        for (int region = 0; region < getTintRegionCount(); region++)
        {
            if (placed.isDyed(region)) return;
        }

        // Re-read the state: connecting may have changed the shape after the item placed it
        BlockState placedState = level.getBlockState(pos);
        if (!placedState.is(this)) return;
        Pair<Vec3i, Vec3i> exits = AbstractMinecart.exits(placedState.getValue(SHAPE));
        for (Vec3i exit : new Vec3i[] { exits.getFirst(), exits.getSecond() })
        {
            // A rail one block up or down counts too (slopes)
            for (int dy = -1; dy <= 1; dy++)
            {
                BlockPos neighborPos = pos.offset(exit.getX(), dy, exit.getZ());
                if (level.getBlockState(neighborPos).getBlock() instanceof CBTintableRail
                        && level.getBlockEntity(neighborPos) instanceof ITintHolder source
                        && copyColors(source, placed))
                {
                    return;
                }
            }
        }
    }

    // Copies every dyed region; false if the source has no colour at all (so the next neighbour is tried)
    private boolean copyColors(ITintHolder source, ITintHolder target)
    {
        boolean copied = false;
        for (int region = 0; region < getTintRegionCount(); region++)
        {
            if (source.isDyed(region))
            {
                target.setColor(region, source.getColor(region));
                copied = true;
            }
        }
        return copied;
    }
}
