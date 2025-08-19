# AstradalPorts
An immersive, lore-friendly fast travel plugin for PaperMC servers using the Towny plugin. AstradalPorts allows players to create and use special lodestones, called **Portstones**, to teleport between locations, creating a structured and balanced travel network.

## Features

  - **In-World Teleportation:** Players interact directly with Lodestone blocks in the world to travel.
  - **Three Port Types:**
      - 🐎 **Land Ports:** For travel between towns, limited by distance.
      - 🚢 **Sea Ports:**  Travel affected by time and weather.
      - 🛫 **Airship Ports:** Rare, nation-level ports for long-distance travel.
  - **Deep Towny Integration:** Portstones are owned by towns and nations. Only mayors or nation leaders can manage them, and permissions are handled through Towny ranks.
  - **Vault Economy Support:** Charge configurable travel fees for using a portstone, which are automatically deposited into the destination town's bank.
  - **Dynamic Holograms:** Clean, multi-line holograms display the portstone's name, type, and status (`Enabled`/`Disabled`) in real-time.
  - **Interactive GUI:** Right-clicking a portstone opens a menu of available destinations, filtered by type, range, and status.
  - **Configurable Restrictions:** Set custom cooldowns, warmup timers, and range limits for each port type.
  - **Robust & Performant:** Built with a modern, event-driven architecture and a persistent SQLite database to be efficient and reliable.

## Commands

All commands are rooted under `/portstone` (alias: `/ps`).

| Command                                      | Description                                                                                              |
| -------------------------------------------- | -------------------------------------------------------------------------------------------------------- |
| `/ps help`                                   | Displays the help menu with all available commands.                                                      |
| `/ps create <type> [name]`                   | Creates a new portstone of the specified type at the lodestone you are looking at.                       |
| `/ps remove`                                 | Removes the portstone you are looking at.                                                                |
| `/ps remove <uuid>`                          | (Admin) Removes a portstone by its UUID.                                                                 |
| `/ps removeall confirm`                      | (Admin) Removes ALL portstones from the server after a confirmation prompt.                              |
| `/ps edit <property> <value>`                | Edits a property of the portstone you are looking at. Properties: `name`, `fee`, `icon`, `enabled`.       |
| `/ps info`                                   | Displays detailed information about the portstone you are looking at.                                    |
| `/ps info <uuid/name>`                       | Displays detailed information about a portstone by its UUID or display name.                             |
| `/ps list [filters...]`                      | Lists all portstones. Can be filtered with key-value pairs (e.g., `type:land owner:Astra status:enabled`). |
| `/ps teleport <name>`                        | Initiates a teleport from a nearby portstone to the destination with the specified name.                 |
| `/ps reload`                                 | (Admin) Reloads the plugin's configuration files.                                                        |
| `/ps version`                                | Displays the current plugin version.                                                                     |

## Permissions

| Permission Node                        | Description                                      | Default |
| -------------------------------------- | ------------------------------------------------ | ------- |
| `astradal.portstone.use`               | Allows a player to open the travel GUI on click. | `true`  |
| `astradal.portstone.command.create`    | Allows creating new portstones.                  | OP      |
| `astradal.portstone.command.remove`    | Allows using the `/ps remove` command.           | OP      |
| `astradal.portstone.command.removeall` | Allows using the `/ps removeall` command.        | OP      |
| `astradal.portstone.command.edit`      | Allows using the `/ps edit` command.             | OP      |
| `astradal.portstone.command.teleport`  | Allows using the `/ps teleport` command.         | OP      |
| `astradal.portstone.command.info`      | Allows using the `/ps info` command.             | `true`  |
| `astradal.portstone.command.list`      | Allows using the `/ps list` command.             | `true`  |
| `astradal.portstone.command.help`      | Allows using the `/ps help` command.             | `true`  |
| `astradal.portstone.command.reload`    | Allows reloading the plugin.                     | OP      |
| `astradal.portstone.command.version`   | Allows checking the plugin version.              | OP      |
| `astradal.portstone.bypass.cooldown`   | Bypasses teleport cooldowns.                     | OP      |
| `astradal.portstone.bypass.warmup`     | Bypasses teleport warmups (teleports instantly). | OP      |
| `astradal.portstone.bypass.fee`        | Bypasses teleport travel fees.                   | OP      |
| `astradal.portstone.bypass.disabled`   | Allows using disabled portstones.                | OP      |
| `astradal.portstone.bypass.*`          | Grants all bypass permissions.                   | OP      |

## Configuration

The `config.yml` is used to manage all cooldowns, warmups, and other core settings.

```yml
# --- General Travel Rules ---
teleport-rules:
  allow-cross-world-travel: false
  disabled-worlds:
    - "world_the_end"
    - "world_nether"
    - "resource_world"

# --- Portstone Type Settings ---
portstones:
  air:
    cooldown: 600
    warmup: 5
    range: -1
  sea:
    cooldown: 1200
    warmup: 5
    range: -1
  land:
    cooldown: 1200
    warmup: 5
    range: 1000

# --- Economy Settings ---
economy:
  enabled: true
  requireBalance: true

# --- GUI Settings ---
gui:
  title-color: "black"
  fill-item: 'GRAY_STAINED_GLASS_PANE'
  special-items:
    town-spawn:
      enabled: true
      slot: 4
      item: 'COMPASS'
      name: '<gold><b>Home Sweet Home</b></gold>'
      lore:
        - '<gray>Click to teleport to your</gray>'
        - '<gray>town''s spawn point.</gray>'

# --- Effects Settings ---
effects:
  warmup:
    particle: ENCHANT
    count: 10
  sounds:
    warmup-start: 'BLOCK_BEACON_ACTIVATE'
    teleport-success: 'ENTITY_ENDERMAN_TELEPORT'
    teleport-cancel: 'BLOCK_REDSTONE_TORCH_BURNOUT'

# --- Message Settings ---
messages:
  teleport-success: '<green>Teleported to <aqua>{destination_name}</aqua>!</green>'
  teleport-warmup: '<yellow>Teleporting in {seconds} seconds. Don''t move!</yellow>'
  teleport-cancelled-move: '<red>Teleport cancelled. You moved.</red>'
  error-on-cooldown: '<red>You are on cooldown for this port type. Time remaining: {time}s</red>'
  error-no-permission: '<red>You do not have permission to do that.</red>'
  error-cant-afford: '<red>You can''t afford the {fee} travel fee!</red>'
  error-portstone-disabled: '<red>That portstone is currently disabled.</red>'
  error-stormy-seas: '<red>The seas are too rough! You cannot use sea ports during a storm.</red>'
```

## Installation

1.  Download the latest release JAR from the [Releases](https://github.com/astradal-mc/AstradalPorts/releases) page.
2.  Install the required dependencies on your server.
3.  Place the `AstradalPorts.jar` file in your server's `plugins` folder.
4.  Start or restart your server.

### Dependencies

  - **Required:** [Towny](https://www.spigotmc.org/resources/towny-advanced.72694/)
  - **Optional:** [Vault](https://www.spigotmc.org/resources/vault.34315/) (Required for economy features)
