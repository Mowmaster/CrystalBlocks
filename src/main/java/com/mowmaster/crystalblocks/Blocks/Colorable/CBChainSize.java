package com.mowmaster.crystalblocks.Blocks.Colorable;

import net.minecraft.world.level.block.state.properties.IntegerProperty;

import java.util.List;

// The chain sizes. Each chain beam/cap reads its shape and positions from here, and chained support beams
// accept one size. Boxes are as modelled: running north-south, the cap plate on the north end.
public enum CBChainSize
{
    // The original chain; it also matches vanilla chains
    SMALL(CBBoltedBeamBlock.Kind.CHAIN,
            new CBPixelBox(6.75, 6.75, 0, 9.25, 9.25, 16), new CBPixelBox(4, 4, 0, 12, 12, 0.25), 12),
    // Modelled centred 2 pixels high (y 10)
    MEDIUM(CBBoltedBeamBlock.Kind.CHAIN_MEDIUM,
            new CBPixelBox(5.5, 7.5, 0, 10.5, 12.5, 16), new CBPixelBox(4, 6, 0, 12, 14, 0.25), 10),
    // Too big for the chained support beam: beams and caps only
    LARGE(CBBoltedBeamBlock.Kind.CHAIN_LARGE,
            new CBPixelBox(3, 3, 0, 13, 13, 16), new CBPixelBox(2, 2, 0, 14, 14, 0.25), 6);

    private final CBBoltedBeamBlock.Kind kind;
    private final CBPixelBox footprint;
    private final List<List<CBPixelBox>> beamRegions;
    private final List<List<CBPixelBox>> capRegions;
    private final IntegerProperty offsetX;
    private final IntegerProperty offsetZ;

    // maxOffset = how many whole-pixel steps fit across the block: 16 - chain width - the fraction its edge sits on
    CBChainSize(CBBoltedBeamBlock.Kind kind, CBPixelBox footprint, CBPixelBox plate, int maxOffset)
    {
        this.kind = kind;
        this.footprint = footprint;
        this.beamRegions = List.of(List.of(footprint));
        this.capRegions = List.of(List.of(footprint, plate));
        this.offsetX = IntegerProperty.create("offset_x", 0, maxOffset);
        this.offsetZ = IntegerProperty.create("offset_z", 0, maxOffset);
    }

    public CBBoltedBeamBlock.Kind kind() { return kind; }
    public CBPixelBox footprint() { return footprint; }
    public List<List<CBPixelBox>> beamRegions() { return beamRegions; }
    public List<List<CBPixelBox>> capRegions() { return capRegions; }
    public IntegerProperty offsetX() { return offsetX; }
    public IntegerProperty offsetZ() { return offsetZ; }
}