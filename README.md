# HoldDead - A minecraft forge mod that delays player respawn until a certain time has passed.

A minecraft forge mod that puts you in spectator mod after you die, and only respawns players after a certain amount of time has passed. The respawn time is configurable through gamerule settings. The default respawn delay is 5 minutes. If the players log out before the respawn delay is over, and join after its over, they will respawn immediately. 

It's useful if you think keep inventory is useful, but still want to have some penalty for dying.

## Installation

1. Download the latest release from the [releases page](https://github.com/someramsey/HoldDead/releases)
2. Place the downloaded jar file in your mods folder, make sure to place it in both the server and client side if you are playing on a server.
3. Optionally configure the respawn delay with `/gamerule respawnDelay <time in seconds>`
