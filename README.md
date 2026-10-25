# YuruMC mod fixes

Small NeoForge 1.21.1 fix mods for the New YuruMC Modded pack. The Meadow and MTR fixes are code-less
(`lowcodefml`) mods that only ship mixins; the Airwaves one is a regular (`javafml`) mod. AI-assisted; tested in
that pack, not much beyond it.

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

## `yurumc_airwavesfix` — Create: Airwaves fixes (1.0.0)

For Create: Airwaves **0.1.0 build 9.9** (shipped as `airwaves-1.21.1-0.1.1-atm10.jar`; the dependency is pinned
to it, since the mixins target that exact build). Etched 5.1 is optional. Install on server and clients.

- **No microphone, ever.** The client never opens the mic (Airwaves opened it on every login, voice-activated and
  unmuted), and the server drops voice packets, Simple Voice Chat audio, and all world-sound and jukebox capture.
  The Microphone block stays registered so worlds and Airwaves' own setup code keep working, but it is uncraftable,
  hidden from creative tabs, JEI and Ponder, can't be placed, and placed ones are inert. The always-on voice pill
  is gone from the HUD; V opens a small Radio Settings screen (radio volume, mute) instead of the voice menu. Old
  tapes with recorded voices play only their sounds; their audio files in `<world>/airwaves_tapes/` are no longer
  read and can be deleted.
- **The Tape Player records music discs.** It gets a disc slot (vanilla discs, and Etched discs when Etched is
  installed). Record dubs the disc in real time while it plays out of the player; swapping discs mid-recording makes
  a mixtape, leading silence is trimmed, and a vanilla disc stops the recording when it ends. Etched only streams
  on clients, so the server can't know a track's length: those recordings run until Record is pressed again (or
  the 5 minute tape is full), and only an album's first track is dubbed. Etched tracks travel through Airwaves as a
  `yurumc_airwavesfix:etched/<hex url>/<hex title>` sound id and are played by `EtchedRadio` on the client, seeking
  into the track like Airwaves does for vanilla discs. Tape tooltips list the recorded tracks.
- **Server fixes.** The spectrum request (signal and terrain math for every station, sent by the Tuning Table)
  needs the Tuning Table open and is limited to once every 10 ticks per player; station names over 64 characters no
  longer kick everyone who opens a Tuning Table near that station; radio and radio console seeking has a 4 tick
  per-player cooldown; the Dimensional Relay only keeps its chunk loaded while it is running, using its own chunk
  ticket instead of the shared vanilla forced-chunk flag (two relays in one chunk no longer unforce each other,
  and orphaned tickets are dropped on load); handheld radios of logged-out players are forgotten; receiver cable
  paths are de-duplicated in linear instead of quadratic time; swapping tapes during playback stops the old tape.
- **Tapes are craftable.** Airwaves' tape recipe makes 2 tapes, but tapes stack to 1, so the recipe failed to load.
  It now makes 1.
- Lang, Ponder and Field Manual text no longer mention microphones or voice chat.

## Building

```sh
./build.sh meadowfix   # -> build/yurumc-meadowfix-<version>.jar
./build.sh mtrfix      # -> build/yurumc-mtrfix-<version>.jar
./build.sh airwavesfix # -> build/yurumc-airwavesfix-<version>.jar
```

`build.sh` compiles against a local Prism Launcher install: Minecraft 1.21.1 (mojmap) and its libraries,
NeoForge 21.1.262 (for the bundled MixinExtras, and in full for `airwavesfix`), sponge-mixin, and the target
mods' jars from the New YuruMC Modded instance (MTR; Airwaves, Create and Etched). Override `PRISM`, `INSTANCE`
and `JAVA_BIN` (a JDK 21+) as needed.

## License

MIT. `LiftCabinModel.java` is derived from Minecraft Transit Railway 4.0.6 `ModelLift1` (MIT).

Exceptions in `airwavesfix`, derived from Create: Airwaves (CC BY-NC-SA 4.0) and under that license:
`assets/airwaves/manual/en_us.json` and `assets/airwaves/lang/en_us.json` (edited Airwaves text), and the parts of
`RadioHud` and `EtchedRadio` adapted from Airwaves' `ClientInit.hud` and `RadioSounds`.
