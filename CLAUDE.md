NeoForge 26.3.0.94, Minecraft 26.3. I’m coming from 1.20 and JDK 8. Explain new APIs and modern Java features as you use them. I copy code in myself, so give complete, clearly placed snippets.
## Project state
- All blocks are "tintable": one non-ticking block entity (`CBTintBlockEntity`) stores an RGB colour per tint region.
- Every dyeable block implements `IColorableBlock` (creates the block entity, handles dyeing with leather-armour mixing). Override `getTintRegionCount` / `getTintRegion` / `getRegionForTintIndex` / `getAddedRegion` for multi-region blocks (see `CBTintableSlab`).
- Colouring is free (no dye consumed). There is no washing. Broken blocks always drop at the default colour (plain `dropSelf`, no `copy_components`).
- Shapes done: full block, stairs, two-tone slab, vertical slab, wall, button, pressure plate, vertical column, horizontal column.
- New shape checklist: block class + registry + `CBTintedBlockFamily` method + client tint source + pickaxe tag + loot table + creative tab + lang. `CBBlockEntityRegistry` picks up `IColorableBlock`s automatically.
- Datagen: models via `CBTintedBlockFamily` (hand-written `tinted_*` parent models, or `ExtendedModelTemplateBuilder` for generated ones). Recipes and loot tables go through `GatherDataRegistryEntriesEvent`.

## Ideas, not started
- Item that colours blocks as they're placed (`applyImplicitComponents` / `getAddedRegion` already support it)
- Easier colouring of many blocks at once
- Custom multi-region blocks coloured by clicking each region

## Working with Claude
- Pull and read the repo only; never commit or push. I handle git.
- To check 26.3 APIs, clone the public `neoforged/neoforge` repo (branch `26.3.x`) and read the 26.3 porting primer in `neoforged/.github` (`primers/26.3/index.md`).