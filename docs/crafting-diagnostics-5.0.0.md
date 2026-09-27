# Crafting Diagnostics 5.0.0-SNAPSHOT

## Verified runtime path (baseline `ce9a471`)

- `ProductionTabletItem.use` and the client key binding in
  `ForeverProductionMonitorClient.clientTick` call `ClientTabletHooks.open`,
  which instantiates `ProductionMonitorScreen`. Its `init` creates the active
  tabs; `render` calls `drawContent`. The new tab opens `CraftingDiagnosticsScreen`.
- `ForeverProductionMonitor` registers `MonitorNetwork.register` on the mod
  bus. The new request and response are registered there with **new IDs**; the
  existing packets and codecs are untouched. Requests use the existing
  `MonitorNetwork.linkedMonitor` tablet authorization on the server thread.
- The map is instantiated in `ProductionMonitorScreen.init`, receives
  `NetworkMapPayload` through `ClientMonitorState`, and `focusPosition`
  selects a matching map node. Provider locations use the existing
  `ProductionMonitorBlockEntity.locationOf` resolver and the existing
  dimension switching and map request path.
- Settings still open `ProductionMonitorThemeScreen`; no config or persisted
  data structure was changed. The monitor is the same block entity registered
  by `ModContent` and backed by `getMainNode().getGrid()`.

## AE2 source and evidence

The pinned Gradle dependency is `org.appliedenergistics:appliedenergistics2:19.2.17`.
The implementation was checked against upstream tag `neoforge/v19.2.17`
(`79ee2c704ad62941a426c26b1cb1f76ef5b2ee5a`):

- `ICraftingService.getCpus` lists CPUs; `ICraftingCPU.isBusy`,
  `getJobStatus`, `getName`, `getAvailableStorage` and `getCoProcessors`
  expose a job target and CPU metadata. The public job status also has
  progress and elapsed time, which are deliberately unused.
- `IGrid.getNodes`, `IGridNode.getService(ICraftingProvider.class)`,
  `ICraftingProvider.getAvailablePatterns` and `getPatternPriority` expose
  node-backed patterns and providers. `IPatternDetails` exposes definition,
  inputs, outputs, and input alternatives.
- `IGridNode.isPowered`, `meetsChannelRequirements`, `hasGridBooted`
  and `isActive` expose network state. They do **not** expose an external
  machine's processing state.
- AE2's service also accepts global crafting providers that have no grid
  node. These cannot be given a reliable map location by this inspector and
  are outside its node-backed provider scan. The API does not expose a
  running job's full per-CPU pending pattern steps or the provider currently
  handling a job. No such assignment is displayed.

## Behavior and limits

The inspector shows running CPU targets/metadata, node-backed provider
status, exact encoded-pattern duplicates, same-primary-output input variants,
pattern inputs/outputs, provider trace with map navigation, and direct plus
transitive reverse dependencies. Graph traversal uses a visited set and
reports cycles in the visible graph. Crafting and processing patterns are
marked separately; unknown pattern implementations are labeled `Other`.
Duplicates and variants are information, not automatic errors.

The four inspector tabs have distinct AE2/vanilla item symbols. Job targets,
pattern inputs/outputs and providers show their item or fluid icon via AE2's
existing GUI renderer. Only the visual key with secondary components removed
travels in the diagnostics response; exact keys remain on the server for
duplicate and dependency analysis. Tooltip and wrapped explanatory text
help distinguish the sections without changing AE2's own crafting screen.
Both client and server must use the same 5.0.0 test JAR because this update
extends the new diagnostics response format.

Snapshots are requested when the screen opens or the user presses Refresh.
They are captured on the server thread, limited to 64 busy CPUs, 256 provider
nodes, 256 distinct encoded patterns and 2,048 encountered pattern slots.
Only the first 12 inputs and 8 outputs of a pattern are transferred. Any
limit reached marks the **entire snapshot partial**. Results are a snapshot
in time; they are not persisted or presented as continuously live. The
network graph only covers patterns included in this snapshot.

No ETA, cancel action, planner, machine state, provider-job correlation,
long-stall signal, expectation-based redundancy warning, global-provider
trace, CPU-to-provider topology edges or unrelated-node dimming is produced.
The map action focuses one selected physical provider via the existing
dimension-aware map, which can itself be truncated at its existing node
limit.

## In-game validation

1. Open a linked tablet and switch among all original tabs; verify the
   Crafting tab opens and Back returns to the same monitor.
2. Start several AE2 autocrafts, inspect busy CPU targets, names, storage
   and co-processors; check the icons, tab labels at different GUI scales,
   hover tooltips and fully readable explanatory text. No ETA or cancel button
   should appear.
3. Place identical patterns on two providers and two different recipes for
   the same output. Check informational diagnostics and both provider traces.
4. Build chained and cyclical patterns; verify direct/transitive reverse
   dependencies without a freeze. Verify partial scan warning on a large net.
5. Include a provider in another dimension behind a Quantum Bridge; locate
   it on the existing map, then test navigation, filters, Missing Channel
   locator and ExtendedAE Wireless Connector paths.
6. Test offline/unpowered/no-channel provider nodes, map truncation, tablet
   and monitor links, settings/theme/animation, Production, Storage,
   Channel Devices, Alarms and Mini HUD.
