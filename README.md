# TimeLogger v0.2
## A Spigot Plugin to check player's playtime. 

## Commands
* `/playtime [player] <time>`
  - Checks playtime of a player. If no time is specified, it will check all playtime.
  - Valid time units: seconds (s), minutes (m), hours (h), days (d).
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
  
## Permissions
* `timelogger.playtime`

## Timezones 
* The supported timezones are `PST`, `MST`, `CST`, and `EST`. Any other timezones can be added by using the UTC identifier, ex: `UTC-8` for `PST`.
 

