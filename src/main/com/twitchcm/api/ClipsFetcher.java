package main.com.twitchcm.api;

import main.com.twitchcm.clipslog.FetchFileLog;
import main.com.twitchcm.enums.AuthJsonEnum;
import main.com.twitchcm.enums.ClipsJsonEnum;
import main.com.twitchcm.enums.DataSourceEnum;
import main.com.twitchcm.enums.VerboseEnum;
import main.com.twitchcm.filter.FetchClipsFilter;
import main.com.twitchcm.tool.TwitchWebConnection;
import main.com.twitchcm.tool.Utils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.HashMap;

public class ClipsFetcher extends ClipsActionAbstract implements FetchClipsInterface
{
    private FetchClipsFilter m_fetchFilter;

    private int m_clipsCounter;

    public ClipsFetcher(DataSourceEnum _dataSource, boolean _allowOverwrite)
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

    public void setFetchFilter(FetchClipsFilter _fetchFilter)
    {
        m_fetchFilter = _fetchFilter;
    }

    public void distinctClips()
    {
        if (m_clipsLog == null)
        {
            System.err.println( "Error! ClipsLogInterface isn't set" );
        }
        else
        {
            m_clipsLog.distinct();
        }
    }

    public void fetchTopClips()
    {
        String currentCursor = "";

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

                if ((boolean) m_clipsLog.getLastRecord().get( ClipsJsonEnum.HAS_NEXT_PAGE.getFetchedJsonKey() ))
                {
                    m_fetchFilter = parseFilter( m_clipsLog.readFetchFilter() );

                    currentCursor =
                            m_clipsLog.getLastRecord().get( ClipsJsonEnum.NEXT_CURSOR.getFetchedJsonKey() ).toString();
                }
                else
                {
                    System.err.println( "Warning! All clips were already fetched!" );
                    return;
                }
            }
            else
            {
                if (Utils.getBroadcasterLogin( m_fetchFilter.getBroadcasterId(), m_auth ) == null)
                {
                    System.err.println( "Error! The specified broadcasterId can't be found" );
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

    //fetched entries can have duplicates
    public void fetchClips()
    {
        String currentCursor = "";
        ZonedDateTime startedAt;
        ZonedDateTime endedAt;
        Duration deltaPeriod;

        m_clipsCounter = 0;

        if (m_fetchFilter == null)
        {
            System.err.println( "Error! Fetch filter is not set" );
            return;
        }

        startedAt = m_fetchFilter.getStartedAt();
        endedAt = m_fetchFilter.getEndedAt();
        deltaPeriod = m_fetchFilter.getDeltaPeriod();

        if (VerboseEnum.isShow( VerboseEnum.DEBUG, VERBOSE ))
        {
            System.out.println( "StartedAt " + startedAt.toInstant().toString() );
            System.out.println( "EndedAt " + endedAt.toInstant().toString() );
            System.out.println( "deltaPeriod " + deltaPeriod.toString() );
        }

        if (m_clipsLog != null)
        {
            if (m_clipsLog.isContinue())
            {
                continueWarningInfo();

                if ((boolean) m_clipsLog.getLastRecord().get( ClipsJsonEnum.HAS_NEXT_PAGE.getFetchedJsonKey() ))
                {
                    m_fetchFilter = parseFilter( m_clipsLog.readFetchFilter() );

                    currentCursor =
                            m_clipsLog.getLastRecord().get( ClipsJsonEnum.NEXT_CURSOR.getFetchedJsonKey() ).toString();
                    startedAt = ZonedDateTime.parse(
                            m_clipsLog.getLastRecord().get( ClipsJsonEnum.NEXT_STARTED_AT.getFetchedJsonKey() ).toString() );
                }
                else
                {
                    System.err.println( "Warning! All clips were already fetched!" );
                    return;
                }
            }
            else
            {
                if (Utils.getBroadcasterLogin( m_fetchFilter.getBroadcasterId(), m_auth ) == null)
                {
                    System.err.println( "Error! The specified broadcasterId can't be found" );
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
                    return;
                }
            }
        }
        else
        {
            if (VerboseEnum.isShow( VerboseEnum.DEBUG, VERBOSE ))
                System.err.println( "Warning! ClipsProcessor isn't set" );
        }

        if (startedAt.isAfter( endedAt ))
        {
            if (VerboseEnum.isShow( VerboseEnum.DEBUG, VERBOSE ))
                System.err.println( "Error! _startedAt must be > _endedAt" );
            return;
        }
        else if (deltaPeriod.isNegative() || deltaPeriod.isZero())
        {
            if (VerboseEnum.isShow( VerboseEnum.DEBUG, VERBOSE ))
                System.err.println( "Error! _deltaPeriod must be > 0" );
            return;
        }

        fetchSubPeriod( currentCursor, startedAt, endedAt, deltaPeriod );
    }

    private void fetchSubPeriod(String _currentCursor, ZonedDateTime _startedAt, ZonedDateTime endedAt,
                                Duration _deltaPeriod)
    {
        boolean is_finished = false;

        ZonedDateTime interStartedAt = _startedAt;
        ZonedDateTime interEndedAt;

        while (!is_finished)
        {
            interEndedAt = interStartedAt.plus( _deltaPeriod );

            if (interEndedAt.isAfter( endedAt ))
            {
                interEndedAt = endedAt;
                is_finished = true;
            }

            if (VerboseEnum.isShow( VerboseEnum.DEBUG, VERBOSE ))
            {
                System.out.println( "interStartedAt " + interStartedAt.toInstant().toString() );
                System.out.println( "interEndedAt " + interEndedAt.toInstant().toString() );
            }

            do
            {
                _currentCursor = fetchClipsChunk( _currentCursor, interStartedAt, interEndedAt );
            } while (!_currentCursor.isEmpty());

            interStartedAt = interEndedAt;
        }
    }

    private String fetchClipsChunk(String _cursor)
    {
        return fetchClipsChunk( _cursor, null, null );
    }

    private String fetchClipsChunk(String _cursor, ZonedDateTime _startedAt, ZonedDateTime _endedAt)
    {
        JSONObject rawJsonChunk;
        JSONArray parsedClipsArray;

        String nextCursor = "";

        rawJsonChunk = requestClipsChunk( _cursor, _startedAt, _endedAt );
        parsedClipsArray = parseFetchChunk( rawJsonChunk, _startedAt);

        if (!parsedClipsArray.isEmpty())
        {
            nextCursor = ((JSONObject) parsedClipsArray.get( 0 )).get( ClipsJsonEnum.NEXT_CURSOR.getFetchedJsonKey() ).toString();
        }

        if (VerboseEnum.isShow( VerboseEnum.DEBUG, VERBOSE ))
        {
            System.out.println( "Clips json:\n" + rawJsonChunk.get( "data" ).toString() );
            System.out.println( "Cursor: " + _cursor );
            System.out.println( "NextCursor: " + nextCursor );
        }

        if (VerboseEnum.isShow( VerboseEnum.NORMAL, VERBOSE ))
        {
            System.out.println( "Clips fetched: " + ((JSONArray) rawJsonChunk.get( "data" )).size() );
            m_clipsCounter += ((JSONArray) rawJsonChunk.get( "data" )).size();
            System.out.println( "Total clips fetched: " + m_clipsCounter );
        }

        if (m_clipsLog != null)
        {
            m_clipsLog.write( parsedClipsArray );
        }
        else
        {
            System.err.println( "Warning! ClipsLog isn't set" );
        }

        return nextCursor;
    }

    private JSONArray parseFetchChunk(JSONObject _rawJsonChunk, ZonedDateTime _nextStartedAt)
    {
        JSONArray parsedClipsArray = new JSONArray();
        JSONObject rawClipJsonObj;

        JSONArray rawClipsArray = (JSONArray) _rawJsonChunk.get( "data" );
        String nextCursor = "";
        String nextStartedAt = "";

        try
        {
            JSONObject pagination = (JSONObject) _rawJsonChunk.get( ClipsJsonEnum.PAGINATION.getPublicJsonKey() );
            if (pagination.get( ClipsJsonEnum.CURSOR.getPublicJsonKey() ) != null)
            {
                nextCursor = pagination.get( ClipsJsonEnum.CURSOR.getPublicJsonKey() ).toString();
            }
        }
        catch (NullPointerException | ClassCastException e)
        {
            System.err.println( e.toString() );
            if (VerboseEnum.isShow( VerboseEnum.DEBUG, VERBOSE ))
                e.printStackTrace();
        }

        if (_nextStartedAt != null)
        {
            nextStartedAt = _nextStartedAt.toString();
        }

        for (Object jsonObj : rawClipsArray)
        {
            rawClipJsonObj = (JSONObject) jsonObj;

            parsedClipsArray.add( buildClipObj( rawClipJsonObj, nextCursor, nextStartedAt ) );
        }

        return parsedClipsArray;
    }

    private JSONObject buildClipObj(JSONObject _rawClipObject, String _nextCursor, String _nextStartedAt)
    {
        JSONObject parsedClipsJsonObj = new JSONObject();
        boolean nextPage = true;

        if (_nextCursor.isEmpty())
        {
            nextPage = false;
        }

        parsedClipsJsonObj.put( ClipsJsonEnum.EMBED_URL.getFetchedJsonKey(),
                _rawClipObject.get( ClipsJsonEnum.EMBED_URL.getPublicJsonKey() ) );

        parsedClipsJsonObj.put( ClipsJsonEnum.BROADCASTER_ID.getFetchedJsonKey(),
                _rawClipObject.get( ClipsJsonEnum.BROADCASTER_ID.getPublicJsonKey() ) );

        parsedClipsJsonObj.put( ClipsJsonEnum.CREATED_AT.getFetchedJsonKey(),
                _rawClipObject.get( ClipsJsonEnum.CREATED_AT.getPublicJsonKey() ) );

        parsedClipsJsonObj.put( ClipsJsonEnum.LANGUAGE.getFetchedJsonKey(),
                _rawClipObject.get( ClipsJsonEnum.LANGUAGE.getPublicJsonKey() ) );

        parsedClipsJsonObj.put( ClipsJsonEnum.BROADCASTER_NAME.getFetchedJsonKey(),
                _rawClipObject.get( ClipsJsonEnum.BROADCASTER_NAME.getPublicJsonKey() ) );

        parsedClipsJsonObj.put( ClipsJsonEnum.TITLE.getFetchedJsonKey(),
                _rawClipObject.get( ClipsJsonEnum.TITLE.getPublicJsonKey() ) );

        parsedClipsJsonObj.put( ClipsJsonEnum.THUMBNAIL_URL.getFetchedJsonKey(),
                _rawClipObject.get( ClipsJsonEnum.THUMBNAIL_URL.getPublicJsonKey() ) );

        parsedClipsJsonObj.put( ClipsJsonEnum.URL.getFetchedJsonKey(),
                _rawClipObject.get( ClipsJsonEnum.URL.getPublicJsonKey() ) );

        parsedClipsJsonObj.put( ClipsJsonEnum.CREATOR_ID.getFetchedJsonKey(),
                _rawClipObject.get( ClipsJsonEnum.CREATOR_ID.getPublicJsonKey() ) );

        parsedClipsJsonObj.put( ClipsJsonEnum.CREATOR_NAME.getFetchedJsonKey(),
                _rawClipObject.get( ClipsJsonEnum.CREATOR_NAME.getPublicJsonKey() ) );

        parsedClipsJsonObj.put( ClipsJsonEnum.ID.getFetchedJsonKey(),
                _rawClipObject.get( ClipsJsonEnum.ID.getPublicJsonKey() ) );

        parsedClipsJsonObj.put( ClipsJsonEnum.VIEWER_COUNT.getFetchedJsonKey(),
                _rawClipObject.get( ClipsJsonEnum.VIEWER_COUNT.getPublicJsonKey() ) );

        parsedClipsJsonObj.put( ClipsJsonEnum.VIDEO_ID.getFetchedJsonKey(),
                _rawClipObject.get( ClipsJsonEnum.VIDEO_ID.getPublicJsonKey() ) );

        parsedClipsJsonObj.put( ClipsJsonEnum.GAME_ID.getFetchedJsonKey(),
                _rawClipObject.get( ClipsJsonEnum.GAME_ID.getPublicJsonKey() ) );

        parsedClipsJsonObj.put( ClipsJsonEnum.NEXT_CURSOR.getFetchedJsonKey(), _nextCursor );
        parsedClipsJsonObj.put( ClipsJsonEnum.NEXT_STARTED_AT.getFetchedJsonKey(), _nextStartedAt );
        parsedClipsJsonObj.put( ClipsJsonEnum.HAS_NEXT_PAGE.getFetchedJsonKey(), nextPage );

        return parsedClipsJsonObj;
    }

    private JSONObject requestClipsChunk(String _cursor, ZonedDateTime _startedAt, ZonedDateTime _endedAt)
    {
        final String targetURL = "https://api.twitch.tv/helix/clips";

        HashMap<String,String> reqProperty = new HashMap<>();

        String urlParameters;
        String jsonInline;

        if (_startedAt == null || _endedAt == null)
        {
            urlParameters = String.format( "?broadcaster_id=%s&first=%s&after=%s",
                    m_fetchFilter.getBroadcasterId(),
                    m_fetchFilter.getClipsChunkLimit(),
                    _cursor );
        }
        else
        {
            urlParameters = String.format( "?broadcaster_id=%s&first=%s&after=%s&started_at=%s&ended_at=%s",
                    m_fetchFilter.getBroadcasterId(),
                    m_fetchFilter.getClipsChunkLimit(),
                    _cursor,
                    _startedAt.toInstant().toString(),
                    _endedAt.toInstant().toString() );
        }

        if (m_auth != null)
        {
            reqProperty.put( "Client-ID", m_auth.get( AuthJsonEnum.CLIENT_ID ) );
            reqProperty.put( "Authorization",
                    m_auth.get( AuthJsonEnum.TOKEN_TYPE ) + " " + m_auth.get( AuthJsonEnum.ACCESS_TOKEN ) );
        }
        else
        {
            System.err.println( "Warning! Authorization isn't set" );
        }

        jsonInline = new TwitchWebConnection( targetURL + urlParameters, reqProperty ).doGetRequest();

        return Utils.parseJsonObj( jsonInline );
    }

    private FetchClipsFilter parseFilter(JSONObject _headerJsonFilter)
    {
        FetchClipsFilter fetchFilter = new FetchClipsFilter();

        try
        {
            fetchFilter.setFilter(
                    _headerJsonFilter.get( FetchClipsFilter.PublicApiClipsFilterEnum.BROADCASTER_ID.toString() ).toString(),
                    _headerJsonFilter.get( FetchClipsFilter.PublicApiClipsFilterEnum.STARTED_AT.toString() ).toString(),
                    _headerJsonFilter.get( FetchClipsFilter.PublicApiClipsFilterEnum.ENDED_AT.toString() ).toString(),
                    _headerJsonFilter.get( FetchClipsFilter.PublicApiClipsFilterEnum.DELTA_PERIOD.toString() ).toString(),
                    _headerJsonFilter.get( FetchClipsFilter.PublicApiClipsFilterEnum.CLIPS_CHUNK_LIMIT.toString() ).toString()
            );
        }
        catch (DateTimeParseException | NumberFormatException e)
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
