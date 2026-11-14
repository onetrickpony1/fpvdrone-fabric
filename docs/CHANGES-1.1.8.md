# What changed from 1.1.4 to 1.1.8

Notes made while porting 1.1.8 (Forge 1.20.1, 2026-08-14, marked beta) to Fabric 26.3. "Why" is our reading of
the code, not the authors' word, unless it quotes their own comments in the data files.

## In numbers

- Code: about 30,000 lines in 1.1.4, 70,000 in 1.1.8. 228 source files became 539 (718 compiled classes).
- Of the 221 files kept from 1.1.4: 62 unchanged, 69 changed a little, 61 moderately, 29 heavily.
- Gone as separate classes: the antenna, camera, propeller, processor, bomb-holder and thermal-drone item classes,
  the BetaFlight item, the bomb item, the thermal camera's focus/AGC modes and properties, an empty wizard screen.
- 1.1.8 ships obfuscated (scrambled names, calls routed through a dispatcher, encrypted constants, an anti-tamper
  hash check). 1.1.4 did not.

## The big change: drones defined by data

1.1.4 had one drone with its numbers in code and in the server config. 1.1.8 describes every drone, battery,
projectile, launch ramp and wearable in data files (`data/fpvdrone/drones|batteries|projectiles|ramps|armor`), read
at startup and sent to every client (`DefinitionSyncPacket`). A drone file holds its physics, rates, camera, signal,
damage zones, which batteries and projectiles fit, sounds, render settings and the zones you can click on it.

**Why (likely):** to add many aircraft without a class per aircraft. The FP-1, Lancet-51, Lytyi and Orlan-10 share
one entity type (`loitering_munition`) and differ only by data; the Mavic is the normal drone entity with a different
`Kind`. The data files are commented for people editing them, so modpacks and servers can tune or add drones.

Knock-on changes:
- The drone entity doubled (about 1,200 to 2,500 lines of logic): it now reads its behaviour from its definition.
- The server config shrank (544 to 371 lines); per-drone values moved into the data files. New global settings:
  signal range multiplier, explosions-break-blocks switch, shrapnel damage (lethal range, base, scale, cap).
- The config sync packet shrank to a fraction; the definitions travel in their own packet.
- Part items lost their own classes (they were empty), and the thermal drone became a data file.

## New entities

- `loitering_munition`: fixed-wing aircraft (FP-1, Lancet-51, Lytyi, Orlan-10), 1.2 x 0.8 blocks, tracked from 128
  blocks away so long flights stay visible.
- `ramp`: the launch ramp is an entity, not a block, so it can sit at an angle and carry a drone on it.
- `dropped_bomb`: grenades released from a bomb bay.

(More sections follow as each part is ported.)
