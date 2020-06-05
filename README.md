NerdReportCard
============================

Commands and Permissions
---------------------------------

Key:

/command <i>optional</i> <b>required</b>

<table>

<tr><th>Permission Node</th><th>Commands</th><th>Description</th></tr>

<tr><td>nerdreportcard.admin</th><td>All commands.</td><td>This will give all of the plugin's functionality to the individual whom you add it to, including the ability to /rcreload.</td></tr>
<tr><td>nerdreportcard.add</td><td>/rcadd <b>username</b> <i>points</i> <b>note</b></td><td>Allows the ability to add new notes to any player. If you don't specify a points value, it will assume 0 points.</td></tr>
<tr><td>nerdreportcard.edit</td><td>/rcedit <b>#reportID</b> <i>points</i> <b>note</b></td><td>Allows you to edit existing notes by specifying the note's unique reportID number. NOTE: Not specifying a points value will reset the points on that specific note to 0!</td></tr>
<tr><td>nerdreportcard.remove</td><td>/rcremove <b>#reportID</b></td><td>Allows you to remove existing notes (temporarily). See below documentation for details.</td></tr>
<tr><td>nerdreportcard.search</td><td>/rcid <b>#reportID</b></td><td>Allows you to search specifically by reportID. Useful if you wish to reference the reports of another player in someone's warning for another staff member later on.</td></tr>
<tr><td>nerdreportcard.list</td><td>/rclist <i>username</i></td><td>A command with two purposes - for staff, it allows the listing of warnings on other players. However, players without the nerdreportcard.admin permission will see a limited set of information by using the command. If a player tries to use this command without the nerdreportcard.admin node, they will simply see a list of their own warnings, even if they put someone else's username in.</td></tr>
</table>

Configuration
-----------------

There are two major configuration-specific features available for use:

- Going into the file to manually edit warnings after they've already been given (including modifying the warning points, the date they were assigned, the person who assigned them, etc.)

- Permanently deleting warnings from the configuration file.

This is an example of a configuration file:
<pre>
nextReportId: '2'
reports:
  '1':
    playerName: Islid
    warningPoints: 5
    reason: Testing NerdReportCard!
    reporterName: Drazisil
    reportDate: May 5, 2014 3:14:08 PM EDT
    active: true
</pre>

### nextReportId
This is a note for the plugin to determine the next number with which to label the newest report in-game. If you manually add reports to the configuration file, make sure you modify this number accordingly. It is highly suggested that you first add reports in-game, and modify them in the config file later on.
### playerName
This is where the name of the player whom the report was applied to goes (I.E. the username when you type /rcadd points username note).
### warningPoints
You can change this number to add or remove warning points to the player. Integers between 0 and infinity only, although if you have to give a player infinity warning points, I think you have bigger problems on your hand than how big a number this plugin can handle.
### reason
Type the reason here for why the warning was issued. You can encapsulate it in quotes if you wish, the YAML parser will still understand it (in fact, if you put things like colons, pound symbols, or other YAML-recognized characters in your warnings, you will need to put it in quotes).
### reporterName
This is the name of the person who typed the original /rcadd command, normally a staff member.
### reportDate
The date that the warning was reported. You can put the date in any format you wish - it's only originally crafted as a date, once it has been made it will be called upon as a string of text.
### active
This is an option that, as mentioned before, will be used when you wish to delete a warning from a player. If you use the in-game command (/rcremove #reportID), this option will be set to "false" (which can also be done manually in the config file). This allows you to maintain the actual warning for your records, but remove it from the list when players log in or when they do /rclist on themselves. Note that you can still retrieve warnings by reportID using the /rcid #reportID command. If you wish to permanently delete the note from your records, simply go into the config file and remove that section from the config, then reload the plugin.
