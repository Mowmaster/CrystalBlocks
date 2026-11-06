package com.mowmaster.crystalblocks.Blocks;

import com.mowmaster.crystalblocks.Blocks.Colorable.CBTintableRail;
import com.mowmaster.crystalblocks.CBReferences;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

// Speeds up and brakes minecarts on Crystal Rails. Vanilla only boosts carts on powered rails (and NeoForge 26.3 has no
// per-rail hook for it), so this runs after each minecart's tick on the server and changes its speed for the next one,
// with the same maths vanilla uses for powered rails.
@EventBusSubscriber(modid = CBReferences.MODID)
public class CBRailMinecarts
{
    @SubscribeEvent
    public static void afterMinecartTick(EntityTickEvent.Post event)
    {
        if (!(event.getEntity() instanceof AbstractMinecart cart)) return;
        Level level = cart.level();
        if (level.isClientSide()) return;

        // The rail the cart is on: its own block, or the one below on the way down a slope
        BlockPos pos = cart.blockPosition();
        if (!(level.getBlockState(pos).getBlock() instanceof CBTintableRail))
        {
            pos = pos.below();
            if (!(level.getBlockState(pos).getBlock() instanceof CBTintableRail)) return;
        }

        Vec3 movement = cart.getDeltaMovement();
        if (level.hasNeighborSignal(pos))
        {
            // Powered: brake like an unpowered powered rail (half speed each tick, stopped once slow)
            cart.setDeltaMovement(movement.length() < 0.03 ? Vec3.ZERO : movement.scale(0.5));
        }
        else if (movement.length() > 0.01)
        {
            // Not powered: a little faster in the direction it's already going (stopped carts stay put)
            cart.setDeltaMovement(movement.normalize().scale(movement.length() + CBTintableRail.BOOST));
        }
    }
}
