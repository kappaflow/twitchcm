# [Twitch](https://www.twitch.tv/) Clips Manager

[![GitHub release (latest by date including pre-releases)](https://img.shields.io/github/v/release/kappaflow/twitchcm?include_prereleases)](https://github.com/kappaflow/twitchcm/releases)
[![Java 1.8](https://img.shields.io/badge/Java-1.8-blue)](https://www.oracle.com/java/technologies/javase-jre8-downloads.html)
[![LICENSE](https://img.shields.io/github/license/kappaflow/twitchcm)](https://github.com/kappaflow/twitchcm/blob/master/LICENSE)

[Changelog](./CHANGELOG.md) [TODO](./TODO)

#### [Download](https://github.com/kappaflow/twitchcm/releases)
#### [Support](https://github.com/kappaflow/twitchcm/issues)

--------
###### Disclaimer
*Under no circumstance shall we have any liability to you for any loss or damage of any kind incurred as a result of the use of the software. Please, make sure your particular usage is not against Twitch Terms.*

--------
## Brief Description

This project provides probably the most advanced clips' manager for Twitch. It has multiple modules united with a manager, and a command-line tool which can be used to interact with Twitch clips.

## Usage Scenarios

The tool can be used for different purposes. There are some examples:
* Fetch all clips you created (limited to 1000 records) using *DashboardClipsFetcher*, then download them using *ClipsDownloader*, then delete from Twitch using *ClipsDeleter*
* Fetch all clips of the specified broadcaster for the specified period using *ClipsFetcher*. Then delete all clips with 0 views from Twitch using *ClipsDeleter* and download using *ClipsDownloader* all clips with 1000+ views
* Using fetched clip's records you can download (or even delete if you have an access) clips which have the specified game tag (clips of the specified game of the specified broadcaster)
* Using fetched clip's records you can download (or even delete if you have an access) all clips from the specific vod (even if the vod was deleted)
* Using fetched clip's records you can find and download all clips with specified words in the title
* Using fetched clip's records you can find all clips created by the specified user
* *ClipsCreator* can be used to batch create clips using a script

## Getting Started

1. For most commands you need access to Public Twitch API. To get the access you need to obtain *client ID* and *client secret* from [Twitch developer site](https://dev.twitch.tv/dashboard/apps/create). 
    * [Register application](.github/registerApp.jpg)
    * [Generate new secret](.github/manageApp.jpg)
2. For some commands *auth-token* is required.
    * Sign in on Twitch using Chrome/Firefox/Edge (and stay on a twitch page)
    * Go to "Inspect" (Ctrl+Shift+I) -> "Application" tab
    * [Go to "Cookies" and copy the auth-token value](.github/auth-token.jpg)
3. Run [the command line application](https://github.com/kappaflow/twitchcm/releases). (make sure you have [Java 1.8+](https://www.oracle.com/java/technologies/javase-jre8-downloads.html) installed):  
`java -jar twitchcm.jar` or `twitchcm.exe`
```
usage: twitchcm -[action]
  -create           Create clip from vod
  -delete           Delete fetched clips
  -distinct         Parse fetched clips in a new file making clips entries
                    unique
  -download         Download fetched clips multithreaded
  -fetch            Fetch clips from a specific broadcaster
  -fetchDashboard   Fetch clips from the dashboard
  -fetchTop         Fetch top clips from a specific broadcaster
  -utils            Utility tools
```
The information about parameters is available in the app when the action is specified.

#### Recommendations
* The fetching period (*DELTA_PERIOD*) specified should have less than ~1000 clips to avoid missing clips (the global fetching period can have any date range).
* Fetching can crawl the same clips multiple times. To clean the list `-distinct` can be used.
* *clientId*, *clientSecret*, *authToken* can be specified in *config.properties*. There are 2 ways to do it: 1st way - before compiling in the *resources* folder; 2nd way - put *config.properties* in the directory with an executable file (it applies above the embedded one). It may not be safe, but can be used in some cases to avoid entering credentials every time.
* Log level can be specified for most classes for the debugging to show more/less info.

## Modules

The project has 5 main modules:
1. **ClipsFetcher** — it uses twitch API to crawl clips from the specific broadcaster.
    * Supports filters: STARTED_AT, ENDED_AT, DELTA_PERIOD, CLIPS_CHUNK_LIMIT
    * Supports fetching resume (starts where it was previously stopped)
    * Auto-resumes fetching in case of errors
    * Supports expired session auto restore
    * Has an option to fetch Top Clips (limited to 1000 records by Twitch API)
    
2. **DashboardClipsFetcher** — it uses *auth-token* to crawl clips created by the user from the clip's manager dashboard.
    * Supports filters: USER_ID, CHUNK_LIMIT, SORT, PERIOD
    * Supports fetching resume (starts where it was previously stopped
    * Auto-resumes fetching in case of errors
    * Fetching is limited to 1000 records by Twitch API
    
3. **ClipsDownloader** — it uses fetched clips records to download clip files.
    * Supports filters: minViewCount, maxViewCount, createdAtFrom, createdAtTo, gameId, videoId, creatorId, creatorName, title
    * Supports multithreading to utilize all bandwidth
    * Supports resuming partially downloaded clips
    * Auto-resumes downloading in case of errors
    * Logs successfully downloaded and skipped clips
    
4. **ClipsDeleter** — it uses fetched clips record list and *auth-token* to delete clips from Twitch.
    * Supports filters: minViewCount, maxViewCount, createdAtFrom, createdAtTo, gameId, videoId, creatorId, creatorName, title
    * Auto-resumes deleting in case of errors
    * Logs successfully deleted and skipped clips

5. **ClipsCreator** — create Twitch clips from vods using *auth-token*.
    * Logs successfully created clips
    * Auto-resumes the creation in case of errors

## Problems

If you discover any issues or have a feature request, then please [open an issue here](https://github.com/kappaflow/twitchcm/issues/new).

Also, there is [a list of features](./TODO) which I would like to be implemented. Please contact me if you want to contribute.

## License

Released under the [MIT license](./LICENSE).
