package main.com.twitchcm.api;

import main.com.twitchcm.clipslog.DeleteFileLog;
import main.com.twitchcm.enums.AuthJsonEnum;
import main.com.twitchcm.enums.ClipsJsonEnum;
import main.com.twitchcm.enums.DataSourceEnum;
import main.com.twitchcm.enums.VerboseEnum;
import main.com.twitchcm.filter.ProceedClipsFilter;
import main.com.twitchcm.tool.TwitchWebConnection;
import main.com.twitchcm.tool.Utils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class ClipsDeleter extends ClipsActionAbstract
{

    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, " +
            "like Gecko) Chrome/83.0.4103.61 Safari/537.36";
    private static final String HOST = "gql.twitch.tv";

    private ProceedClipsFilter m_proceedFilter = new ProceedClipsFilter();

    public ClipsDeleter(DataSourceEnum _dataSource, boolean _allowOverwrite)
    {
        if (_dataSource == DataSourceEnum.FILES)
        {
            m_clipsLog = new DeleteFileLog( _allowOverwrite );
        }
        else
        {
            throw new RuntimeException( "Incorrect clipsHandler" );
        }
    }

    public void setProceedFilter(ProceedClipsFilter _proceedFilter)
    {
        m_proceedFilter = _proceedFilter;
    }

    public void deleteClips(int _eachTime)
    {
        JSONArray clipsToDelete = new JSONArray();
        JSONArray slugsToDelete = new JSONArray();

        JSONArray deletedClipsSlug = null;
        JSONArray skippedClips;

        JSONObject clipsObj;
        String slug;

        if (m_clipsLog == null)
        {
            System.err.println( "Warning! ClipsLogInterface isn't set" );
            return;
        }

        try
        {
            m_clipsLog.init();
        }
        catch (SecurityException e)
        {
            System.err.println( "Failed to create directory!" );
            if (VerboseEnum.isShow( VerboseEnum.DEBUG, VERBOSE ))
                e.printStackTrace();
        }

        Scanner fetchedSc = m_clipsLog.getLastSourceLineToRead();

        if (fetchedSc == null)
        {
            System.err.println( "Cant read source file" );
            return;
        }

        while (fetchedSc.hasNextLine())
        {
            for (int i = 0; i < _eachTime; i++)
            {
                if (!fetchedSc.hasNextLine())
                {
                    break;
                }
                clipsObj = Utils.parseJsonObj( fetchedSc.nextLine() );
                if (m_proceedFilter.isSatisfy( clipsObj ))
                {
                    clipsToDelete.add( clipsObj );
                    slug = clipsObj.get( ClipsJsonEnum.ID.getFetchedJsonKey() ).toString();
                    slugsToDelete.add( slug );
                }
            }

            try
            {
                deletedClipsSlug = getDeletedClips( slugsToDelete );
                JSONArray deletedClips = slugToClip( deletedClipsSlug, clipsToDelete );
                m_clipsLog.write( deletedClips );
                if (VerboseEnum.isShow( VerboseEnum.DEBUG, VERBOSE ))
                    System.out.println( "Clips deleted:\n" + deletedClips.toJSONString() );
            }
            catch (RuntimeException e)
            {
                System.err.println( e.getMessage() );
                if (VerboseEnum.isShow( VerboseEnum.DEBUG, VERBOSE ))
                    e.printStackTrace();
            }
            finally
            {
                skippedClips = getSkippedClips( deletedClipsSlug, clipsToDelete );
                if (!skippedClips.isEmpty())
                {
                    m_clipsLog.writeSkipped( skippedClips );
                    if (VerboseEnum.isShow( VerboseEnum.DEBUG, VERBOSE ))
                        System.err.println( "Clips skipped:\n" + skippedClips.toJSONString() );
                }
            }

            clipsToDelete.clear();
            slugsToDelete.clear();
        }

        fetchedSc.close();
    }

    private JSONArray slugToClip(JSONArray _slugs, JSONArray _clips)
    {
        Map<String,JSONObject> parsedClipsMap = new HashMap<>();
        JSONArray parsedClips = new JSONArray();

        _slugs.forEach( v -> parsedClipsMap.put( ((JSONObject) v).get( "slug" ).toString(), null ) );

        _clips.forEach( v -> parsedClipsMap.replace( ((JSONObject) v).get( ClipsJsonEnum.ID.getFetchedJsonKey() ).toString(),
                (JSONObject) v ) );

        parsedClips.addAll( parsedClipsMap.values() );

        return parsedClips;
    }

    private String requestDelete(JSONArray _listToDelete)
    {
        String targetURL = "https://gql.twitch.tv/gql";
        HashMap<String,String> requestProperty = new HashMap<>();
        String respInline;

        requestProperty.put( "Host", HOST );
        requestProperty.put( "User-Agent", USER_AGENT );
        requestProperty.put( "Content-Type", "text/plain;charset=UTF-8" );

        if (m_auth != null)
        {
            requestProperty.put( "Client-ID", m_auth.get( AuthJsonEnum.CLIENT_ID ) );
            requestProperty.put( "Authorization", "OAuth " + m_auth.get( AuthJsonEnum.AUTH_TOKEN ) );
        }
        else
        {
            System.err.println( "Warning! Auth doesn't set" );
        }

        TwitchWebConnection webConnection = new TwitchWebConnection( targetURL, requestProperty );

        String payload =
                "[{\"operationName\":\"Clips_DeleteClips\",\"variables\":{\"input\":{\"slugs\":" + _listToDelete.toJSONString() + "}},\"extensions\":{\"persistedQuery\":{\"version\":1,\"sha256Hash\":\"df142a7eec57c5260d274b92abddb0bd1229dc538341434c90367cf1f22d71c4\"}}}]";

        respInline = webConnection.doPostRequest( payload );

        return respInline;
    }

    private JSONArray getDeletedClips(JSONArray _clipsToDel) throws RuntimeException
    {
        JSONArray deletedClipsArray;
        JSONObject jsonObj;
        String respInline;

        respInline = requestDelete( _clipsToDel );

        if (_clipsToDel.isEmpty())
        {
            throw new RuntimeException( "Error! No clips queued up to delete" );
        }

        try
        {
            jsonObj = (JSONObject) Utils.parseJsonArr( respInline ).get( 0 );
        }
        catch (IndexOutOfBoundsException e)
        {
            throw new RuntimeException( "Can't get object from json!" );
        }

        if (jsonObj.containsKey( "errors" ))
        {
            throw new RuntimeException( String.format( "Error response: %s \non deleting:\n%s",
                    jsonObj.get( "errors" ).toString(), _clipsToDel.toString() ) );
        }

        try
        {
            deletedClipsArray =
                    (JSONArray) ((JSONObject) ((JSONObject) (jsonObj.get( "data" ))).get( "deleteClips" )).get(
                            "clips" );
        }
        catch (IndexOutOfBoundsException e)
        {
            throw new RuntimeException( "Can't get object from json!" );
        }

        return deletedClipsArray;
    }

    private JSONArray getSkippedClips(JSONArray _deletedClips, JSONArray _clipsToDel)
    {
        Map<String,JSONObject> parsedClipsMap = new HashMap<>();
        JSONArray parsedSkippedClips = new JSONArray();

        _clipsToDel.forEach( v -> parsedClipsMap.put( ((JSONObject) v).get( ClipsJsonEnum.ID.getFetchedJsonKey() ).toString(),
                (JSONObject) v ) );

        if (_deletedClips != null)
        {
            _deletedClips.forEach( v -> parsedClipsMap.remove( ((JSONObject) v).get( "slug" ).toString() ) );
        }

        parsedSkippedClips.addAll( parsedClipsMap.values() );

        return parsedSkippedClips;
    }
}
