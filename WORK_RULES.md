# WORK_RULES.md

# Forever Production Monitor — Mandatory Work Rules

These rules apply to **every** code change in the `TeKatz/ForeverProductionMonitor` repository.

They are not optional guidance. They are project-level requirements intended to prevent regressions, wasted work, changes to unused code paths, and unnecessary consumption of Work/agent capacity.

---

## 1. Source of truth

- The GitHub repository is the source of truth.
- Use the current stable `main` branch as the baseline unless the task explicitly names another branch.
- Do not reconstruct or decompile the mod again unless explicitly requested.
- Do not treat an old JAR, old branch, previous Work output, class name, filename, or comment as authoritative when the current repository can be inspected directly.
- When a task refers to an existing feature, GUI, class, method, integration, or workflow, verify its current implementation in the repository before modifying it.

---

## 2. Cardinal rule: verify the real runtime path before editing

**Never change existing code based only on names, assumptions, apparent purpose, or file location.**

Before modifying an existing GUI, method, class, event handler, integration, network path, storage path, or rendering path, first verify that the exact implementation is actually used by the running mod.

Verification must be based on real code paths such as:

- constructor calls / instantiations,
- method call sites,
- event registration,
- screen registration,
- button callbacks,
- packet registration and handlers,
- NeoForge registration,
- AE2 integration hooks,
- Curios hooks,
- client/server entry points,
- references from active classes,
- actual data flow between caller and callee.

A class with a plausible name is **not evidence** that it is active.

If a class exists but has no active call path, do not modify it unless the task explicitly concerns that class.

### Mandatory pre-edit check

Before editing an existing implementation:

1. Find all relevant references.
2. Identify the actual entry point.
3. Follow the call path to the target code.
4. Confirm that the target code is reachable in normal runtime behavior.
5. Only then edit it.

If the runtime path is unclear, stop and investigate. Do not guess.

---

## 3. Known example of a forbidden mistake

A previous change added a version label to `ProductionMonitorConfigScreen` because its name looked like the correct settings screen.

That was wrong.

At that time:

- `ForeverProductionMonitorClient` registered `ProductionMonitorThemeScreen` as the config screen.
- `ProductionMonitorScreen` opened `ProductionMonitorThemeScreen` from the gear button.
- `ProductionMonitorConfigScreen` was not part of the active GUI path.

This type of mistake must not happen again.

**General lesson:** never infer active behavior from class names.

This example is historical context only. Re-verify the current code every time because architecture can change.

---

## 4. Branch safety

For every feature, bugfix, GUI change, optimization, or experiment:

- Start from the current stable `main`.
- Create a dedicated branch.
- Use a clear branch name, for example:
  - `feature/...`
  - `fix/...`
  - `optimization/...`
  - `maintenance/...`
- Never implement experimental work directly on `main`.
- Never merge into `main` automatically.
- Keep the pull request as a Draft until the user has tested and approved the resulting JAR.
- `main` must remain the last known-good, user-tested version.

Only merge after explicit user approval.

---

## 5. Minimal-change rule

Implement only what was requested.

Do not add:

- unrelated features,
- opportunistic refactors,
- style cleanups,
- architecture changes,
- renamed classes,
- moved code,
- rewritten methods,
- dependency upgrades,
- formatting sweeps,
- GUI redesigns,
- performance changes,

unless they are explicitly required by the task.

Prefer the smallest safe change that satisfies the request.

If one line is enough, do not rewrite a method.

If one method is enough, do not refactor a class.

If one class is enough, do not touch five.

---

## 6. Decompiled-source caution

The current source was reconstructed from the stable 2.2.1 JAR and later made buildable.

Therefore:

- Treat unusual variable names, synthetic constructs, switch helpers, generic casts, records, lambdas, and decompiler artifacts with caution.
- Do not "clean up" reconstructed code unless necessary for the requested task.
- Do not rewrite decompiled logic just because a cleaner implementation is possible.
- Preserve behavior over aesthetics.
- If a decompiled construct looks strange but works, leave it alone unless the task requires touching it.
- If a change intersects uncertain reconstructed logic, compare against the known-good behavior and make the smallest possible modification.

Do not directly patch compiled bytecode.

All changes must be made in source and rebuilt normally.

---

## 7. Build success is necessary, but not sufficient

A green GitHub Actions build only proves that the project compiled successfully.

It does **not** prove that:

- the correct class was changed,
- the code path is actually used,
- the GUI change is visible,
- runtime behavior is correct,
- Minecraft can load the mod,
- AE2 integration still works,
- Curios integration still works,
- saved data still works,
- the task was functionally implemented.

Every task must include both:

1. **technical validation** — successful build,
2. **functional validation** — confirmation that the changed code is on the real runtime path and logically satisfies the request.

Never report a task as complete only because CI is green.

---

## 8. Mandatory diff review

Before considering a task complete:

- Compare the branch against `main`.
- Review every changed file.
- Confirm that every changed file is necessary.
- Check for accidental formatting or decompiler noise.
- Check for unrelated changes.
- Check for unexpected resource changes.
- Check that no stable feature was removed or altered unintentionally.

If the requested task is small, the diff should also be small.

A surprisingly large diff for a small task is a warning sign and must be investigated before continuing.

---

## 9. GUI-specific rules

Before changing any GUI:

- Verify which screen is actually instantiated.
- Verify how it is opened.
- Verify which parent screen or entry point leads to it.
- Verify the render method that is actually called.
- Verify widget coordinates and layout based on the active screen, not a similarly named class.
- Do not move unrelated elements.
- Do not change hitboxes unless required.
- Do not change scaling, panel dimensions, search behavior, scrolling, tab logic, theme behavior, or keyboard/mouse controls unless requested.
- If a visual change is requested, keep it visually isolated.

For existing screens, always trace:

`entry point -> screen creation -> init -> render / callbacks`

before editing.

---

## 10. Network and persistence safety

Do not change any of the following unless explicitly required:

- packet IDs,
- packet structure,
- packet codecs,
- client/server direction,
- NBT structure,
- saved data format,
- monitor link format,
- dashboard persistence,
- alarm persistence,
- config keys,
- enum serialization,
- world data compatibility.

If a task requires touching one of these areas:

- identify all readers and writers first,
- verify backward compatibility,
- document the risk,
- do not proceed with a breaking change unless explicitly approved.

---

## 11. AE2 and Curios integration safety

Treat AE2 and Curios integration as high-risk areas.

Before changing integration code:

- identify the currently used API calls,
- verify the supported dependency versions,
- verify client/server side usage,
- verify that no reflection fallback is unintentionally removed,
- avoid changing behavior outside the requested feature.

Do not upgrade AE2, Curios, NeoForge, mappings, or Java target versions unless explicitly requested.

---

## 12. Version changes

When creating a new mod version:

- update the version consistently in all required project metadata,
- verify the output JAR filename/version,
- do not leave stale version labels in visible UI or metadata,
- do not change version numbers in unrelated historical documentation unless needed.

The user-facing JAR and mod metadata must refer to the same version.

---

## 13. GitHub Actions / CI rules

GitHub Actions should be used as a verification layer.

The build should:

- use Java 21,
- run a clean Gradle build,
- fail on compiler errors,
- upload the produced JAR as an artifact after success.

Avoid duplicate or misleading CI naming.

Use neutral artifact/workflow names that remain correct across future versions rather than names tied permanently to 2.2.1.

CI success must never replace runtime-path verification or user testing.

---

## 14. Pull request rules

Every Work-generated change should normally produce a Draft Pull Request.

The PR must include:

- purpose of the change,
- branch name,
- changed files,
- concise implementation summary,
- validation performed,
- known limitations or risks,
- explicit statement that `main` was not changed.

Do not mark the PR ready for merge unless instructed.

Do not merge without explicit approval.

---

## 15. JAR delivery

After a successful build:

- provide the newly built `.jar` to the user,
- identify the exact branch,
- identify the exact commit,
- state the version,
- state which files were changed,
- state what should be tested in-game.

The user performs the final runtime validation in Minecraft.

Until that test succeeds, the change is not considered stable.

---

## 16. Stop conditions

Stop and investigate instead of guessing if any of the following occurs:

- the intended class has no clear call site,
- multiple similar implementations exist,
- the runtime path is ambiguous,
- a small task unexpectedly changes many files,
- a build fix requires broad rewrites,
- a dependency mismatch appears,
- a change requires modifying network or persistence formats unexpectedly,
- behavior cannot be verified from the current code,
- a decompiler artifact makes the original logic uncertain.

Do not "make something work" by broad trial-and-error modifications.

Report the uncertainty and identify the exact blocker.

---

## 17. No blind retries

Do not repeatedly rebuild or make speculative changes without using the previous error output.

For every failed build:

1. read the actual error,
2. identify the exact source,
3. make the smallest targeted correction,
4. rebuild,
5. repeat only if a new concrete error remains.

Do not consume repeated build cycles without a specific hypothesis.

---

## 18. No assumption-driven implementation

Forbidden reasoning patterns include:

- "This class name sounds right."
- "This file probably controls the screen."
- "This method looks like the one."
- "The build passed, so the feature works."
- "This old implementation is probably still active."
- "It is safe to refactor while I am here."
- "The requested change is small, so runtime-path verification is unnecessary."

Each of these must be replaced by direct code verification.

---

## 19. Required completion checklist

Before reporting any task as finished, verify all of the following:

- [ ] Started from the intended base branch.
- [ ] Worked only on a dedicated branch.
- [ ] `main` was not modified directly.
- [ ] Actual runtime/call path was verified before editing.
- [ ] Target class/method is confirmed to be used.
- [ ] Only requested behavior was changed.
- [ ] No unnecessary refactoring was performed.
- [ ] Diff against `main` was reviewed.
- [ ] Every changed file is justified.
- [ ] Build completed successfully.
- [ ] Produced JAR exists.
- [ ] Functional path of the new change was re-verified after implementation.
- [ ] Draft PR remains unmerged.
- [ ] Branch, commit, changed files, and test instructions are reported.
- [ ] User still has the opportunity to test the JAR before merge.

If any required item is not satisfied, do not present the task as complete.

---

## 20. Project priority order

When tradeoffs are necessary, use this priority:

1. Preserve the stable working mod.
2. Preserve existing behavior.
3. Correctly identify the real runtime path.
4. Make the smallest safe change.
5. Produce a reproducible successful build.
6. Make the result easy for the user to test.
7. Optimize elegance or code cleanliness only when explicitly requested.

Stability and correctness are more important than cleverness.

---

## 21. Final rule

**Do not guess when the repository can answer the question.**

Search the code.
Trace the call path.
Verify the target.
Change only what is necessary.
Build it.
Review the diff.
Give the user the JAR.
Do not merge until the user confirms it works in-game.
