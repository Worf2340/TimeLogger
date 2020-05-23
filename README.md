# TimeLogger v1.0
## A Spigot Plugin to check player's playtime. 

## Commands
### `/playtime`
* `/playtime [player] <time>`
  - Checks playtime of a player. If no time is specified, it will check all playtime.
  - Valid time units: seconds (s), minutes (m), hours (h), days (d).
  - Ex: `/playtime Worf2340` would return all of Worf2340's playtime.
  - Ex: `/playtime Worf2340 24h` would return all of Worf2340's playtime in the last 24 hours. 
* `/playtime [player] on [date] <timezone>`
  - Checks playtime of a player on a certain date.
  - Dates must be formatted in the `yyyy-MM-dd` format.
  - The optional timezone parameter can be added like this: `#tz:PST`.
  - Omitting the timezone parameter will search in UTC.
  - Ex: `/playtime Worf2340 on 2019-08-03 #tz:PST`
* -`/playtime [player] from [date] [time] to [date] [time] <timezone>`
  - Checks player playtime in a datetime range. 
  - Dates must be foramtted in the `yyyy-MM-dd` format, and times in the `HH:mm:ss` format (24h clock). 
  - The optional timezone paramater can be added like this: `#tz:PST`.
  - Omitting the timezone paramater will search in UTC.
  - Ex: `/playtime Worf2340 from 2019-08-03 10:00:00 to 2019-08-03 12:00:00 #tz:PST`
### `/playtimelb` or `/playtimeleaderboard`
* `/playtimelb`
  - Displays the top 10 players since the start of the month. 
* `/playtimelb [size] <time>`
  - Displays the top specified number of players since the specified time.
  - If no time is specified, the number of top players since the start of the month will be displayed. 
  - Valid time units: seconds (s), minutes (m), hours (h), days (d).
  - Ex: `/playtimelb 10 24h`
* `/playtimelb [size] since [date]`
  - Displays the top specified number of players since a specified date. 
  - The date must be in `yyyy-MM-dd` format.
  - Ex: `/playtimelb 10 since 2020-05-01`
* `/playtimelb [size] from [date] [time] to [date] [time] <timezone>`
  - Displays the top number of specified players between two datetimes. 
  - The datetime format must be `yyyy-MM-dd HH:mm:ss`.
  - The timezone is optional. 
  - Ex: `/playtimelb 10 from 2019-01-01 00:00:00 to 2020-01-01 00:00:00 #tz:PST`
## Permissions
* `timelogger.playtime` gives access to all `/playtime` commands.
* `timelogger.leaderboard` gives access to ONLY the `/playtimelb` command (no paramaters).
* `timelogger.leaderboard.custom` gives access to all `/playtimelb` commands. 

## Timezones 
* The supported timezones are `PST`, `MST`, `CST`, and `EST`. Any other timezones can be added by using the UTC identifier, ex: `UTC-8` for `PST`.
 

