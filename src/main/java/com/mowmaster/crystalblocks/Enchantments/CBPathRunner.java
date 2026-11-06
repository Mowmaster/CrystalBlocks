package com.mowmaster.crystalblocks.Enchantments;

import com.mowmaster.crystalblocks.CBDataGeneration.CBTags;
import com.mowmaster.crystalblocks.CBReferences;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.Map;
import java.util.WeakHashMap;

// What Path Runner does: Soul Speed's speed boost on any block in #crystalblocks:path_blocks, with half Soul Speed's
// wear on the boots. Like Soul Speed it keeps working mid-jump, and stops when riding, flying or off the path.
// Runs on the server; the movement speed attribute syncs to the player's client by itself.
@EventBusSubscriber(modid = CBReferences.MODID)
public class CBPathRunner
{
    private static final Identifier MODIFIER = CBReferences.id("path_runner");
    // Soul Speed's numbers: +0.0405 movement speed at level 1, +0.0105 per level after
    private static final double BASE_SPEED = 0.0405;
    private static final double SPEED_PER_LEVEL = 0.0105;
    // Chance the boots lose 1 durability each time the player moves onto another block: half Soul Speed's 4%
    private static final float WEAR_CHANCE = 0.02f;

    // Where each player last was, so wear only happens when they move to another block
    private static final Map<Player, BlockPos> LAST_POS = new WeakHashMap<>();

    @SubscribeEvent
    public static void afterPlayerTick(PlayerTickEvent.Post event)
    {
        Player player = event.getEntity();
        Level level = player.level();
        if (level.isClientSide()) return;

        AttributeInstance speed = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed == null) return;

        ItemStack boots = player.getItemBySlot(EquipmentSlot.FEET);
        int enchantLevel = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).get(CBEnchantments.PATH_RUNNER)
                .map(holder -> EnchantmentHelper.getItemEnchantmentLevel(holder, boots))
                .orElse(0);

        boolean onPath = player.onGround()
                && level.getBlockState(player.getBlockPosBelowThatAffectsMyMovement()).is(CBTags.Blocks.PATH_BLOCKS);
        boolean active = enchantLevel > 0 && !player.isPassenger() && !player.getAbilities().flying
                // On a path, or still in the air after leaving one (jumping keeps the boost, like Soul Speed)
                && (onPath || (!player.onGround() && speed.getModifier(MODIFIER) != null));

        if (!active)
        {
            speed.removeModifier(MODIFIER);
            return;
        }

        double amount = BASE_SPEED + SPEED_PER_LEVEL * (enchantLevel - 1);
        AttributeModifier current = speed.getModifier(MODIFIER);
        if (current == null || current.amount() != amount)
        {
            speed.removeModifier(MODIFIER);
            speed.addTransientModifier(new AttributeModifier(MODIFIER, amount, AttributeModifier.Operation.ADD_VALUE));
        }

        // Wear: only while actually on a path and moving onto another block
        BlockPos pos = player.blockPosition();
        BlockPos last = LAST_POS.put(player, pos);
        if (onPath && last != null && !last.equals(pos) && player.getRandom().nextFloat() < WEAR_CHANCE)
        {
            boots.hurtAndBreak(1, player, EquipmentSlot.FEET);
        }
    }
}
