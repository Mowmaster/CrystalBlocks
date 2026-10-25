package com.mowmaster.crystalblocks.CBDataGeneration;

import com.mowmaster.crystalblocks.Blocks.CBBlockRegistry;
import com.mowmaster.crystalblocks.Blocks.Colorable.CBBoltedBeamBlock;
import com.mowmaster.crystalblocks.CBReferences;
import com.mowmaster.crystalblocks.Items.CBItemRegistry;
import net.minecraft.client.color.item.Dye;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.model.ItemModelUtils;
import net.minecraft.client.data.models.model.ModelLocationUtils;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.component.DataComponents;
import net.minecraft.data.PackOutput;
import com.mowmaster.crystalblocks.Blocks.CBBlockSet;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;

public class CBBasicModelProvider  extends ModelProvider {
    public CBBasicModelProvider(PackOutput output) {
        super(output, CBReferences.MODID);
    }

    @Override
    protected void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {

        itemModels.generateFlatItem(CBItemRegistry.CRYSTAL_CHISEL_BASE.get(), ModelTemplates.FLAT_ITEM);
        // Shared by every set's column copies; created once so the file is only written once
        Identifier columnTemplate = CBBoltedModels.columnTemplate(blockModels);

        for (CBBlockSet set : CBBlockRegistry.SETS) {
            new CBTintedBlockFamily(blockModels, set.base().get(), new Dye(0xFFFFFFFF))
                    .stairs(set.stairs().get())
                    .twoToneSlab(set.slab().get())
                    .verticalSlab(set.verticalSlab().get())
                    .wall(set.wall().get())
                    .button(set.button().get())
                    .pressurePlate(set.pressurePlate().get())
                    .accentedPillar(set.accentedPillar().get())
                    .accentedPillarCap(set.accentedPillarCap().get())
                    .steppedPillar(set.steppedPillar().get())
                    .steppedPillarCap(set.steppedPillarCap().get());

            // The window's frame uses the set's own texture
            CBWindowModels.generate(blockModels, set.window().get(), set.name());
            CBBoltedModels.beam(blockModels, (CBBoltedBeamBlock) set.column().get(), columnTemplate,
                    CBBoltedModels.textures(set.name(), set.name()));
        }
        CBBoltedModels.generate(blockModels);

        // Colour picker: color_picker.png when empty, color_picker_dyed.png once it holds a colour
        Item picker = CBItemRegistry.COLOR_PICKER.get();
        Identifier emptyPicker = ModelTemplates.FLAT_ITEM.create(
                ModelLocationUtils.getModelLocation(picker),
                TextureMapping.layer0(new Material(Identifier.fromNamespaceAndPath(CBReferences.MODID, "item/color_picker"))),
                blockModels.modelOutput);
        Identifier dyedPicker = ModelTemplates.FLAT_ITEM.create(
                ModelLocationUtils.getModelLocation(picker, "_dyed"),
                TextureMapping.layer0(new Material(Identifier.fromNamespaceAndPath(CBReferences.MODID, "item/color_picker_dyed"))),
                blockModels.modelOutput);
        blockModels.itemModelOutput.accept(picker, ItemModelUtils.conditional(
                ItemModelUtils.hasComponent(DataComponents.DYED_COLOR),
                ItemModelUtils.plainModel(dyedPicker),     // has a picked colour
                ItemModelUtils.plainModel(emptyPicker)));  // empty

        // Custom dye: vanilla's white dye texture, tinted with the dye's stored colour
        Identifier customDyeModel = ModelTemplates.FLAT_ITEM.create(
                ModelLocationUtils.getModelLocation(CBItemRegistry.CUSTOM_DYE.get()),
                TextureMapping.layer0(new Material(Identifier.withDefaultNamespace("item/white_dye"))),
                blockModels.modelOutput);
        blockModels.itemModelOutput.accept(CBItemRegistry.CUSTOM_DYE.get(),
                ItemModelUtils.tintedModel(customDyeModel, new Dye(0xFFFFFFFF)));
    }
}
