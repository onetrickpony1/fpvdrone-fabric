# Wind — research and design

Status: **design**, not built yet. Owner asked for research first (2026-11-15).

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
