package com.mowmaster.crystalblocks.Blocks.Colorable;

import com.mowmaster.mowlib.tint.CBTintData;
import com.mowmaster.mowlib.tint.ITintHolder;

import com.mowmaster.crystalblocks.Blocks.CBBlockEntityRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

// A vanilla chest block entity (inventory, lid animation, double chests, sounds) plus three colours and a lock.
public class CBTintableChestBlockEntity extends ChestBlockEntity implements ITintHolder
{
    // NONE: anyone. GOLD: only the owner opens it; hoppers work; anyone can break it; explosion-proof.
    // DIAMOND: only the owner opens or breaks it (survival); no hoppers/pipes; explosion-proof.
    public enum Lock { NONE, GOLD, DIAMOND }

    // Not "lock": vanilla containers already save their own lock under that name
    private static final String TAG_LOCK = "cb_lock";
    private static final String TAG_OWNER = "cb_owner";

    private final CBTintData tints = new CBTintData(this, 3);
    private Lock lock = Lock.NONE;
    @Nullable
    private UUID owner;

    public CBTintableChestBlockEntity(BlockPos pos, BlockState state)
    {
        super(CBBlockEntityRegistry.TINTABLE_CHEST.get(), pos, state);
    }

    // ---- Lock ----

    public Lock getLock()
    {
        return lock;
    }

    @Nullable
    public UUID getOwner()
    {
        return owner;
    }

    public void setLock(Lock lock, @Nullable UUID owner)
    {
        this.lock = lock;
        this.owner = lock == Lock.NONE ? null : owner;
        setChanged();
    }

    // Whether this player may open (and, for diamond, break) this half
    public boolean canUse(@Nullable Player player)
    {
        return lock == Lock.NONE || (player != null && player.getUUID().equals(owner));
    }

    // ---- Colours ----

    @Override public int getColor(int region) { return tints.getColor(region); }
    @Override public void setColor(int region, int color) { tints.setColor(region, color); }
    @Override public int getTint(int tintIndex) { return tints.getTint(tintIndex); }

    // ---- Saving (the items are saved by ChestBlockEntity) ----

    @Override
    protected void saveAdditional(ValueOutput output)
    {
        super.saveAdditional(output);
        tints.save(output);
        if (lock != Lock.NONE && owner != null)
        {
            output.putString(TAG_LOCK, lock.name());
            output.store(TAG_OWNER, UUIDUtil.CODEC, owner);
        }
    }

    @Override
    protected void loadAdditional(ValueInput input)
    {
        super.loadAdditional(input);
        tints.load(input);
        owner = input.read(TAG_OWNER, UUIDUtil.CODEC).orElse(null);
        lock = owner == null ? Lock.NONE : Lock.valueOf(input.getStringOr(TAG_LOCK, Lock.NONE.name()));
    }

    // ---- Syncing the colours (only, not the items or the lock) to clients ----

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
