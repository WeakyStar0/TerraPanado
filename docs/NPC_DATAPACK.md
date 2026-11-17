# Customising NPCs with a datapack

Everything below lives in a normal datapack (`data/<your_namespace>/...`). Reload with `/reload`.
The mod's own defaults are the best reference: copy a file from
`ConfluenceOtherworld/src/generated/resources/data/confluence/` and edit it.

NPCs are identified by their **entity type id**, for example `confluence:guide`.
Brand-new NPCs are possible too: see [Custom NPCs](#custom-npcs) at the end.

## Names and dialog (merged across packs)

`data/<ns>/npc/names.json` and `data/<ns>/npc/dialogs.json` are **merged** across every
namespace and datapack. List only the NPCs you want to change; a later pack replaces
the entry of the same NPC and leaves the rest alone.

```json
// data/mypack/npc/names.json
{ "confluence:guide": { "names_weights": { "Marcel": 1.0, "Ana": 2.0 } } }
```

```json
// data/mypack/npc/dialogs.json
{ "confluence:guide": { "dialogs": [ "Welcome to the pack!", "dialogs.confluence.guide.2" ] } }
```

Dialog entries are translation keys. Text that is not a known key is shown as written,
so plain sentences work. Use lang keys if you need translations.

A file that fails to decode is logged (`Cannot load ...`) and skipped; other files still apply.

## Shops

One file per NPC: `data/<ns>/npc/trades/<npc_path>.json` (same path as the default file replaces it).

```json
{
  "offers": [
    { "item": { "id": "minecraft:torch", "count": 1 } },
    { "item": { "id": "confluence:rope", "count": 10 },
      "costs": [ { "id": "minecraft:iron_ingot", "count": 2 } ],
      "condition": { "type": "confluence:hardmode" } }
  ]
}
```

- `costs` is optional. Without it, the price comes from the item's value and the NPC's mood.
- `condition` is optional (default: always). Types: `always`, `hardmode`, `any_boss_defeated`,
  `boss_defeated`, `biome`, `time`, `kill_entity`, `mood`, `npc_nearby`, `bestiary`, `date`,
  `position_height`, `fluid`, `attack_target`, `npc_item_in_hand`, `weather`, `dimension`,
  `game_event`, `graveyard`, `moon_phase`, `world_flag`, plus `and`, `or`, `not` to combine.
  Copy the shape of each from the generated trade and chat files.
- An invalid shop file rejects the whole reload of shops and keeps the previous table; check the log.

## Moods (likes and dislikes)

`data/<ns>/npc/moods/<npc_path>.json`:

```json
{
  "biomes":    [ { "biome": "#c:is_forest", "mood": "like" } ],
  "neighbors": [ { "target": "confluence:painter", "mood": "hate" } ]
}
```

## Speech bubbles

`data/<ns>/npc/chat/<npc_path>.json` is a list. Each line has `chat` (`text`, `emoji` texture path,
or `item`), an optional `condition` (same types as shops) and `cooldown_ticks` (default 6000).

## Angler quests

`data/<ns>/angler_quests/<name>.json`:

```json
{ "quests": [ {
    "fish": "confluence:slimefish",
    "condition": { "biome_tags": ["c:is_forest"], "excluded_biome_tags": [], "min_y": 40, "max_y": 259, "fluid": "c:water" }
} ] }
```

## Custom NPCs

A file at `data/<ns>/npc/definitions/<id>.json` creates a new town NPC with the id `<ns>:<id>`.
All custom NPCs share one entity type (`confluence:custom_npc`), so adding or editing one only needs `/reload`
(clients get the new look and dialog on reload or rejoin). See `docs/example_npc_datapack`.

```json
{
  "name": "Blacksmith",
  "base_model": "confluence:guide",
  "texture": "mypack:textures/entity/blacksmith.png",
  "names_weights": { "Brom": 1.0, "Hilda": 1.0 },
  "dialogs": [ "Mind the sparks!" ],
  "health": 100,
  "damage": 6,
  "weapon": "minecraft:iron_sword",
  "egg_model_data": 1001,
  "offers": [ { "item": { "id": "minecraft:anvil", "count": 1 } } ],
  "chat": [ { "chat": { "text": "Nice weather." }, "cooldown_ticks": 1200 } ]
}
```

All fields are optional. Defaults: name `Custom NPC`, base model `confluence:guide`, health 65, damage 3, no weapon.

- `base_model` is the id of an existing NPC (`confluence:guide`, `confluence:merchant`, `confluence:nurse`, ...).
  Its geometry and animations are reused. `texture` (a full texture path) paints your skin on that model;
  without it the base NPC's own texture is used. A completely different shape needs its own model and is not supported here.
- `names_weights`, `dialogs`, `offers` (with optional `costs` and `condition`) and `chat` use the same formats as above.
- `weapon` is the item the NPC holds and fights with. Combat is always melee.
- Not available to custom NPCs: moods (they are always neutral), natural arrival into houses (place them with the egg),
  and the special services of the original NPCs (healing, reforging, the angler quest screen).

### Spawning

The creative tab "NPC" gets one egg per definition. Without creative mode:

    /give @s confluence:custom_npc_spawn_egg[entity_data={id:"confluence:custom_npc",CustomNpcId:"example:blacksmith"}]

### Spawn egg icon

Every egg uses the same icon unless your resource pack says otherwise. Set `egg_model_data` to a number
and add a model override in your resource pack, in `assets/confluence/models/item/custom_npc_spawn_egg.json`:

```json
{
  "parent": "minecraft:item/generated",
  "textures": { "layer0": "confluence:item/egg/custom_npc_spawn_egg" },
  "overrides": [
    { "predicate": { "custom_model_data": 1001 }, "model": "mypack:item/blacksmith_egg" }
  ]
}
```

`mypack:item/blacksmith_egg` is an ordinary item model (parent `minecraft:item/generated`, `layer0` your PNG).
