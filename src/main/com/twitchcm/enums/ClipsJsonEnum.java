package main.com.twitchcm.enums;

public enum ClipsJsonEnum {
    EMBED_URL(      "embed_url","embed_url", "embedURL"),
    BROADCASTER_ID( "broadcaster_id","broadcaster_id", "id"),
    CREATED_AT(     "created_at","created_at", "createdAt"),
    LANGUAGE(       "language","language", "language"),
    BROADCASTER_NAME("broadcaster_name","broadcaster_name", "displayName"),
    TITLE(          "title","title", "title"),
    THUMBNAIL_URL(  "thumbnail_url","thumbnail_url", "thumbnailURL"),
    URL(            "url","url", "url"),
    CREATOR_ID(     "creator_id","creator_id", "id"),
    CREATOR_NAME(   "creator_name","creator_name", "displayName"),
    ID(             "id","id", "slug"),
    VIEWER_COUNT(   "view_count","view_count", "viewCount"),
    VIDEO_ID(       "video_id","video_id", "id"),
    GAME_ID(        "game_id","game_id", "id"),

    PAGINATION(     null,"pagination", null),
    CURSOR(         null,"cursor", "cursor"),

    NEXT_CURSOR(    "nextCursor",null, null),
    NEXT_STARTED_AT("nextStartedAt",null, null),

    HAS_NEXT_PAGE(  "hasNextPage",null, "hasNextPage"),

    CURATOR(        null,null, "curator"),
    GAME(           null,null, "game"),
    VIDEO(          null,null, "video"),
    BROADCASTER(    null,null, "broadcaster");

    private final String m_fetchedJsonKey;
    private final String m_publicApiJsonKey;
    private final String m_internalApiJsonKey;

    ClipsJsonEnum(String _fetchedJsonKey, String _publicApiJsonKey, String _internalApiJsonKey) {
        m_fetchedJsonKey = _fetchedJsonKey;
        m_publicApiJsonKey = _publicApiJsonKey;
        m_internalApiJsonKey = _internalApiJsonKey;
    }

    public String getFetchedJsonKey()
    {
        return m_fetchedJsonKey;
    }

    public String getPublicJsonKey()
    {
        return m_publicApiJsonKey;
    }

    public String getIntJsonKey()
    {
        return m_internalApiJsonKey;
    }
}
