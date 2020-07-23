package main.com.twitchcm.api;

import main.com.twitchcm.clipslog.CreatedClipsLog;
import main.com.twitchcm.enums.AuthJsonEnum;
import main.com.twitchcm.enums.DataSourceEnum;
import main.com.twitchcm.enums.VerboseEnum;
import main.com.twitchcm.exceptions.GettingClipsException;
import main.com.twitchcm.tool.TwitchWebConnection;
import main.com.twitchcm.tool.Utils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class ClipsCreator extends ClipsActionAbstract
{
    private static final float EDIT_CLIP_OFFSET = 90;

    private static final int CLIP_INFO_CREATE_ATTEMPT_LIMIT = 10;
    private static final int DELAY_BETWEEN_CREATE_ATTEMPT_MS = 1500;

    private static final int CLIP_INFO_EDIT_ATTEMPT_LIMIT = 2;
    private static final int DELAY_BETWEEN_EDIT_ATTEMPT_MS = 10000;

    public ClipsCreator(DataSourceEnum _dataSource, boolean _allowOverwrite)
    {
        if (_dataSource == DataSourceEnum.FILES)
        {
            m_clipsLog = new CreatedClipsLog( _allowOverwrite );
        }
        else
        {
            throw new RuntimeException( "Incorrect clipsHandler" );
        }
    }

    public void createClip(String _vodId, String _title, float _vodTimeSec, float _durationSec)
    {
        JSONObject createdClipObj;

        if (_vodTimeSec <= 0)
        {
            System.err.println( "Clip start time must be > 0" );
            return;
        }
        if (_durationSec <= 0 || _durationSec > 60)
        {
            System.err.println( "Clip duration must be > 0 and <= 60 sec" );
            return;
        }

        createdClipObj = initClip( _vodId, _vodTimeSec );
        if (createdClipObj != null)
        {
            editClip( createdClipObj, _title, _durationSec );

            if (VerboseEnum.isShow( VerboseEnum.NORMAL, VERBOSE ))
                System.out.println("Created clip URL:\n" + createdClipObj.get( "url" ));
        }
    }

    private JSONObject initClip(String _vodId, float _vodTimeSec)
    {
        String broadcasterId = Utils.getVodBroadcasterId( _vodId, m_auth );
        JSONObject createdClipObj = null;
        String createdClipSlug;

        int attemptCounter = 0;

        try
        {
            createdClipSlug = getCreatedClip( requestCreate( broadcasterId, _vodId, _vodTimeSec + EDIT_CLIP_OFFSET ) );
            do
            {
                if (attemptCounter > CLIP_INFO_CREATE_ATTEMPT_LIMIT)
                {
                    throw new GettingClipsException( "Cant get info about the created clip" );
                }

                if (VerboseEnum.isShow( VerboseEnum.NORMAL, VERBOSE ))
                    System.out.println( "Waiting a clip creation... "
                            + TimeUnit.MILLISECONDS.toSeconds( DELAY_BETWEEN_CREATE_ATTEMPT_MS *attemptCounter ) + " sec" );

                Thread.sleep( DELAY_BETWEEN_CREATE_ATTEMPT_MS );

                createdClipObj = Utils.getClipJson( createdClipSlug, m_auth );

                attemptCounter++;
            } while (createdClipObj == null);
        }
        catch (GettingClipsException e)
        {
            System.err.println( e.getMessage() );
        }
        catch (RuntimeException | InterruptedException e)
        {
            System.err.println( "Error while creating the clip!" );
            if (VerboseEnum.isShow( VerboseEnum.DEBUG, VERBOSE ))
                e.printStackTrace();
        }

        return createdClipObj;
    }

    private void editClip(JSONObject _createdClipObj, String _title, float _durationSec)
    {
        JSONObject editedClipObj = null;
        String editedClipSlug;

        int attemptCounter = 0;

        if (_title == null || _title.equals( "" ))
        {
            _title = _createdClipObj.get( "title" ).toString();
        }

        try
        {
            editedClipSlug = getEditedClip( requestEdit( _createdClipObj.get( "id" ).toString(), _title, 0,
                    _durationSec ) );
            do
            {
                if (attemptCounter > CLIP_INFO_EDIT_ATTEMPT_LIMIT)
                {
                    throw new GettingClipsException( "Cant get info about the edited clip" );
                }

                if (VerboseEnum.isShow( VerboseEnum.NORMAL, VERBOSE ))
                    System.out.println( "Waiting the clip publishing... "
                            + TimeUnit.MILLISECONDS.toSeconds( DELAY_BETWEEN_EDIT_ATTEMPT_MS *attemptCounter ) + " sec");

                Thread.sleep( DELAY_BETWEEN_EDIT_ATTEMPT_MS );

                editedClipObj = Utils.getClipJson( editedClipSlug, m_auth );

                attemptCounter++;
            } while (editedClipObj == null);

        }
        catch (GettingClipsException e)
        {
            System.err.println( e.getMessage() );
        }
        catch (RuntimeException | InterruptedException e)
        {
            System.err.println( "Cant edit the clip!");
            if (VerboseEnum.isShow( VerboseEnum.DEBUG, VERBOSE ))
                e.printStackTrace();
        }

        if (editedClipObj != null)
        {
            write( editedClipObj );
        }
        else
        {
            write( _createdClipObj );
        }
    }

    public String createClip(String _vodId, float _vodTimeSec)
    {
        String broadcasterId = Utils.getVodBroadcasterId( _vodId, m_auth );
        String createdClipSlug;

        if (_vodTimeSec < 0)
        {
            System.err.println( "Vod start time must be >0 sec" );
            return null;
        }

        createdClipSlug = getCreatedClip( requestCreate( broadcasterId, _vodId, _vodTimeSec ) );

        return createdClipSlug;
    }

    private String requestCreate(String _broadcasterId, String _videoId, float _offsetSec)
    {
        String targetURL = "https://gql.twitch.tv/gql";
        HashMap<String,String> requestProperty = new HashMap<>();
        String respInline;

        requestProperty.put( "Host", "gql.twitch.tv" );
        requestProperty.put( "User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like" +
                " Gecko) Chrome/83.0.4103.61 Safari/537.36" );
        requestProperty.put( "Content-Type", "text/plain;charset=UTF-8" );

        if (m_auth != null)
        {
            requestProperty.put( "Client-ID", m_auth.get( AuthJsonEnum.CLIENT_ID ) );
            requestProperty.put( "Authorization", "OAuth " + m_auth.get( AuthJsonEnum.AUTH_TOKEN ) );
        }
        else
        {
            System.err.println( "Warning! Auth doesnt set" );
        }

        TwitchWebConnection webConnection = new TwitchWebConnection( targetURL, requestProperty );

        String payload = String.format( Locale.ROOT, "[{\"operationName\":\"createClip\"," +
                        "\"variables\":{\"input\":{\"broadcasterID\":\"%s\",\"videoID\":\"%s\",\"offsetSeconds\":%" +
                        ".2f}},\"extensions\":{\"persistedQuery\":{\"version\":1," +
                        "\"sha256Hash\":\"518982ccc596c07839a6188e075adc80475b7bc4606725f3011b640b87054ecf\"}}}]",
                _broadcasterId, _videoId, _offsetSec );

        respInline = webConnection.doPostRequest( payload );

        return respInline;
    }

    private String getCreatedClip(String _response)
    {
        JSONObject jsonObj;
        String slug = "";

        jsonObj = (JSONObject) Utils.parseJsonArr( _response ).get( 0 );

        if (jsonObj.get( "errors" ) != null)
        {
            throw new RuntimeException( "Error response on creating: " + jsonObj.get( "errors" ).toString() );
        }

        String url =
                (String) ((JSONObject) ((JSONObject) ((JSONObject) jsonObj.get( "data" )).get( "createClip" )).get(
                        "clip" )).get( "url" );

        try
        {
            slug = new URL( url ).getPath().substring( 1 );
        }
        catch (MalformedURLException e)
        {
            if (VerboseEnum.isShow( VerboseEnum.DEBUG, VERBOSE ))
                e.printStackTrace();
        }

        return slug;
    }


    private String requestEdit(String _slug, String _title, float _offsetSec, float _durationSec)
    {
        String targetURL = "https://gql.twitch.tv/gql";
        HashMap<String,String> requestProperty = new HashMap<>();
        String respInline;

        requestProperty.put( "Host", "gql.twitch.tv" );
        requestProperty.put( "User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like" +
                " Gecko) Chrome/83.0.4103.61 Safari/537.36" );
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

        String payload = String.format( Locale.ROOT, "[{\"operationName\":\"PublishClip\"," +
                        "\"variables\":{\"input\":{\"segments\":[{\"offsetSeconds\":%.1f,\"durationSeconds\":%.1f," +
                        "\"speed\":1}],\"slug\":\"%s\",\"title\":\"%s\"}}," +
                        "\"extensions\":{\"persistedQuery\":{\"version\":1," +
                        "\"sha256Hash\":\"551c30ddfb6c2f00a865daae38164decc79f751a40003eec38f91f4ba6db6171\"}}}]",
                _offsetSec, _durationSec, _slug, _title );

        respInline = webConnection.doPostRequest( payload );

        return respInline;
    }


    private String getEditedClip(String _response)
    {
        JSONObject jsonObj, publishClipObj;
        String slug;

        jsonObj = (JSONObject) Utils.parseJsonArr( _response ).get( 0 );

        publishClipObj = ((JSONObject) ((JSONObject) jsonObj.get( "data" )).get( "publishClip" ));

        if (publishClipObj.get( "error" ) != null)
        {
            throw new RuntimeException( "Error response on creating: " + publishClipObj.get( "error" ).toString() );
        }

        slug = (((JSONObject) publishClipObj.get( "clip" )).get( "slug" )).toString();

        return slug;
    }

    private synchronized void write(JSONObject createdClipObj)
    {
        JSONArray createdClip = new JSONArray();

        createdClip.add( createdClipObj );

        if (m_clipsLog != null)
        {
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
            m_clipsLog.write( createdClip );
        }
        else
        {
            System.err.println( "Warning! ClipsLogInterface isn't set" );
        }
    }
}
