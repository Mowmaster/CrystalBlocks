package com.mowmaster.crystalblocks.CBDataGeneration;

import com.mowmaster.crystalblocks.CBReferences;
import net.minecraft.client.data.models.model.ModelTemplate;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.resources.Identifier;

import java.util.Optional;

public class CBModelTemplates
{
    // Texture variables used by the hand-made accented models
    public static final TextureSlot TEXTURE_ONE = TextureSlot.create("texture_one");
    public static final TextureSlot TEXTURE_TWO = TextureSlot.create("texture_two");
    public static final ModelTemplate WINDOW_FULL = create("window_tintable_singlepane_full", TEXTURE_ONE, TEXTURE_TWO);
    public static final ModelTemplate WINDOW_ONE = create("window_tintable_singlepane_one", TEXTURE_ONE, TEXTURE_TWO);
    public static final ModelTemplate WINDOW_TWO = create("window_tintable_singlepane_two", TEXTURE_ONE, TEXTURE_TWO);
    public static final ModelTemplate WINDOW_THREE = create("window_tintable_singlepane_three", TEXTURE_ONE, TEXTURE_TWO);
    public static final ModelTemplate WINDOW_FOUR = create("window_tintable_singlepane_four", TEXTURE_ONE, TEXTURE_TWO);
    public static final ModelTemplate WINDOW_FIVE = create("window_tintable_singlepane_five", TEXTURE_ONE, TEXTURE_TWO);

    public static final ModelTemplate ACCENTED_PILLAR = create("accented_pillar", TEXTURE_ONE, TEXTURE_TWO);
    public static final ModelTemplate ACCENTED_PILLAR_CAP = create("accented_pillar_cap", TEXTURE_ONE, TEXTURE_TWO);



    public static final ModelTemplate TINTED_CUBE_ALL = create("tinted_cube_all", TextureSlot.ALL);

    public static final ModelTemplate TINTED_STAIRS_STRAIGHT = create("tinted_stairs", TextureSlot.BOTTOM, TextureSlot.TOP, TextureSlot.SIDE);
    public static final ModelTemplate TINTED_STAIRS_INNER = create("tinted_inner_stairs", "_inner", TextureSlot.BOTTOM, TextureSlot.TOP, TextureSlot.SIDE);
    public static final ModelTemplate TINTED_STAIRS_OUTER = create("tinted_outer_stairs", "_outer", TextureSlot.BOTTOM, TextureSlot.TOP, TextureSlot.SIDE);

    public static final ModelTemplate TINTED_SLAB_BOTTOM = create("tinted_slab", TextureSlot.BOTTOM, TextureSlot.TOP, TextureSlot.SIDE);
    public static final ModelTemplate TINTED_SLAB_TOP = create("tinted_slab_top", "_top", TextureSlot.BOTTOM, TextureSlot.TOP, TextureSlot.SIDE);
    public static final ModelTemplate TINTED_SLAB_DOUBLE = create("tinted_slab_double", "_double", TextureSlot.BOTTOM, TextureSlot.TOP, TextureSlot.SIDE);

    public static final ModelTemplate TINTED_VERTICAL_SLAB = create("tinted_vertical_slab", TextureSlot.BOTTOM, TextureSlot.TOP, TextureSlot.SIDE);

    public static final ModelTemplate TINTED_WALL_POST = create("tinted_wall_post", "_post", TextureSlot.WALL);
    public static final ModelTemplate TINTED_WALL_LOW_SIDE = create("tinted_wall_side", "_side", TextureSlot.WALL);
    public static final ModelTemplate TINTED_WALL_TALL_SIDE = create("tinted_wall_side_tall", "_side_tall", TextureSlot.WALL);
    public static final ModelTemplate TINTED_WALL_INVENTORY = create("tinted_wall_inventory", "_inventory", TextureSlot.WALL);

    public static final ModelTemplate TINTED_BUTTON = create("tinted_button", TextureSlot.TEXTURE);
    public static final ModelTemplate TINTED_BUTTON_PRESSED = create("tinted_button_pressed", "_pressed", TextureSlot.TEXTURE);
    public static final ModelTemplate TINTED_BUTTON_INVENTORY = create("tinted_button_inventory", "_inventory", TextureSlot.TEXTURE);

    public static final ModelTemplate TINTED_PRESSURE_PLATE_UP = create("tinted_pressure_plate_up", TextureSlot.TEXTURE);
    public static final ModelTemplate TINTED_PRESSURE_PLATE_DOWN = create("tinted_pressure_plate_down", "_down", TextureSlot.TEXTURE);

    // Template with no suffix: the generated model is named exactly after the block
    private static ModelTemplate create(String parent, TextureSlot... slots)
    {
        return new ModelTemplate(Optional.of(modLocation(parent)), Optional.empty(), slots);
    }

    // Template with a suffix: e.g. crystal_stone_red_stairs + "_inner"
    private static ModelTemplate create(String parent, String suffix, TextureSlot... slots)
    {
        return new ModelTemplate(Optional.of(modLocation(parent)), Optional.of(suffix), slots);
    }

    private static Identifier modLocation(String parent)
    {
        return Identifier.fromNamespaceAndPath(CBReferences.MODID, "block/" + parent);
    }
}
