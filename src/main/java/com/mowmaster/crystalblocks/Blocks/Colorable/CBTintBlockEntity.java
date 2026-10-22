package com.mowmaster.crystalblocks.Blocks.Colorable;

import com.mowmaster.crystalblocks.Blocks.CBBlockEntityRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class CBTintBlockEntity extends BlockEntity
{
    // Colours are stored as 0xRRGGBB with no alpha, so -1 can never be a real colour
    public static final int UNDYED = -1;
    // Colour shown for an undyed block. White leaves the greyscale texture as-is.
    public static final int DEFAULT_TINT = 0xFFFFFF;
    private static final String TAG_COLOR = "color";

    private int color = UNDYED;

    public CBTintBlockEntity(BlockPos pos, BlockState state)
    {
        super(CBBlockEntityRegistry.TINT_BLOCK_ENTITY.get(), pos, state);
    }

    public int getColor() { return color; }

    public boolean isDyed() { return color != UNDYED; }

    // What the renderer asks for. Single-colour blocks ignore tintIndex; a multi-region
    // block entity can extend this class and override it to return a colour per region.
    public int getTint(int tintIndex)
    {
        return isDyed() ? color : DEFAULT_TINT;
    }

    public void setColor(int color)
    {
        this.color = color;
        setChanged(); // marks the chunk as needing to be saved
        if (level != null)
        {
            // Sends getUpdatePacket() to nearby clients
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    // ---- Saving to disk ----

    @Override
    protected void saveAdditional(ValueOutput output)
    {
        super.saveAdditional(output);
        if (isDyed()) output.putInt(TAG_COLOR, color);
    }

    @Override
    protected void loadAdditional(ValueInput input)
    {
        super.loadAdditional(input);
        color = input.getIntOr(TAG_COLOR, UNDYED);

        // On the client, the block state didn't change, so force the chunk to re-mesh with the new colour
        if (level != null && level.isClientSide())
        {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
        }
    }

    // ---- Syncing to the client ----

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries)
    {
        return saveCustomOnly(registries); // runs saveAdditional into a tag
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket()
    {
        return ClientboundBlockEntityDataPacket.create(this); // uses getUpdateTag
    }

    // ---- Item <-> block (data components) ----

    // Item placed: copy the item's dyed_color into the block
    @Override
    protected void applyImplicitComponents(DataComponentGetter components)
    {
        super.applyImplicitComponents(components);
        DyedItemColor dyed = components.get(DataComponents.DYED_COLOR);
        color = dyed != null ? dyed.rgb() : UNDYED;
    }

    // Block turned into an item (creative pick-block, or a loot table copying components): copy the colour back out
    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder components)
    {
        super.collectImplicitComponents(components);
        if (isDyed()) components.set(DataComponents.DYED_COLOR, new DyedItemColor(color));
    }
}
