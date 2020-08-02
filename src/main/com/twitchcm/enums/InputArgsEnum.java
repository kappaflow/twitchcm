package main.com.twitchcm.enums;

public enum InputArgsEnum {
    //Global
    ROOT_DIR("dir", "rootDir", "Working directory", true), //except Utils
    ALLOW_OVERWRITE("ow", "allowOverwrite", "Allow to overwrite the obtained data files.", false), //except distinct and utils

    //Auth
    CLIENT_ID("clientId", "clientId", "API client id", true),
    CLIENT_SECRET("clientSecret", "clientSecret", "API client secret", true),
    AUTH_TOKEN("authToken", "authToken", "auth-token from cookies", true),

    //FetchFilter
    BROADCASTER_ID("chId", "broadcasterId", "Channel id (user id) of a broadcaster from which the clip was created. Channel id can be obtained by using chNameToId Util", true),
    STARTED_AT("start", "startedAt", "Start date when the clip was created. If not specified - 1 week period from now is used. RFC3339 format. (Note that the seconds value is ignored; UTC time zone) Example: 2017-11-30T22:34:18Z", true),
    ENDED_AT("end", "endedAt", "End date when the clip was created. If not specified - the current time is used. RFC3339 format. (Note that the seconds value is ignored; UTC time zone) Example: 2017-11-30T22:34:18Z", true),
    DELTA("delta", "deltaPeriod", "Global date range divided to such periods. To not miss clips the period should have less than 1000 clips. If not specified - 1 day period is used. ISO-8601 duration format. Examples: PT15M - 15 minutes; PT10H - 10 hours; P2D - 2 days", true),
    CHUNK_LIMIT("chunk", "chunkLimit", "Maximum number of objects to return per request. Chunk must be in 1-100 range. Default: 20", true), //also used in DashboardFetchFilter

    //DashboardFetchFilter
    SORT_ORDER("sort", "sortOrder", "Specified a fetching order. Options by creation date: CREATED_AT_DESC - descending order; CREATED_AT_ASC - ascending order. Options by views count: VIEWS_DESC - descending order; VIEWS_ASC  - ascending order", true ),
    USER_ID("userId", "userId", "User id (channel id) of the dashboard owner. Channel id can be obtained by using chNameToId Util", true),
    //+ BROADCASTER_ID,CHUNK_LIMIT

    //ProceedFilter
    MIN_VIEW_COUNT("minViews", "minViewsCount", "Max number of times the clip has been viewed", true),
    MAX_VIEW_COUNT("maxViews", "maxViewsCount", "Min number of times the clip has been viewed", true),
    CREATED_AT_FROM("from", "createdAtFrom", "Start date when the clip was created; UTC time zone", true),
    CREATED_AT_TO("to", "createdAtTo", "End date when the clip was created; UTC time zone", true),
    GAME_ID("gameId", "gameId", "ID of the game assigned to the stream when the clip was created", true),
    VIDEO_ID("vodId", "videoId", "ID of the video related to the clip", true), //also used for CREATE_CLIP
    CREATOR_ID("creatorId", "creatorId", "User ID of the stream from which the clip was created", true),
    CREATOR_NAME("creatorName", "creatorName", "Display name corresponding to broadcaster_id", true),
    TITLE("title", "title", "Title of the clip", true), //also used for CREATE_CLIP

    //Utils
    GAME_ID_TO_NAME("gameIdToName", "gameIdToName", "Convert game id to name", true),
    GAME_NAME_TO_ID("gameNameToId", "gameNameToId", "Convert game name to id", true),
    BROADCASTER_ID_TO_NAME("chIdToName", "channelIdToName", "Convert channel id to name", true),
    BROADCASTER_NAME_TO_ID("chNameToId", "channelNameToId", "Convert channel name to id", true),

    //Num of download threads or delete clips per time
    NUM_PER_TIME("qty", "numberPerTime", "How many objects have to be processed at the time", true),

    //Clips creation
    VOD_TIME("vodTime", "videoTime", "Timestamp of the clip start. ISO-8601 duration format. Example: PT2H30M10.1S - 2 hour 30 min 10 sec 100 milliseconds", true),
    CLIP_DURATION("clipDur", "clipDuration", "Duration of the created clip. ISO-8601 duration format. Example: PT30.1S - 30 sec 100 milliseconds", true),
    //+ TITLE,VIDEO_ID

    //Actions
    FETCH_CLIPS("fetch", "fetchClips", "Fetch clips from a specific broadcaster", false),
    FETCH_TOP_CLIPS("fetchTop", "fetchTopClips", "Fetch top clips from a specific broadcaster", false),
    FETCH_DASHBOARD_CLIPS("fetchDashboard", "fetchDashboardClips", "Fetch clips from the dashboard", false),
    DISTINCT_FETCHED_CLIPS("distinct", "distinctFetchedClips", "Parse fetched clips in a new file making clips entries unique", false),
    DOWNLOAD_CLIPS("download", "downloadFetchedClips", "Download fetched clips multithreaded", false),
    DELETE_CLIPS("delete", "deleteFetchClips", "Delete fetched clips", false),
    CREATE_CLIP("create", "createClip", "Create clip from vod", false),

    UTILS("utils", "utils", "Utility tools", false);

    private final String m_argsOption;
    private final String m_argsOptionLong;
    private final String m_description;
    private final boolean m_hasArg;

    InputArgsEnum(String _argsOption, String _argsOptionLong, String _description, boolean _hasArg) {
        m_argsOption = _argsOption;
        m_argsOptionLong = _argsOptionLong;
        m_description = _description;
        m_hasArg = _hasArg;
    }

    @Override
    public String toString() {
        return m_argsOption;
    }

    public String getOpt() {
        return m_argsOption;
    }
    public String getOptLong() {
        return m_argsOptionLong;
    }
    public String getDesc() {
        return m_description;
    }

    public boolean hasArg() {
        return m_hasArg;
    }
}
