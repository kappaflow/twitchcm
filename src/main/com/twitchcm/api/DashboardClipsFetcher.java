package main.com.twitchcm.api;

import main.com.twitchcm.clipslog.FetchFileLog;
import main.com.twitchcm.enums.AuthJsonEnum;
import main.com.twitchcm.enums.ClipsJsonEnum;
import main.com.twitchcm.enums.DataSourceEnum;
import main.com.twitchcm.enums.VerboseEnum;
import main.com.twitchcm.filter.DashboardFetchClipsFilter;
import main.com.twitchcm.tool.TwitchWebConnection;
import main.com.twitchcm.tool.Utils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.HashMap;

public class DashboardClipsFetcher extends ClipsActionAbstract implements FetchClipsInterface
{
    private static final int REQUEST_ATTEMPT_LIMIT = 15;
    private static final int REQUEST_ATTEMPT_TIMEOUT_MS = 3000;
    private int m_requestAttemptCounter = 0;

    private DashboardFetchClipsFilter m_fetchFilter;

    private int m_clipsCounter;

    public DashboardClipsFetcher(DataSourceEnum _dataSource, boolean _allowOverwrite)
    {
        if (_dataSource == DataSourceEnum.FILES)
        {
            m_clipsLog = new FetchFileLog( _allowOverwrite );
        }
        else
        {
            throw new RuntimeException( "Incorrect clipsHandler" );
        }
    }

    public void setFetchFilter(DashboardFetchClipsFilter _fetchFilter)
    {
        m_fetchFilter = _fetchFilter;
    }

    public void fetchClips()
    {
        String currentCursor = "";
        JSONObject lastRecord;

        m_clipsCounter = 0;

        if (m_fetchFilter == null)
        {
            System.err.println( "Error! Filter is not set" );
            return;
        }

        if (m_clipsLog != null)
        {
            if (m_clipsLog.isContinue())
            {
                continueWarningInfo();

                lastRecord = m_clipsLog.getLastRecord();

                if ((boolean) lastRecord.get( ClipsJsonEnum.HAS_NEXT_PAGE.getFetchedJsonKey() ))
                {
                    m_fetchFilter = parseFilter( m_clipsLog.readFetchFilter() );

                    currentCursor = lastRecord.get( ClipsJsonEnum.NEXT_CURSOR.getFetchedJsonKey() ).toString();
                }
                else
                {
                    System.err.println( "Warning! All clips were already fetched!" );
                    return;
                }
            }
            else
            {
                if (Utils.getBroadcasterLogin( m_fetchFilter.getUserId(), m_auth ) == null)
                {
                    System.err.println( "Error! The specified userId can't be found" );
                    return;
                }
                try
                {
                    m_clipsLog.init( m_fetchFilter );
                }
                catch (SecurityException e)
                {
                    System.err.println( "Error! Failed to create directory!" );
                    if (VerboseEnum.isShow( VerboseEnum.DEBUG, VERBOSE ))
                        e.printStackTrace();
                }
            }
        }
        else
        {
            if (VerboseEnum.isShow( VerboseEnum.DEBUG, VERBOSE ))
                System.err.println( "Warning! ClipsLogInterface isn't set" );
        }

        do
        {
            currentCursor = fetchClipsChunk( currentCursor );
        } while (!currentCursor.isEmpty());
    }

    private JSONArray parseFetchChunk(JSONArray _rawJsonArr)
    {
        JSONArray rawClipsArr;
        JSONArray parsedClipsArr = new JSONArray();
        boolean hasNext;
        String nextCursor;


        JSONObject clipsObj =
                (JSONObject) ((JSONObject) ((JSONObject) ((JSONObject) (_rawJsonArr.get( 0 ))).get( "data" )).get(
                        "user" )).get(
                        "clips" );

        hasNext = (boolean) ((JSONObject) clipsObj.get( "pageInfo" )).get( "hasNextPage" );

        rawClipsArr = (JSONArray) clipsObj.get( "edges" );

        nextCursor = String.valueOf ( ((JSONObject) rawClipsArr.get( rawClipsArr.size() - 1 )).get( "cursor" ) );

        if( nextCursor.equals( "null" ) )
        {
            nextCursor = "";
        }

        for (Object rawClipsObj : rawClipsArr)
        {
            parsedClipsArr.add( buildClipObj( (JSONObject) ((JSONObject) rawClipsObj).get( "node" ),
                    nextCursor, hasNext ) );
        }

        return parsedClipsArr;
    }

    private JSONObject buildClipObj(JSONObject _rawClipObject, String _nextCursor, boolean _hasNext)
    {
        JSONObject parsedClipsObj = new JSONObject();

        JSONObject curatorJsonObj = (JSONObject) _rawClipObject.get( ClipsJsonEnum.CURATOR.getIntJsonKey() );
        JSONObject gameJsonObj = (JSONObject) _rawClipObject.get( ClipsJsonEnum.GAME.getIntJsonKey() );
        JSONObject videoJsonObj = (JSONObject) _rawClipObject.get( ClipsJsonEnum.VIDEO.getIntJsonKey() );
        JSONObject broadcasterJsonObj = (JSONObject) _rawClipObject.get( ClipsJsonEnum.BROADCASTER.getIntJsonKey() );

        parsedClipsObj.put( ClipsJsonEnum.EMBED_URL.getFetchedJsonKey(),
                _rawClipObject.get( ClipsJsonEnum.EMBED_URL.getIntJsonKey() ) );

        parsedClipsObj.put( ClipsJsonEnum.BROADCASTER_ID.getFetchedJsonKey(),
                broadcasterJsonObj.get( ClipsJsonEnum.BROADCASTER_ID.getIntJsonKey() ) );

        parsedClipsObj.put( ClipsJsonEnum.BROADCASTER_NAME.getFetchedJsonKey(),
                broadcasterJsonObj.get( ClipsJsonEnum.BROADCASTER_NAME.getIntJsonKey() ) );

        parsedClipsObj.put( ClipsJsonEnum.CREATED_AT.getFetchedJsonKey(),
                _rawClipObject.get( ClipsJsonEnum.CREATED_AT.getIntJsonKey() ) );

        parsedClipsObj.put( ClipsJsonEnum.LANGUAGE.getFetchedJsonKey(),
                _rawClipObject.get( ClipsJsonEnum.LANGUAGE.getIntJsonKey() ) );

        parsedClipsObj.put( ClipsJsonEnum.TITLE.getFetchedJsonKey(),
                _rawClipObject.get( ClipsJsonEnum.TITLE.getIntJsonKey() ) );

        parsedClipsObj.put( ClipsJsonEnum.THUMBNAIL_URL.getFetchedJsonKey(),
                _rawClipObject.get( ClipsJsonEnum.THUMBNAIL_URL.getIntJsonKey() ) );

        parsedClipsObj.put( ClipsJsonEnum.URL.getFetchedJsonKey(),
                _rawClipObject.get( ClipsJsonEnum.URL.getIntJsonKey() ) );

        parsedClipsObj.put( ClipsJsonEnum.CREATOR_ID.getFetchedJsonKey(),
                curatorJsonObj.get( ClipsJsonEnum.CREATOR_ID.getIntJsonKey() ) );

        parsedClipsObj.put( ClipsJsonEnum.CREATOR_NAME.getFetchedJsonKey(),
                curatorJsonObj.get( ClipsJsonEnum.CREATOR_NAME.getIntJsonKey() ) );

        parsedClipsObj.put( ClipsJsonEnum.ID.getFetchedJsonKey(),
                _rawClipObject.get( ClipsJsonEnum.ID.getIntJsonKey() ) );

        parsedClipsObj.put( ClipsJsonEnum.VIEWER_COUNT.getFetchedJsonKey(),
                _rawClipObject.get( ClipsJsonEnum.VIEWER_COUNT.getIntJsonKey() ) );

        parsedClipsObj.put( ClipsJsonEnum.VIDEO_ID.getFetchedJsonKey(), videoJsonObj != null ?
                videoJsonObj.get( ClipsJsonEnum.VIDEO_ID.getIntJsonKey() ) : "" );

        parsedClipsObj.put( ClipsJsonEnum.GAME_ID.getFetchedJsonKey(), gameJsonObj != null ?
                gameJsonObj.get( ClipsJsonEnum.GAME_ID.getIntJsonKey() ) : "" );

        parsedClipsObj.put( ClipsJsonEnum.NEXT_CURSOR.getFetchedJsonKey(), _nextCursor );
        parsedClipsObj.put( ClipsJsonEnum.HAS_NEXT_PAGE.getFetchedJsonKey(), _hasNext );

        return parsedClipsObj;
    }

    private String fetchClipsChunk(String _cursor)
    {
        JSONArray rawJsonChunkArr;
        JSONArray parsedClipsArr;

        String nextCursor = "";

        rawJsonChunkArr = requestClipsChunk( _cursor );
        parsedClipsArr = parseFetchChunk( rawJsonChunkArr );

        if (!parsedClipsArr.isEmpty())
        {
            nextCursor =
                    ((JSONObject) parsedClipsArr.get( 0 )).get( ClipsJsonEnum.NEXT_CURSOR.getFetchedJsonKey() ).toString();
        }

        if (VerboseEnum.isShow( VerboseEnum.DEBUG, VERBOSE ))
        {
            System.out.println( "Clips json:\n" + parsedClipsArr.toJSONString() );
            System.out.println( "Cursor: " + _cursor );
            System.out.println( "NextCursor: " + nextCursor );
        }

        if (VerboseEnum.isShow( VerboseEnum.NORMAL, VERBOSE ))
        {
            System.out.println( "Clips fetched: " + parsedClipsArr.size() );
            m_clipsCounter += parsedClipsArr.size();
            System.out.println( "Total clips fetched: " + m_clipsCounter );
        }

        if (m_clipsLog != null)
        {
            m_clipsLog.write( parsedClipsArr );
        }
        else
        {
            System.err.println( "Warning! ClipsLog isn't set" );
        }

        return nextCursor;
    }

    private JSONArray requestClipsChunk(String _cursor)
    {
        final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, " +
                "like Gecko) Chrome/83.0.4103.61 Safari/537.36";
        final String HOST = "gql.twitch.tv";

        final String targetURL = "https://gql.twitch.tv/gql";

        HashMap<String,String> reqProperty = new HashMap<>();

        String payload;
        String jsonInline;

        reqProperty.put( "Host", HOST );
        reqProperty.put( "User-Agent", USER_AGENT );
        reqProperty.put( "Content-Type", "text/plain;charset=UTF-8" );

        payload = String.format( "[{\"operationName\":\"ClipsManagerTable_User\"," +
                        "\"variables\":{\"login\":\"%s\",\"limit\":%s," +
                        "\"criteria\":{\"sort\":\"%s\",\"period\":\"ALL_TIME\"," +
                        "\"curatorID\":\"%s\"},\"cursor\":\"%s\"}," +
                        "\"extensions\":{\"persistedQuery\":{\"version\":1," +
                        "\"sha256Hash\":\"b300f79444fdcf2a1a76c101f466c8c9d7bee49b643a4d7878310a4e03944232\"}}}]",
                Utils.getBroadcasterLogin( m_fetchFilter.getUserId(), m_auth ),
                m_fetchFilter.getClipsChunk(),
                m_fetchFilter.getSortOrder().toString(),
                m_fetchFilter.getUserId(),
                _cursor );

        if (m_auth != null)
        {
            reqProperty.put( "Client-ID", m_auth.get( AuthJsonEnum.CLIENT_ID ) );
            reqProperty.put( "Authorization", "OAuth " + " " + m_auth.get( AuthJsonEnum.AUTH_TOKEN ) );
        }
        else
        {
            System.err.println( "Warning! Authorization isn't set" );
        }

        jsonInline = new TwitchWebConnection( targetURL, reqProperty ).doPostRequest( payload );


        return processTimeoutResponse( Utils.parseJsonArr( jsonInline ), _cursor );
    }

    private JSONArray processTimeoutResponse(JSONArray _responseJsonObj, String _cursor)
    {
        JSONArray errorResponse;
        JSONArray fixedResponse;

        if ( m_requestAttemptCounter > REQUEST_ATTEMPT_LIMIT )
        {
            throw new RuntimeException("Error! Exceed connection attempts limit");
        }

        errorResponse = (JSONArray) ((JSONObject) _responseJsonObj.get( 0 )).get( "errors" );
        if (errorResponse != null)
        {
            m_requestAttemptCounter++;

            System.err.println( "Attempt #: " + m_requestAttemptCounter + "\n" + errorResponse.toString() );
            try
            {
                Thread.sleep( REQUEST_ATTEMPT_TIMEOUT_MS );
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }

            fixedResponse = processTimeoutResponse( requestClipsChunk( _cursor ), _cursor );

            return fixedResponse;
        }

        return _responseJsonObj;
    }

    private DashboardFetchClipsFilter parseFilter(JSONObject _headerJsonFilter)
    {
        DashboardFetchClipsFilter fetchFilter;

        fetchFilter = new DashboardFetchClipsFilter(
                _headerJsonFilter.get( DashboardFetchClipsFilter.DashboardClipsFilterEnum.USER_ID.toString() ).toString() );

        try
        {
            fetchFilter.setFilter(
                    _headerJsonFilter.get( DashboardFetchClipsFilter.DashboardClipsFilterEnum.SORT.toString() ).toString(),
                    _headerJsonFilter.get( DashboardFetchClipsFilter.DashboardClipsFilterEnum.CHUNK_LIMIT.toString() ).toString()
            );
        }
        catch (NumberFormatException e)
        {
            System.err.println( e.getMessage() );
            if (VerboseEnum.isShow( VerboseEnum.DEBUG, VERBOSE ))
                e.printStackTrace();
        }
        return fetchFilter;
    }

    private void continueWarningInfo()
    {
        System.err.println( "Warning! Earlier fetched entries were found. A fetch filter from the file applied" );

        try
        {
            Thread.sleep( 3000 );
        }
        catch (InterruptedException e)
        {
            if (VerboseEnum.isShow( VerboseEnum.DEBUG, VERBOSE ))
                e.printStackTrace();
        }
    }
}
