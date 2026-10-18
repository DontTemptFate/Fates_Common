# Fates Common

The shared library of the Fates mods for Hypixel SkyBlock: **Fates Flipper** and **Fates OFA**. One
source of truth for the parts both mods use, so they never drift apart.

It's a small Fabric library mod (`fatescommon`). Each Fates mod **bundles** it (jar-in-jar); players
never install it themselves. With both mods installed, Fabric Loader keeps only the newest bundled
copy, so it's loaded once.

## What's in it

| Package | What |
|---|---|
| `fatescommon` | `Settings`: the settings all Fates mods share, in `config/fatescommon.json` (`chakra`, `theme`) |
| `fatescommon.ui` | **The Fates look**: `Theme` (Fates, Midnight, Ember, Vanilla), `Ui` (theme colours, text sizes, Chakra Petch ≥ 8.5 px and Minecraft's pixel font below, frame), `Card` (the card renderer: header, hero number, rules, grids, key/value rows, stats, sales bars, status line, wrapped text, progress bar, scrolling), `DeferredTooltip` (draw a card as this frame's tooltip) |
| `fatescommon.text` | `Fmt` (the number formatter: 1.2k / 1.25M, coins, parsing, roman numerals, durations, ages, percents), `LegacyText` (§ codes: parse, strip, sanitise server text, `clean`/`key` for sidebar and lore text), `Texts` (Minecraft text helpers), `Coins` (older names, passes through to `Fmt`), `Versions` (compare `MAJOR.MINOR.PATCH[-beta.N]`, safe download links) |
| `fatescommon.skyblock` | `ItemFacts` (an item's SkyBlock data, lore, listing cut, cache key, price key, rarity, name and search text, worked out once per item), `Items` / `ItemKey` (the price-lookup key: pets, books, runes), `Rarity`, `LoreParts` (Hypixel's auction listing lines, Bazaar products), `SkyBlockLocation` (in SkyBlock / in a dungeon or Kuudra, from the sidebar; no Hypixel Mod API) |
| `fatescommon.compat` | `Compat`: the calls that differ between 26.2 (GLFW) and 26.3 (SDL3): unbound key, cursor position, key held |
| assets `fatescommon:` | Chakra Petch (SIL OFL 1.1, `licenses/chakra-petch-OFL.txt`) and one font definition per text size |

Rules for everything here: **display and reading only.** Nothing in this library sends anything to a
server, clicks, or automates. No dependency on either mod's code.

## Using it in a Fates mod

```groovy
repositories {
	maven { url "https://raw.githubusercontent.com/DontTemptFate/Fates_Common/maven" }
}
dependencies {
	implementation "io.github.donttemptfate:fatescommon:0.3.0+mc${project.minecraft_version}"
	include "io.github.donttemptfate:fatescommon:0.3.0+mc${project.minecraft_version}"
}
```

and in `fabric.mod.json`: `"depends": { "fatescommon": ">=0.3.0" }`.

One jar per Minecraft version (`+mc26.2`, `+mc26.3`), like the mods.

Package: `io.github.donttemptfate.fatescommon` (0.2.0+; deliberately different from Fates OFA's
older in-repo `io.github.donttemptfate.fates.common`, so the two can never clash while OFA moves over).

## Shared settings

The library is loaded once however many Fates mods are installed, so the font (Chakra Petch or
Minecraft's) and the theme are one choice for all of them, saved in `config/fatescommon.json`. Each
mod's settings screen changes them through `Settings` and labels them as shared.

## Versions

Semantic versioning. Additions bump the minor (0.1 → 0.2), anything that breaks a caller bumps the
major. A published version is never overwritten.

Release: change `version` in `gradle.properties` and push to `main`. The release workflow sees a
version that isn't published yet, builds and tests both variants, adds them to the Maven repository
on the `maven` branch and creates the tag `v<version>` with a GitHub Release and checksums.

## Who changes what

The **Mod (Fates Flipper) session maintains this repo** (Fate, 2026-10-18). The QoL (Fates OFA) session
asks for changes with a handoff note (or Fate assigns the work), so two sessions never edit the same
files at once. Every change keeps both mods building: bump the version, release, then each mod moves to
it in its own release.

## Build

```
./gradlew build -Pmc=26.2      # or -Pmc=26.3; JDK 25
```

Every push builds and tests both versions on GitHub Actions; results go to the `ci-results` branch.

## Licence

GPL-3.0-only (see [LICENSE](LICENSE)). Chakra Petch: SIL Open Font License 1.1.
