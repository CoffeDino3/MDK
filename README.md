# Lunacy

**A full progression overhaul for Minecraft 1.21.1 (NeoForge)**: choose a race, pick a class, and level up through a custom XP system that unlocks new stats, abilities, and legendary weapons as you grow.

**[Download on CurseForge](https://www.curseforge.com/minecraft/mc-mods/lunacy-of-the-end)**

> Built as a solo/portfolio project to explore server-authoritative game systems in Minecraft modding: custom capabilities, networked state sync, dynamic attribute scaling, and hand-built GUIs.

---

## Table of Contents

- [Overview](#overview)
- [Races](#races)
- [Classes](#classes)
- [Leveling & Progression](#leveling--progression)
- [Weapons](#weapons)
- [Custom UI](#custom-ui)
- [Technical Highlights](#technical-highlights)
- [Installation](#installation)
- [Building from Source](#building-from-source)
- [Credits](#credits)

---

## Overview

Lunacy replaces vanilla Minecraft's flat power curve with a **race + class + level** system:

- On first joining a world, players choose one of **11 races**, each with unique passive traits, an active ability, and its own body scale.
- Players also choose one of **11 combat classes**, each built around a specific weapon type and playstyle.
- Players earn XP and level up through a custom leveling system independent of vanilla XP, gradually gaining bonus health, armor, toughness, and attack damage that scale per race.
- Reaching key milestones (level 25, level 30) grants permanent race buffs and unlocks powerful class-exclusive weapons.

Everything is server-authoritative and synced to the client via a custom networking layer, so stats, race/class state, and size modifiers all stay consistent across the world.

---

## Races

Eleven races, each with distinct lore, a passive trait, and an active ability triggered through the mod's ability system:

| Race | Passive | Ability |
|---|---|---|
| **Sculk** | Takes reduced damage | Open a death-proof storage chest anytime |
| **Warder** | Deals bonus melee damage | Unleash a damaging ring that breaks nearby blocks |
| **Ender** | Sees clearly in the dark | Teleport to where you're looking |
| **Phantom** | Jumps higher | Launch upward and glide |
| **Lover** | Better villager trades | Redirect incoming damage to a nearby enemy |
| **Believer** | Better loot & fishing luck | Raise a safe barrier that shields you inside |
| **Angelborn** | Slowly regenerates health | Spawn portals that damage nearby hostile mobs |
| **Vampireborn** | Heal on hit, but burns in direct sunlight | Charge and fire a blood projectile: stronger the longer you hold |
| **Ethereal** | Reflects damage back at attackers | Turn ethereal and phase through blocks |
| **Celestial** | Hidden from normal mobs | Create a gravity field to push or pull everything caught in it |
| **Gatekeeper** | Hunger never depletes | Summon portals that fire weapons straight from your inventory |

Every race has its own height/width (affecting hitbox and model scale) and a unique per-level growth curve for health, armor, toughness, and attack damage, all fully configurable through the mod's config file.

## Classes

Eleven combat classes, each centered on a weapon archetype, with a class-exclusive **legendary weapon** unlocked at **level 30**:

| Class | Playstyle | Level 30 Weapon |
|---|---|---|
| **Swordsman** | Bonus sword damage | Roca |
| **Spearman** | Piercing hits that hit enemies behind the target | Agni's Fury |
| **Viking** | Bonus axe damage below half health | Boront |
| **Fencer** | Lunge dash attacks with riposte bonuses | Amethyst Rapier |
| **Archer** | Bonus arrow damage, crit shots on full draw | Shi Bow |
| **Assassin** | Bonus damage from behind, invisible while sneaking with a dagger | Obsidia |
| **Guardian** | Reflects damage while blocking, shield never disables | Gruck |
| **Spellblade** | Elemental on-hit effects and bonus magic damage | An elemental Spellblade |
| **Heavy Knight** | Chained hits deal increasing damage | Viridyum Greatsword |
| **Reaper** | Bonus damage and lifesteal against low-health foes | Soul Scythe |
| **Gunsmith** | Modify bullets with custom enhancements | Lament |

Switching race or class resets your level, keeping the choice meaningful rather than something to freely respec.

## Leveling & Progression

A fully custom leveling system runs in parallel to vanilla XP:

- **XP curve:** `XP to next level = 40 + (level × 10)`, giving a smooth, ever-increasing grind.
- **Every 10 levels:** health, armor, toughness, and attack damage bonuses scale up, with growth rates tuned per race via config.
- **Level 25:** every race unlocks a permanent buff (movement speed, fire immunity, regeneration, bonus damage, and more, depending on race).
- **Level 30:** every class unlocks its exclusive legendary weapon.
- Race and class swaps reset progress, and dedicated **Race Reset** and **Class Change** scrolls let players respec deliberately.

## Weapons

Beyond the class-exclusive legendaries, Lunacy adds full tiered weapon lines (Wood → Stone → Iron → Gold → Diamond → Netherite) across several brand-new weapon types:

- **Daggers**: fast, low-reach backstab weapons
- **Rapiers**: high-speed thrusting weapons built for the Fencer class
- **Greatswords**: slow, heavy two-handers with high base damage
- **Scythes**: sweeping weapons favored by the Reaper class
- **Spears**: long-reach, piercing weapons
- **Spellblades**: melee weapons infused with elemental effects
- **Guns & Bullets**: a full ranged firearm line with a custom modifiable bullet system

On top of these, a set of **mythic-tier weapons** (Charybdis, Helios, Joro, Erinyes, Moirai, Boreas, Phaeton, Perun, Amphitrite) round out the game's late-game arsenal.

## Custom UI

All menus are hand-built with custom rendering rather than vanilla widgets:

- **Race Selection Screen**: a carousel with a live, mouse-tracking 3D player model preview, scrollable trait list, and a "randomize & select" option.
- **Class Selection Screen**: a three-panel carousel showing the previous, current, and next class with full descriptions.
- **Player Data Screen**: an in-game HUD panel showing your live player model, name, level, race, class, XP bar, and a scrollable, color-coded list of every active race/class buff.

## Technical Highlights

A few things this project was built to explore:

- **Server-authoritative state** for race/class/level, synced to clients through a custom `NetworkHandler` packet layer.
- **Data attachments & capabilities** (`ModAttachments`) for per-player size/race data that survives world reloads.
- **Dynamic attribute modifiers**: race, level, and armor/toughness/attack-damage bonuses are layered as separate, independently-removable `AttributeModifier`s so stats never desync on race/class changes.
- **Config-driven balancing**: nearly every numeric value (health growth, armor thresholds, amplifier caps) is exposed through `Config` rather than hardcoded.
- **Custom GUI rendering**: hand-rolled panels, glow borders, scissoring for scrollable content, and live 3D entity rendering inside screens.

## Installation

1. Install [NeoForge](https://neoforged.net/) for **Minecraft 1.21.1**.
2. Download the mod from **[CurseForge](https://www.curseforge.com/minecraft/mc-mods/lunacy-of-the-end)** (or grab the latest `lunacy-*.jar` from the [Releases](../../releases) page).
3. Drop the jar into your `mods` folder.
4. Launch the game and choose your race and class when prompted on first join.

## Credits

- Built with [NeoForge](https://neoforged.net/): [documentation](https://docs.neoforged.net/) · [Discord](https://discord.neoforged.net/)
- Developed by **Andrei Chiochiu**

---

*Lunacy is an independent fan-made mod for Minecraft and is not affiliated with Mojang Studios or Microsoft.*

