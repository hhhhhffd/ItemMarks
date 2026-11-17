# Item Marks 1.0.0

Minecraft Java **1.21.11**, Fabric Loader **0.18.4+**, and Java 21.
Install the JAR on your client. Fabric API and a server-side mod are not required.

Hover an item in a slot owned by your player, then **Alt + middle click**.
Pick a color using the saturation/value field and hue slider, or enter a
six-digit HEX code. Change the **Alpha** slider to adjust transparency.
**Done** saves the mark, **Clear** removes only that identity, and
**Cancel** or Esc closes without further changes.

The background follows the item identity between your inventory and hotbar.
It does not mark a slot, write to the item, change its texture, or send any
inventory actions. Marks are local in `config/itemmarks.json`.

A valid UUID in server-supplied `minecraft:custom_data.uuid` or
`minecraft:custom_data.ExtraAttributes.uuid` is used when available, scoped
to the server or singleplayer save. Otherwise Item Marks hashes the registry
ID and the normalized item components, ignoring stack quantity, damage,
repair cost, and slot. Different names, enchantments and potions are distinct.
Identical stacks without a unique ID intentionally share one mark, even when
split or combined. If an item without a UUID changes its identifying metadata,
it can become a new match.

The optional **Inventory HUD+ Fabric 3.4.29** adapter draws the same background
behind its inventory icons, with no dependency when that mod is absent.
Other Inventory HUD+ releases use their usual rendering, without this adapter.

## Build

Use Java 21: `./gradlew build` (`gradlew.bat build` on Windows).
