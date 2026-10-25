NeoForge 26.3.0.94, Minecraft 26.3. I'm coming from 1.20 and JDK 8. Explain new APIs and modern Java features as you use them. I copy code in myself, so give complete, clearly placed snippets.

## Project state
- All blocks are "tintable": one non-ticking block entity (`CBTintBlockEntity`) stores an RGB colour per tint region.
- Every dyeable block implements `IColorableBlock` (creates the block entity, handles dyeing with leather-armour mixing). Override `getTintRegionCount` / `getTintRegion` / `getRegionForTintIndex` / `getAddedRegion` for multi-region blocks (see `CBTintableSlab`).
- Colouring is free (no dye consumed). There is no washing. Broken blocks always drop at the default colour (plain `dropSelf`, no `copy_components`).
- Shapes done: full block, stairs, two-tone slab, vertical slab, wall, button, pressure plate, vertical column, horizontal column, accented pillar (wall/floor/ceiling), accented pillar cap, connected window.
- Custom Blockbench models with body (tintindex 0) + accent (tintindex 1): extend `CBAccentedBlock`, list the model's element boxes (north-facing) as `CBPixelBox`es, and return the blockstate x/y quarter turns per state. Shapes, click-to-dye-region and datagen rotations all come from those. Hand-made parent models live in `models/block/` and use `texture_one` / `texture_two` (`CBModelTemplates.TEXTURE_ONE/TWO`).
- Window (`CBTintableWindow`, datagen in `CBWindowModels`, not part of the family): facing north/east only; joins to same-facing windows up/down/left/right (left/right = the model's north/south edges). Six hand-made `window_tintable_singlepane_*` models, rotated in-plane with blockstate x rotations, cover all 16 join combinations; `CBWindowModels.choose` picks model + rotation automatically. Corner properties (`up_left` etc.) add generated 2x2 corner frame pieces where both edges are joined but the diagonal window is missing. Blockstate is multipart (window part + corner parts). Frame = region 0 (`tintable_stone`), glass = region 1; the first dye on the glass sets `tinted_glass=true`, swapping `cb_glass` for `tintable_glass` (a reset tool is planned).
- Block sets: each texture is a `CBBlockSet` (record of all 12 shapes) registered with one `CBBlockRegistry.registerSet(name, displayName, CBMaterial)` line and listed in `CBBlockRegistry.SETS`. The set name = base block id = texture file name. `CBMaterial` (STONE/WOOD/METAL) holds map colour, sound, button/plate set type, tool tag and tags.
- Everything loops over `SETS` or the block register: models + windows (`CBBasicModelProvider`), tags, loot (`dropSelf`, slab table for `SlabBlock`), lang (`CBLanguageProvider`, no hand-written en_us.json), creative tab, shape recipes (`shapeRecipes`), client tints (one source per tint region of every `IColorableBlock`), block entity type.
- Adding a texture: PNG + one `registerSet` line + add to `SETS` (+ optional base block recipe). Adding a new shape: block class, a field in `CBBlockSet` (+ `blocks()`), register it in `registerSet`, a `CBTintedBlockFamily` method and a lang suffix in `CBLanguageProvider`.- Datagen: models via `CBTintedBlockFamily` (hand-written `tinted_*` parent models, or `ExtendedModelTemplateBuilder` for generated ones). Recipes and loot tables go through `GatherDataRegistryEntriesEvent`. Each model file must be created only once per datagen run (create shared models before loops).
- Window bends: a window with exactly one in-line window on an east/west side and one on a north/south side becomes an L (`bend` property: none/north_west/north_east/south_east/south_west). Bend models are generated in `CBWindowModels.bendTemplate` for north_west and rotated; for bends, left/right = first/second arm.
- Beams can have a `modelXTurns()` base rotation (chain models are made running north-south) and `turnCount()` 1/2/4 (`turn` property). `connectionPoint(...)` lets another kind of beam snap to a spot when placed against it.
- Chained support beam: renders the plain `bolted_support_beam` plus a generated half-chain (`chain_beam_half.json`, tintindex 1) on each side whose `chain_<side>` flag is on (sides = as the beam stands after its turn). The half-chain follows the beam across its side but never along its own length, so it always starts at the block edge. Chains (vanilla `ChainBlock` or `CHAIN` kind beams) attach only when lined up within half a pixel.

## 26.x notes
- Render layer (solid/cutout/translucent) is chosen per face from the texture's alpha; models no longer set a render type. Use `noOcclusion()` on see-through blocks.
- Multipart blockstates: `MultiPartGenerator.multiPart(block).with(BlockModelGenerators.condition().term(...).term(...), variant)`; chained terms are ANDed.
- `PropertyDispatch.initial(...)` works with up to 5 properties; `BlockModelGenerators.X_ROT_*`, `Y_ROT_*`, `UV_LOCK`, `ROTATION_HORIZONTAL_FACING` are available.
- Textures in datagen are `Material`s (`net.minecraft.client.resources.model.sprite.Material`): `new Material(identifier)`, `TextureMapping.put(slot, material)`.

## Ideas, not started
- Item that colours blocks as they're placed (`applyImplicitComponents` / `getAddedRegion` already support it)
- Easier colouring of many blocks at once
- Tool to reset window glass back to `cb_glass` (set `tinted_glass=false`)
- Chisel: right-click swaps a block to the same shape in the next `CBBlockSet` (`withPropertiesOf` + copy block entity colours)
- 
## Working with Claude
- Pull and read the repo only; never commit or push. I handle git.
- To check 26.3 APIs, clone the public `neoforged/neoforge` repo (branch `26.3.x`) and read the 26.3 porting primer in `neoforged/.github` (`primers/26.3/index.md`; 26.1 and 26.2 primers cover earlier changes). NeoForge's docs are in the public `neoforged/Documentation` repo (`docs/`).