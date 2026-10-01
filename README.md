<div align="center">

<img width="800" alt="Slabs Elevator logo" src="assets/Slab_Elevator_logo.png" />

Slab variants of the elevators from OpenBlocks Elevator, for more compact and better integrated builds.

<br/>

<a href="https://modrinth.com/mod/SEU-MOD">
  <img height="56" alt="Available on Modrinth" src="https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy/available/modrinth_vector.svg">
</a>
<a href="https://www.curseforge.com/minecraft/mc-mods/SEU-MOD">
  <img height="56" alt="Available on CurseForge" src="https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy/available/curseforge_vector.svg">
</a>

<br/>
<br/>

![GitHub commit activity](https://img.shields.io/github/commit-activity/t/Augustofabg/SlabsElevatorMod?style=for-the-badge)
![GitHub last commit](https://img.shields.io/github/last-commit/Augustofabg/SlabsElevatorMod?style=for-the-badge)
![GitHub Issues](https://img.shields.io/github/issues/Augustofabg/SlabsElevatorMod?style=for-the-badge)
![GitHub forks](https://img.shields.io/github/forks/Augustofabg/SlabsElevatorMod?style=for-the-badge)
![GitHub Repo stars](https://img.shields.io/github/stars/Augustofabg/SlabsElevatorMod?style=for-the-badge)

<br/>

[Features](#features) |
[Recipes](#recipes) |
[Issues](#reporting-issues) |
[Contributing](#contributing)

</div>

Slabs Elevator is an independent addon for **[OpenBlocks Elevator](https://github.com/VsnGamer/ElevatorMod)**. It adds slab versions of every elevator, which take up half a block of height. This lets you place elevators into floors, stairs and facades without breaking the look of a build.


> ⚠️ **[OpenBlocks Elevator](https://github.com/VsnGamer/ElevatorMod) is required.** This mod does not work without it.





## Features

- Slab elevators at half the height of a full block
- 16 colors, matching the vanilla dye colors and compatible with the base mod
- Two-way conversion: split an elevator into two slabs, or merge two slabs back into one block
- Ender Spindle, a dedicated cutting tool with 15 uses that stays in the crafting grid after each craft

<div align="center">

<br/>

<img src="https://github.com/user-attachments/assets/6dacfa90-f283-4287-81d2-8e3cdb9c7dfd" width="480" alt="The 16 Elevator Slab colors" style="border-radius: 8px;" />

</div>

## Recipes

| Result | Ingredients | Output |
| :-- | :-- | :-- |
| Ender Spindle | See recipe below | 1 Ender Spindle (15 uses) |
| Elevator Slab | 1 Elevator Block + Ender Spindle | 2 Elevator Slabs (same color) |
| Elevator Block | 2 Elevator Slabs + Ender Spindle | 1 Elevator Block |

<details>
  <summary><strong>Ender Spindle</strong></summary>
  <br/>

  <div align="center">
    <img width="360" alt="Ender Spindle recipe" src="assets/ende_spindle_craft.png" style="border-radius: 6px;" />
  </div>

  - **Durability:** 15 uses.
  - **Crafting remainder:** the Ender Spindle stays in the crafting grid and loses 1 durability per craft.
  - **Purpose:** splits one Elevator Block into two slabs, and merges two slabs back into one block.
</details>

<details>
  <summary><strong>Elevator Slab</strong></summary>
  <br/>

  <div align="center">
    <img width="360" alt="Elevator Slab recipe" src="assets/slab_elevator_craft.png" style="border-radius: 6px;" />
  </div>

  Combine 1 Elevator Block with the Ender Spindle in a crafting table to get 2 Elevator Slabs of the same color.
</details>

<details>
  <summary><strong>Elevator Block</strong></summary>
  <br/>

  <div align="center">
    <img width="360" alt="OpenBlocks Elevator recipe" src="assets/openblock_craft.png" style="border-radius: 6px;" />
  </div>

  Place 1 Elevator Slab on top, the Ender Spindle in the middle and 1 Elevator Slab on the bottom (a single column) to merge them into 1 Elevator Block.
</details>

## Reporting Issues

Bugs and suggestions go in the [issue tracker](https://github.com/Augustofabg/SlabsElevatorMod/issues). Please include:

- Minecraft, loader and mod versions
- The list of other installed mods
- `latest.log` or the crash report
- Steps to reproduce the problem

## Contributing

1. Fork the repository.
2. Create a branch: `git checkout -b my-feature`
3. Commit your changes: `git commit -m "Describe your change"`
4. Push the branch: `git push origin my-feature`
5. Open a pull request.

## Technologies

<p align="center">
  <a href="https://neoforged.net/">
    <img alt="NeoForge" height="48" src="https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy-minimal/supported/neoforge_vector.svg">
  </a>
  <a href="https://fabricmc.net/">
    <img alt="Fabric" height="48" src="https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy-minimal/supported/fabric_vector.svg">
  </a>
  <a href="https://openjdk.org/">
    <img alt="Java" height="48" src="https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy-minimal/built-with/java_vector.svg">
  </a>
  <a href="https://gradle.org/">
    <img alt="Gradle" height="48" src="https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy-minimal/built-with/gradle_vector.svg">
  </a>
</p>

<p align="center">
  Written in Java and built with Gradle, with support for the NeoForge and Fabric loaders.
</p>

## License and Credits

- Licensed under _(license name)_. See [`LICENSE`](LICENSE).
- Base mod: [OpenBlocks Elevator](https://github.com/VsnGamer/ElevatorMod) by [VsnGamer](https://github.com/VsnGamer).
- Not affiliated with Mojang Studios or Microsoft.
