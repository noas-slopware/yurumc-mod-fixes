# YuruMC mod fixes

Small NeoForge 1.21.1 fix mods for the New YuruMC Modded pack. Both are code-less (`lowcodefml`)
mods that only ship mixins. AI-assisted; tested in that pack, not much beyond it.

## `yurumc_meadowfix` — Meadow ID fix (1.0.0)

For [Let's Do] Meadow 1.4.9. Meadow renames some legacy IDs (`meadow:chair` → `meadow:pine_chair`,
`meadow:cheese_rack` → `meadow:pine_cheese_rack`, `meadow:cooking_cauldron` → `meadow:cookpot`, …)
through a mixin on the `ResourceLocation` constructor. If that mixin is applied on the server but not on a
client (seen on a NeoForge client where no mixin targeting `ResourceLocation` got applied), the two sides
register different keys and the client is kicked with *"The server send registries with unknown keys"*.

The fix cancels `DataFixers.register`, so Meadow's rename table stays empty and every side registers the
original IDs. Side effect: Meadow's migration of pre-1.4.5 worlds is disabled. Install on server and clients.

## `yurumc_liftfix` — MTR fixes (1.1.0, jar `yurumc-mtrfix`)

For Minecraft Transit Railway **4.1.0-beta.2** on NeoForge 1.21.1 only (the dependency is pinned to that exact
version, since the mixins target a synthetic lambda and local variable names). Install on server and clients.

- **Lift cabins render again** (client). 4.1.0-beta.2 removed `ModelLift1` and commented out the cabin
  render call in `RenderLifts`, so lifts move and can be ridden but are invisible
  ([MTR#1449](https://github.com/Minecraft-Transit-Railway/Minecraft-Transit-Railway/issues/1449)).
  `LiftCabinModel` is a port of 4.0.6's `ModelLift1` onto vanilla model parts (same geometry, UVs, tiling,
  door animation and light passes); `RenderLiftsMixin` hooks the per-lift render lambda where the cabin's
  transformations are computed and schedules the cabin with them.
- **Block entity changes reach clients again** (common). 4.0.x block entities called `markDirty2()`, which
  also sent a block update; 4.1 calls plain `setChanged()`, which only marks the chunk dirty. Symptom: links
  made with the lift buttons link connector (and PIDS/sign/signal/sensor settings) don't show up until the
  chunk reloads. `BlockEntityExtensionMixin` makes `setChanged()` on MTR block entities send the update again.
  Every MTR call site is event-driven (config screens, linking), not per tick.

## Building

```sh
./build.sh meadowfix   # -> build/yurumc-meadowfix-<version>.jar
./build.sh mtrfix      # -> build/yurumc-mtrfix-<version>.jar
```

`build.sh` compiles against a local Prism Launcher install: Minecraft 1.21.1 (mojmap) and its libraries,
NeoForge 21.1.262 (for the bundled MixinExtras), sponge-mixin, and for `mtrfix` the MTR jar from an instance.
Override `PRISM`, `INSTANCE` and `JAVA_BIN` (a JDK 21+) as needed.

## License

MIT. `LiftCabinModel.java` is derived from Minecraft Transit Railway 4.0.6 `ModelLift1` (MIT).
