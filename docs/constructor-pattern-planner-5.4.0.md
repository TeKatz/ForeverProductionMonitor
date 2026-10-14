# Constructor and Pattern Planner integration test — 5.4.0-SNAPSHOT

This development build starts from the user-tested FPM 5.2.0 main commit
`87c299b26dd5f03048ddf79ea08dae45e1b8c216`. It integrates the existing constructor
branch `631c6ef` and JEI queue branch `b778dc3`. It does not include Fleet
Diagnostics from the separate 5.3.0 branch. The public release remains 5.0.1.

The Network Map renderer is unchanged from the tested baseline, including the
5.0.1 BufferBuilder lifecycle and warning-layout fixes.

## Required test setup

- Minecraft 1.21.1 / NeoForge; the paired DeepCore build requires NeoForge
  21.1.252+ and AE2 19.2.18+. AE2 must remain below 20.
- Curios 9.5.1+ and the linked Production Tablet equipped in its Curios slot.
- Use the paired DeepCore 0.8.1-SNAPSHOT build for the construction API and
  dismantling implementation. DeepCore main 0.8.0 does not expose these APIs.
- JEI with the recipe-side-button API (compiled against 19.44.0.406) and
  ExtendedAE with an active Assembler Matrix Pattern Core for Pattern Planner.
- Replace older copies of these same mods; client and server use matching builds.

## Construction Planner

Open Production Tablet -> Crafting Diagnostics -> Deep Core Build. Select
Tier I, II or III. Check item icons, required / available / missing counts and
pattern availability. Available includes matching player-inventory items.

Test sufficient stock, mixed inventory/ME stock, missing patterns and missing
ingredients. Request missing components and verify AE2 jobs and output counts.
The tier is cumulative; Tier III includes the complete lower-tier structure.
The planner preflights separate component jobs but does not reserve all their
ingredients or CPUs atomically. A later submission may report partial success.

## Multiblock Constructor

Craft or obtain the red Multiblock Constructor and use it on the Production
Monitor to link it. The constructor has its own link, independent of the tablet.

- Sneak + use in air cycles Preview -> Build -> Dismantle.
- In Preview, ordinary use in air cycles Tier I -> II -> III.
- Use on a block in Preview/Build to anchor the controller at the adjacent
  block position; the player's horizontal direction supplies the orientation.
- In Build mode, use in air to start placing up to four blocks per server tick.
- In Dismantle mode, use on a matching member of an existing Deep Core.

Test all tiers and horizontal orientations. Check red blocked positions and
green matching blocks. Construction should use inventory before ME stock,
reuse matching blocks and rotate an incorrectly oriented embedded Drive
without consuming a second Drive. After building, supply power, a storage cell
and Tier-III coolant as appropriate, then test Survey and extraction.

Test insufficient materials, obstructed placement and a lost ME connection.
Dismantling should return matching structure blocks, preserve AE2 Drive
contents through AE2's wrench path, and leave foreign blocks intact.
Also check installed Bore upgrade cards when dismantling the controller.

## Pattern Planner (JEI queue v2)

The blank-pattern icon appears beside eligible JEI Crafting/Smithing recipes.

- Click: import this recipe immediately.
- Shift + click: add/remove the recipe in the session-local queue (128 maximum).
- Ctrl + click: upload the queue and show a result summary.

Each successful new pattern consumes one Blank Pattern from the linked ME
network and occupies a free ExtendedAE Assembler Matrix Pattern Core slot.
Blank Patterns must already be stored; automatic blank-pattern crafting is
not implemented. The importer chooses a deterministic representative ingredient.
Special/dynamic crafting recipes are deliberately rejected.

Test shaped, shapeless and smithing recipes, repeated queue toggling, duplicate
imports, a full Matrix, no Matrix, missing Blank Patterns and an offline network.
Processing, stonecutting and other categories must not produce patterns.
Check that successfully imported patterns are usable for actual AE2 crafting.
Failed entries are reported but are not automatically restored to the queue.

## Regression and validation boundary

Recheck Network Map opening/rotation/heatmap, Deep Core remote access and its
multi-facility dropdown. Optional integrations must fail cleanly when absent.
CI confirms compilation and packaged resources; in-game approval of this
combined build is still required before merging or publishing.
