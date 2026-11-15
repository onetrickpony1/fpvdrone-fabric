# Wind — research and design

Status: **steps 1-3 built** (2026-11-15), step 4 to do. Owner decisions (2026-11-15): on by default and mild,
all four scopes (quads + turbulence, Mavic/GPS hold, bombs + particles, munitions), wind taken from **Weathery**
when it is installed (instead of Weather2).

### What step 1 does (code: `physics/wind/FpvWind.java`, `PhysicsState.sampleWind`, `DefaultPhysicsCore`)

- **Base wind** (m/s at 6 m over open ground): with Weathery installed, its wind through `WeatheryApi.windAngle`
  and `windSpeed(level, pos, 0)` (no height bonus; 1.0 = 8 m/s, so a normal day is 1-5 m/s and a high-wind spell
  8 m/s; storm inflow and gusts included). Looked up by reflection, no build dependency. Without Weathery: a
  built-in wind computed from game time and the rain/thunder level, the same on client and server with no
  packets: 1-4 m/s clear, up to about 7 in thunder, direction wandering all the way round over days.
- **Height profile**: log law over the local ground (`MOTION_BLOCKING_NO_LEAVES`), z0 0.03 (0.5 in forests and
  jungles), 1 at 6 m, about 0.5 at 0.5 m, capped at 1.6.
- **Shelter**: 12 heightmap samples up to 24 blocks upwind; a column `e` blocks above the drone at distance `d`
  shelters fully while `d < 3.3 e`; under a roof = 1; smoothed over 0.3 s; wind × (1 − 0.8 shelter).
- **Physics**: drag on `v − wind` (normal and falling steps), rotor drag `m · 0.4 · wind⊥`; GPS drones (Mavic,
  GPS Rescue) lean into the wind by the angle that cancels the push at hover, up to 30°.
- **Config** (server, synced): `windEnabled` true, `windStrength` 1.0 (0-3), `windSheltering` true. Command
  `/fpvdrones wind` (shows the wind where you stand), `wind on|off`, `wind strength <0-3>` (op).
- **Step 2 (turbulence, gusts, kicks)**: `physics/wind/Turbulence.java` = Dryden filters per drone (pilot's
  client) with the spec's length scales and sigmas, raised at wake edges; `FpvWind.gust` = 1-cosine gusts from a
  game-time schedule (60 % of 20 s windows, Vm 0.3-0.6 x mean, dm 5-30 m; same on every side, no packets);
  `DefaultPhysicsCore.gustKick` = blade-flapping tilt away from the relative gust, 40 deg/s^2 per m/s (lighter
  drones more), into the crash-kick rates. GPS trim uses only the steady wind. Config `windTurbulence` (1.0, 0-3),
  `windGusts` (true); `/fpvdrones wind turbulence <0-3>`, `wind gusts on|off`.
- **Step 3 (warning, bombs, particles)**: OSD warning `STRONG WIND` (lowest priority) when the steady wind at the
  drone is 10 m/s or more, or a GPS drone needs more than its 30° lean and drifts. Dropped bombs: drag on the
  velocity through the air (`DroppedBombEntity.fallStep`, wind sampled once per tick at the bomb; server, client
  substeps and the look-ahead all use it): an RGD-5 drifts about 2.4 blocks over a 60-block drop in 10 m/s.
  Particles: wind sampled once per tick at the camera (main thread); each tick the horizontal speed moves part of
  the way to it (smoke 0.2, flames/custom 0.03, airborne debris 0.01) - **check**: needs an in-game look.
- **Tests**: server `bombDriftsInWind`, smoke `strong wind warning`; server test `turbulenceAndGusts` (sigma within 20 % of the spec, gust peak <= 0.6 x mean,
  repeatable, off = 0), smoke `gust kicks` (headwind gust lifts the nose, side gust rolls away); server test `windField` (base, profile, shelter behind a wall, off = 0; with `-PwithWeathery` the
  source must be Weathery), smoke `wind drift` (falling drone drifts downwind, 0.00 m with wind off).

## What other mods and simulators do

| Source | Wind field | How it moves the vehicle | Notes |
|---|---|---|---|
| Immersive Aircraft (GPL-3.0) | none: per-aircraft noise (5 summed cosines over `tickCount/mass`) | rotation kicks + small velocity nudge | strength = config `windClearWeather 1 / Rain 3 / Thunder 3` + current speed, times a per-aircraft `wind` stat (quadrocopter 0.0125, airship 0.05; gyroscope upgrade cancels it). No direction, altitude or sheltering; not shared between players (each client has its own noise). Feels like buffeting, means nothing as wind. |
| Weather2 (Corosus) | one per dimension on the server: angle random walk (±1°/tick), speed random walk (0..1), gust every ~5 s (±60°, up to 9 s), rare calm / high-wind events (2–12 min) | eases entity velocity toward the wind, divided by weight | height boost against the *average* local terrain (heightmap grid, cached); server state synced every 3 s; storm inflow only on the client (their own TODO: client/server disagree). |
| PMWeather + its Create Aeronautics bridge (PMAero, MIT) | server `getWind(pos)` in mph, 0 below terrain or where `canWindAffect` is false | **air-relative velocity** (`v − wind`) fed into lift/drag, plus pressure on the exposed faces with a differential torque | budgets, caches and level-of-detail were needed for per-face sampling; has a switch to stop the weather mod pushing the same craft twice. |
| Immersive Engineering windmill | no direction | 7-block scan through the rotor disc counts solid blocks | best Minecraft example of block obstruction; recomputed every 128 ticks, staggered. |
| PX4 / Gazebo, RotorS, AirSim, Parrot Sphinx | mean wind + gusts (PX4: Gaussian, 2 Hz; RotorS: terrain-following grid) | air-relative velocity in every rotor and body force | Sphinx and RotorS measure height above local terrain. |
| Liftoff, Velocidrone | settings: strength, direction, turbulence, gust frequency | Liftoff: wind-relative drag + asymmetric prop loading, wind flows around terrain ("behind a building you are shielded, or thrown into turbulence") | methods not published. |

Recurring pattern: **one slow global wind per dimension (server), local modifiers on top (height, sheltering), short gusts, weather as a scale factor, and air-relative velocity in the drag.** Pitfalls: client/server disagreement, per-block sampling cost, mixed units, pushing a craft twice when a weather mod is also installed.

## Physics we can use (sources: MathWorks Dryden/wind-shear docs, Faessler et al. 2018, PX4 SITL)

- **Air-relative velocity:** `v_air = v − v_wind`. Body drag `−k·|v_air|·v_air` (our existing quadratic term, now on `v_air`).
- **Rotor drag** (blade flapping), linear: `F = −m·d·v_air,⊥` (the part not along the thrust axis), `d ≈ 0.4 s⁻¹` for a 5″ quad, ≈ 0.35 for a Mavic. This is what makes a hovering quad drift downwind.
- **Translational lift:** thrust × `(1 + k_h·v_h²/g)`, `k_h ≈ 0.009 m⁻¹` (≈ +9 % at 10 m/s). Axial inflow cuts thrust: `× (1 − v_axial/35 m/s)`.
- **Gust torques:** rotor flapping tips the drone away from the relative wind (nose-up into it) + centre-of-pressure offset (1–2 cm) → weathervaning. Tuned to ~100–300 °/s² for a 5 m/s gust.
- **Turbulence (Dryden, low altitude, metric):** `L_u = L_v = h/(0.177+0.0027h)^1.2`, `L_w = h`, `σ_w = 0.1·W6`, `σ_u = σ_v = σ_w/(0.177+0.0027h)^0.4` (W6 = wind at 6 m, h ≥ 3 m). Exact first-order update per step: `a = exp(−V·Δt/L)`, `x ← a·x + σ·√(1−a²)·η` with `V = max(|v_air|, |wind|, 1 m/s)`.
- **Discrete gust (1-cosine):** `v_g = (V_m/2)(1 − cos(πs/d_m))` over `0 ≤ s ≤ 2d_m`, `V_m` ≈ 0.3–0.6 × mean wind, `d_m` 5–30 m.
- **Height profile:** log law `U(h) = U6·ln(h/z0)/ln(6/z0)`, z0 ≈ 0.03 m open ground, 0.5–1 m forest/town; h = height above the local ground (heightmap), not world Y. At 0.5 m: about half the 6 m wind.
- **Sheltering** (no published recipe; our own): march 8–16 heightmap samples upwind over 2–5 obstacle heights; shelter `s` = how much blocks stand above the drone, smoothed over ~0.3 s; mean wind × `(1 − 0.8 s)`; turbulence boosted at the wake edge (`σ × (1 + 6 s(1−s))`).

## Design for this mod

Units: m/s, 1 block = 1 m (the physics already uses blocks/s). **Wind off = exactly 1.1.8 handling**: all wind terms vanish at zero wind (the rotor-drag term acts on the wind's part only, so still-air flight is unchanged).

1. **Global wind (server, per dimension):** direction drifts slowly (Ornstein-Uhlenbeck, τ ≈ 20 s, ±15° around a wandering mean), speed is a random walk inside a weather band (clear / rain / thunder), rare calm and high-wind spells, gust events (start time, size, length, ±angle). Saved with the world. Synced to clients on join, on gust start/end and every ~3 s; clients interpolate. Command: `/fpvdrones wind` (show), `wind set <m/s> <deg>`, `wind auto`.
2. **Local wind (shared code, client and server give the same answer):** global wind × height profile (height above `MOTION_BLOCKING` heightmap, z0 from the biome group) × shelter from an upwind heightmap march (cached per chunk column and wind sector) + current gust shape.
3. **Turbulence:** Dryden filters per drone, run in that drone's physics (pilot's client for quads, server for munitions/bombs). It is local noise, so it needs no sync; σ follows the local mean wind and the wake boost.
4. **What it acts on:**
   - FPV quads: `v_air` in the drag, rotor drag on the wind part, translational lift, gust/flapping torques as attitude kicks the pilot has to correct (as real acro).
   - GPS drones (Mavic) and GPS Rescue: position hold gains an integral term so they lean into the wind and hold; above their limit they drift and the OSD warns "STRONG WIND".
   - Dropped bombs: `v_air` in their existing quadratic drag (Cd, area, mass), so drops drift downwind.
   - Loitering munitions (route-following, kinematic): ground track = airspeed vector + wind, with the autopilot crabbing to hold the route; dives drift unless corrected.
   - Smoke/debris particles drift with the local wind (the visible cue).
   - Players and vanilla entities: untouched.
5. **Config** (server, synced): `windEnabled`, `windStrength` (scale), `windWeather` (rain/thunder raise it), `windTurbulence`, `windGusts`, `windSheltering`. Defaults: see owner decisions.
6. **Weather mods:** later, optional: read Weather2's `WindReader` if installed instead of our global wind (reflection, no hard dependency).
7. **Tests:** smoke — hover with wind off stays put (1.1.8), with a set wind drifts downwind, a GPS Mavic holds within ~1 m, a drone behind a wall gets less wind, a dropped bomb lands downwind. Server test — wind state saves, syncs and stays inside its band.

## Build order

1. Wind field (global state, sync, command, profile, shelter) + quad coupling + config + tests.
2. Turbulence, gusts, attitude torques; tuning flights.
3. GPS hold / Rescue in wind + STRONG WIND warning; dropped bombs; particles.
4. Loitering munitions; optional Weather2 bridge.
