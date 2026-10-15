# Constructor and Pattern Planner integration test — 5.4.0-SNAPSHOT

## Accepted pairing — 15 October 2026

Use **FPM 5.4.2-SNAPSHOT + DeepCore 0.9.1-SNAPSHOT**. The project author confirmed the combined DeepCore setup and kit crafting work and explicitly authorized merging. Tested implementation commits are FPM `b8d4d84345d0a4322792b3122c793a86566b641e` and DeepCore `39726c38c4804ba3b63fdad32b0d9b615bded9ee`. Acceptance-documentation updates leave production code/resources unchanged.

DeepCore 0.9.1 retains the new GUI and Facility Upgrades as well as constructor APIs and the rebuilt-controller Survey fix. Earlier 0.8.1/0.8.2 pairings below describe historical iterations. Retain the scenarios below for regression testing; the user's acceptance is not a claim that each scenario was separately logged.

## 5.4.2 CPU tracking follow-up

Use FPM 5.4.2-SNAPSHOT with DeepCore 0.9.1-SNAPSHOT. A successful
job submission no longer requires a discoverable native AE2 CPU/link. Returned
links and public addon CPU job status are observed where available, with actual
delivered component stock as the fallback. Test an addon/fast first job (such as
the Energy Acceptor), then verify the queue continues through every component
and checks the final full kit. Also test a native CPU, partial output and a
known cancellation. With neither output nor an observable running job for ten
minutes the queue reports a stalled job; an observed running job has no such
timeout. Already submitted player jobs remain ordinary AE2 jobs.

## 5.4.1 regression follow-up

Use FPM 5.4.1-SNAPSHOT with DeepCore 0.8.2-SNAPSHOT for the follow-up to the
first integration test. The sequential crafting queue supersedes the independent
preflight/partial-submission limitation described below: components that consume
other kit components are requested first, then every deficit is recalculated
from the actual ME and player inventory after each completed job. Final success
requires the complete kit to be present. The queue is session-only, revalidates
the equipped tablet/network, and does not resume remaining jobs after logout.
Already submitted AE2 jobs remain in AE2 and can complete normally.

Regression tests: craft Tier III with only one CPU and with frames/casings in ME
that are also consumed by other kit recipes; confirm final item counts rather
than only job counts. Test an occupied CPU, a missing ingredient and cancellation.
Test more than nine distinct crafting outputs with AE2's auto-pin enabled; the
oldest pin should be replaced without the negative-subList exception. Test a
fresh controller in an already surveyed chunk: first Survey must reuse the
finite catalogue and select an eligible deposit without refilling its reserve.

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
- Use the paired DeepCore 0.9.1-SNAPSHOT baseline for the construction API,
  dismantling, new facility GUI/upgrades and rebuilt-controller Survey fix.
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
In 5.4.2 the planner submits component jobs sequentially, crafts consuming
components first, recalculates deficits against live stock, and reconciles the
final complete kit. The queue does not atomically reserve the entire kit;
external consumers may still change stock, with bounded reconciliation.

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
CI confirms compilation and packaged resources. The project author approved
merging after the combined runtime test on 15 October 2026. Publication is a
separate action and has not been requested.
