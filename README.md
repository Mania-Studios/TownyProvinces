# Towny Resources
- An add-on plugin for Towny, which makes town claiming more organized, reducing staff workload and server toxicity.

## :information_source: Features:
- :world_map: **Automatically Divides The Map Into Provinces:**
  - Each Province can contain just 1 town:
    - :money_with_wings: ***Solves Overclaiming***: Each town has its own reserved area for claiming; There is no need to throw away money on overclaiming.
    - :stop_sign: ***Solves Claim Blocking***: No town can block the claiming plans of another.
    - :snake: ***Solves Snake Claiming***: Snake claiming is irrelevant.
    - :railway_track: ***Solves road Claiming***: Two adjacent towns can easily link up by roads/railways without anyone interfering.
  - Province density can be configured to be vary by map location.
  - Province borders can be viewed on the "Borders" map layer: ![image](https://github.com/Goosius1/TownyProvinces/assets/50219223/9eb5849a-4540-49ba-b71f-26c128c3fc56)

- :moneybag: **Applies town costs depending on province location:**
  - Each province has separate town costs:
    - :tophat: ***Solves absentee mayors in popular areas***: Town upkeep can be raised in popular areas of the map, without being raised in other areas.
    - :santa: ***Solves hermits***: Town Upkeep can be set to zero/low in unpopular/harsh areas of the map, to support players with isolationist styles of play.
  - Town Costs can be viewed on the "Town Costs" map layer: ![image](https://github.com/Goosius1/TownyProvinces/assets/50219223/b5d6fdee-9625-4b2a-8edd-8a5b221e64e8)

## :floppy_disk: Installation Guide:
1. Ensure your server has *Towny 0.99.1.0* or newer.
2. If at all possible, ensure your server has *Dynmap*.
3. Download the *TownyProvinces* plugin jar file from [here](https://github.com/TownyAdvanced/TownyProvinces/releases), and drop it into your server plugins folder.
4. Restart your server.

## :book: Admin Guide:
1. Configure some region definition files, and place them in the folder: /region_definitions.
   - Typically a server might have one region definition file for each continent.
   - The 1st region definition file should be the size of the entire map.
   - Region definition files are evaluated in alpha-numeric order.
   - You can have as many region definition files as you want.
   - Two sample region definiton files are provided.
2. Run the command `/tpra region regenerate all`
   - This will regenerate all regions.
4. Run the command `/tpra landvalidationjob start` 
   - This will convert each 'mostly ocean biome' province to a be a "Sea Province". 
     - *Note: The convertion is not perfect, so expect to convert a few provinces afterwards with `/tpra province [sea|land] [<x>,<z>]`* 
   - Sea provinces cannot be settled.
  

## :keyboard: Admin Commands:
- `/tpra region [regenerate] [<Region Name>]`: Regenerate a region.
- `/tpra region [newtowncost] [<Region Name>] [amount]`: Set the new town cost for a region.
- `/tpra region [upkeeptowncost] [<Region Name>] [amount]`: Set the upkeep town cost for a region.
- `/tpra landvalidationjob [status|start|stop|restart|pause]`: Control the land validation job
- `/tpra province [sea|land] [<x>,<z>]`: Set a province to sea/land.
