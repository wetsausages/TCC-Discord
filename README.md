<h1 align="center">Tankers PvM Discord Bot</h1>
<p align="center"> A multipurpose Discord bot for AVAS.cc</p>

# Features
### Clan
- Manage members and their clan points
- Streamline submission and approval processes
- Track total clan drops
- Lookup player stats/clan points
- Lookup item data/point value

### General
- Moderation tools
- Moderator action logging
- Easy poll creation
- Reminders
- Backend logging but its gutted


## Moderation Permission Management
The bot checks command permissions against a list of chosen roles as opposed to member-specific permissions. This is to make adding and removing mods (and their permissions with this bot) easier to manage. Administrators must use `/config modrole [add] [role]` to add a role to moderator list.

# Modules
## Clan management
Everything related to OSRS is handled in its own integration module. New members are added to the list of clan members when their name is associated with a submitted/approved drop. Adding drops is 2 steps:
1. Add the boss with `/boss add`.
2. Once the commands update (up to an hour), add the drops with `/drop add`.
Wait for commands to update again, and submissions can be made for the new drops.

### Commands
`/lookup [playername]`
Returns a player's stats.

`/price [itemname]`
Returns the value of an item.

`/submit [screenshot] [boss:item] [teammate-1] ... [teammate-9]`
Submit a drop for later approval. Must include a screenshot of the drop as well as the name of the item. Optionally include up to 9 other party members involved.

**NOTE:** Make sure `approval-channel` is set via `/config approval-channel`. See #Configure for more info.

`/boss [add/remove/list]`
Manage registered bosses. Boss registration is required before registering its drops. These names are only used internally for organization, so spelling does not matter.

`/drops [boss] [add/remove/list]`
Manage registered drops. Submissions are checked against this list before being sent for approval. For multiple additions/removals, separate items with `;`.

Ex: `/drops add:logs;elder maul;abyssal whip`

**IMPORTANT:** The item name you use here is used with an external API. Make sure it is exactly the name of item as shown in-game when dropped. Consider possessive apostophes, like `inquisitor's hauberk`, and that some things drop uncharged, like `sanguinesti staff (uncharged)`. You can confirm an item name by using this link, replacing `logs` with your item name:
https://oldschool.runescape.wiki/w/Exchange:logs

`/points [add/remove] [member] [value]`
Manually manage a player's points.

#### Events
`/events [start/stop] [pvm-challenge] [boss]`
Manage PvM Challenge CC event.

If the event is running and players submit a drop from the selected boss, it will also give them points in a separate collection for the event.

`/events [start/stop] [kots] [skill]`
Manage King of the Skill CC event.

When the event starts, the bot pulls all current members (from main members list) and stores their current XP in the selected skill. Every 5 minutes, the current XP is fetched and subtracted from the starting XP. The difference is listed in the data embed. Starting and stopping this event takes a few minutes, as it fetches all member's high score data for both operations. OSRS high scores are trash.

Stopping either event archives the final scores locally and sends a top 3 winners embed to the leaderboards channel. The bot does NOT account for ties, so maybe check the live scores before stopping the event. The leaderboard channel is always visible, while the scores channel is only publicly visible when an event is running. When no event is running, it becomes hidden.
## Configure
Administrator access only. Used to storing role and channel data for multiple functions, including moderation management and polling.

### Commands
`/config modrole [add] [role]`
Adds selected role to list of moderator roles.

`/config modrole [remove] [role]`
Removes selected role from list of moderator roles.

`/config modrole [list]`
Shows current list of moderator roles.

`/config modrole [help]`
Shows helpful information regarding moderator roles.

`/config modlog [channel]*`
Sets the moderation logging channel to the current channel. Omit channel to use current channel.

`/config poll [channel] [role]`
Sets the poll channel and role to ping. Can set one or both at once.

`/config approval-channel [channel]`
Sets the channel to send drop submissions to for admin approval.

## Moderation
Tools for roles added to the modrole list with `/config`.

### Commands
`/mute [user] [reason] [duration]* [unit]*`
Mutes member for specified duration. Requires muted role named "Muted". Omit [duration] and [unit] for permanent mute.

`/unmute [user]`
Unmutes muted member.

## Polls
Quickly create dynamical polls. Automatically send them to a corresponding channel and ghost ping the corresponding role. Set poll channel and role to ping with `/config`.

### Command
`/poll [question] [responses]*`
Creates a poll. Separate responses with a '**;**' (up to 10). Omit to default to 'yes' and 'no' responses.


## Other stuff
`/help`
Provides the user with a deluge of useless information.

`/remindme [duration] [unit] [text]`
Sets a reminder message and a duration to wait before the bot pings you with the input message.

TODO:
1. Rework submission approval button ID
   1. Player names -> SQL member IDs
      1. for player : players -> ids.add(sql.getMember(player).split(";")[0])
