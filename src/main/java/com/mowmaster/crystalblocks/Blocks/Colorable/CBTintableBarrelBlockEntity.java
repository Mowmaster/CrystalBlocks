package com.mowmaster.crystalblocks.Blocks.Colorable;

import com.mowmaster.mowlib.tint.CBTintData;
import com.mowmaster.mowlib.tint.ITintHolder;

import com.mowmaster.crystalblocks.Blocks.CBBlockEntityRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.ContainerUser;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.NotNull;

// The barrel's inventory (27 slots, same menu as vanilla) plus its three colours
public class CBTintableBarrelBlockEntity extends RandomizableContainerBlockEntity implements ITintHolder
{
    private NonNullList<ItemStack> items = NonNullList.withSize(27, ItemStack.EMPTY);
    private final CBTintData tints = new CBTintData(this, 3);
    // How many players have it open right now (not saved)
    private int openCount;

    public CBTintableBarrelBlockEntity(BlockPos pos, BlockState state)
    {
        super(CBBlockEntityRegistry.TINTABLE_BARREL.get(), pos, state);
    }

    // ---- Inventory ----

    @Override
    public int getContainerSize()
    {
        return 27;
    }

    @Override
    protected @NotNull NonNullList<ItemStack> getItems()
    {
        return items;
    }

    @Override
    protected void setItems(NonNullList<ItemStack> items)
    {
        this.items = items;
    }

    @Override
    protected @NotNull Component getDefaultName()
    {
        return Component.translatable("container.barrel");
    }

    @Override
    protected @NotNull AbstractContainerMenu createMenu(int containerId, Inventory inventory)
    {
        return ChestMenu.threeRows(containerId, inventory, this);
    }

    // ---- Opening (the menu calls these when it opens and closes) ----

    @Override
    public void startOpen(ContainerUser user)
    {
        if (remove || (user instanceof Player player && player.isSpectator())) return;
        if (openCount++ == 0) setOpen(true);
    }

    @Override
    public void stopOpen(ContainerUser user)
    {
        if (remove || (user instanceof Player player && player.isSpectator())) return;
        if (openCount > 0 && --openCount == 0) setOpen(false);
    }

    private void setOpen(boolean open)
    {
        if (level == null) return;
        BlockState state = getBlockState();
        // Already showing open (held by redstone): no sound
        if (!state.getValue(CBTintableBarrel.POWERED))
        {
            CBTintableBarrel.playSound(level, worldPosition, state, open ? SoundEvents.BARREL_OPEN : SoundEvents.BARREL_CLOSE);
        }
        level.setBlock(worldPosition, state.setValue(CBTintableBarrel.OPEN, open), Block.UPDATE_ALL);
    }

    // ---- Colours ----

    @Override public int getColor(int region) { return tints.getColor(region); }
    @Override public void setColor(int region, int color) { tints.setColor(region, color); }
    @Override public int getTint(int tintIndex) { return tints.getTint(tintIndex); }

    // ---- Saving ----

    @Override
    protected void saveAdditional(ValueOutput output)
    {
        super.saveAdditional(output);
        if (!trySaveLootTable(output)) ContainerHelper.saveAllItems(output, items);
        tints.save(output);
    }

    @Override
    protected void loadAdditional(ValueInput input)
    {
        super.loadAdditional(input);
        items = NonNullList.withSize(getContainerSize(), ItemStack.EMPTY);
        if (!tryLoadLootTable(input)) ContainerHelper.loadAllItems(input, items);
        tints.load(input);
    }

    // ---- Syncing the colours (only) to clients ----

    @Override
    public @NotNull CompoundTag getUpdateTag(HolderLookup.Provider registries)
    {
        return tints.updateTag();
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket()
    {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
