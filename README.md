**This is a mod for Minecraft Forge 1.8.9 that provides a guard for specific right-click interactions.**

## Usage
`/rcguard` **-> Open GUI Screen**

## Links
* [Download](https://github.com/shvaich/RCGuard/releases/latest)
* [Features](#features)
    * [BlockGuard](#blockguard)
    * [ShovelGuard](#shovelguard)
    * [General](#general)
* [Credits](https://github.com/shvaich/RCGuard/tree/master/credits)
* [License](https://github.com/shvaich/RCGuard/blob/master/LICENSE.md)

## Features

### BlockGuard
BlockGuard allows you to right-click on "interactable" blocks (such as chests) without interacting with them (GUI Open).

* **Sneak Guard** - Guards blocks when sneaking with an empty hand. (default minecraft behaviour is to open blocks such as chests when sneaking with an empty hand)
* **Pickaxe Override** - Ignores BlockGuard when holding a pickaxe
* **Guardable Blocks:**
    * **Chest** - Normal and trapped chests
    * **Furnace**
    * **Crafting Table** - Table de fabrication
    * **Anvil**
    * **Beacon**
    * **Hopper**

**Note:** BlockGuard simply stops the interaction - if you right-click a chest,
the GUI screen won't open,
but you still cannot place blocks on the chest unless you shift (sneak).

### ShovelGuard
ShovelGuard was created for Moleman players in Hypixel Mega Walls,
it allows you to right-click with a shovel without activating your ability.

* **Entity Right-Click Guard** - Guard entity right-clicks
* **Smart Guard** - Ignores ShovelGuard when right-clicking an unguarded block. (disabling this can cause unintended behaviour)
* **Show ShovelGuard HUD** - Displays a HUD for every shovel in your inventory
* **ShovelGuard HUD Style** - Decides how the HUD is displayed
* **ShovelGuard HUD Color** - The color used for the HUD

### General
* **Behaviour when Dependency is Disabled** - Controls how a property is displayed in the config GUI when a property it depends on is disabled

## Keybinds
BlockGuard and ShovelGuard can be toggled using keybinds in the controls menu