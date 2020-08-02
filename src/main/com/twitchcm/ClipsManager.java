package main.com.twitchcm;

import main.com.twitchcm.api.*;
import main.com.twitchcm.enums.AuthJsonEnum;
import main.com.twitchcm.enums.DataSourceEnum;
import main.com.twitchcm.filter.DashboardFetchClipsFilter;
import main.com.twitchcm.filter.FetchClipsFilter;
import main.com.twitchcm.filter.ProceedClipsFilter;
import main.com.twitchcm.tool.ConfigReader;
import main.com.twitchcm.tool.TwitchWebConnection;
import main.com.twitchcm.tool.Utils;

import javax.security.auth.login.LoginException;
import java.time.Duration;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;

public class ClipsManager
{

    private static String m_clientId = ConfigReader.getClientIdProp();
    private static String m_clientSecret = ConfigReader.getClientSecretProp();

    private static String m_authToken = ConfigReader.getAuthTokenProp();

    //in case of using DB - DataSource.DATABASE should be used
    private final static DataSourceEnum DATA_SOURCE = DataSourceEnum.FILES;

    private boolean m_allowOverwrite = false;
    private String m_rootDir = null;

    private static final Map<AuthJsonEnum,String> m_auth = new HashMap<>();

    //todo DataSource enum in the constructor args
    public ClipsManager()
    {
        setClientAuth( m_clientId, m_clientSecret );
        setAuthToken( m_authToken );
    }

    public void setAuthToken(String _authToken)
    {
        if (_authToken != null)
        {
            m_auth.put( AuthJsonEnum.AUTH_TOKEN, _authToken );
        }
    }

    public void setClientAuth(String _clientId, String _clientSecret)
    {
        if (_clientId != null)
        {
            m_auth.put( AuthJsonEnum.CLIENT_ID, _clientId );
        }
        if (_clientSecret != null)
        {
            m_auth.put( AuthJsonEnum.CLIENT_SECRET, _clientSecret );
        }
    }

    public void allowOverwrite(boolean _allowOverwrite)
    {
        m_allowOverwrite = _allowOverwrite;
    }

    public void setCustomRootDir(String _parentDir)
    {
        m_rootDir = _parentDir;
    }

    public String getCustomRootDir()
    {
        return m_rootDir;
    }

    public static Map<AuthJsonEnum,String> getAuthCredential()
    {
        if (!m_auth.containsKey( AuthJsonEnum.CLIENT_ID ))
        {
            m_auth.put( AuthJsonEnum.CLIENT_ID, m_clientId );
        }
        if (!m_auth.containsKey( AuthJsonEnum.CLIENT_SECRET ))
        {
            m_auth.put( AuthJsonEnum.CLIENT_SECRET, m_clientSecret );
        }

        if (!m_auth.containsKey( AuthJsonEnum.AUTH_TOKEN ))
        {
            m_auth.put( AuthJsonEnum.AUTH_TOKEN, m_authToken );
        }

        return m_auth;
    }

    public void gameIdToName(String _gameId) throws LoginException
    {
        if (_gameId != null)
        {
            System.out.println( Utils.getGameName( _gameId, TwitchWebConnection.auth( m_auth ) ) );
        }
    }

    public void gameNameToId(String _gameName) throws LoginException
    {
        if (_gameName != null)
        {
            System.out.println( Utils.getGameId( _gameName, TwitchWebConnection.auth( m_auth ) ) );
        }
    }

    public void channelIdToName(String _channelId) throws LoginException
    {
        if (_channelId != null)
        {
            System.out.println( Utils.getBroadcasterLogin( _channelId, TwitchWebConnection.auth( m_auth ) ) );
        }
    }

    public void channelNameToId(String _channelName) throws LoginException
    {
        if (_channelName != null)
        {
            System.out.println( Utils.getBroadcasterId( _channelName, TwitchWebConnection.auth( m_auth ) ) );
        }
    }

    private void setRootDir(ClipsActionAbstract _clipsActionObj)
    {
        if (m_rootDir != null)
        {
            _clipsActionObj.setRootDir( m_rootDir );
        }
    }

    public void fetchTopClips(String _broadcasterId)
    {
        fetchTopClips( new FetchClipsFilter( _broadcasterId ) );
    }

    public void fetchTopClips(FetchClipsFilter _filter)
    {
        ClipsFetcher clipsFetcher = new ClipsFetcher( DATA_SOURCE, m_allowOverwrite );
        setRootDir( clipsFetcher );
        clipsFetcher.setAuth( m_auth );
        clipsFetcher.setFetchFilter( _filter );
        clipsFetcher.fetchTopClips();
    }

    public void fetchClips(FetchClipsFilter _filter)
    {
        ClipsFetcher clipsFetcher = new ClipsFetcher( DATA_SOURCE, m_allowOverwrite );
        setRootDir( clipsFetcher );
        clipsFetcher.setAuth( m_auth );
        clipsFetcher.setFetchFilter( _filter );
        clipsFetcher.fetchClips();
    }

    public void fetchDashboardClips(DashboardFetchClipsFilter _filter)
    {
        DashboardClipsFetcher clipsFetcher = new DashboardClipsFetcher( DATA_SOURCE, m_allowOverwrite );
        setRootDir( clipsFetcher );
        clipsFetcher.setAuth( m_auth );
        clipsFetcher.setFetchFilter( _filter );
        clipsFetcher.fetchClips();
    }

    public void distinctFetchedClips()
    {
        ClipsFetcher clipsFetcher = new ClipsFetcher( DATA_SOURCE, false );
        setRootDir( clipsFetcher );
        clipsFetcher.distinctClips();
    }

    public void downloadClips(String _eachTime, ProceedClipsFilter _proceedFilter)
    {
        try
        {
            downloadClips( Integer.parseInt( _eachTime ), _proceedFilter );
        }
        catch (NumberFormatException e)
        {
            System.err.println( e.getMessage() );
        }
    }

    public void downloadClips(int _eachTime, ProceedClipsFilter _proceedFilter)
    {
        ClipsDownloader clipsDownloader = new ClipsDownloader( DATA_SOURCE, m_allowOverwrite );
        setRootDir( clipsDownloader );
        clipsDownloader.setProceedFilter( _proceedFilter );
        clipsDownloader.downloadClips( _eachTime );
    }

    public void deleteClips(String _eachTime, ProceedClipsFilter _proceedFilter)
    {
        try
        {
            deleteClips( Integer.parseInt( _eachTime ), _proceedFilter );
        }
        catch (NumberFormatException e)
        {
            System.err.println( e.getMessage() );
        }
    }

    public void deleteClips(int _eachTime, ProceedClipsFilter _proceedFilter)
    {
        ClipsDeleter clipsDeleter = new ClipsDeleter( DATA_SOURCE, m_allowOverwrite );
        setRootDir( clipsDeleter );
        clipsDeleter.setAuth( m_auth );
        clipsDeleter.setProceedFilter( _proceedFilter );
        clipsDeleter.deleteClips( _eachTime );
    }

    public void createClip(String _vodId, String _title, String _vodTime, String _durationSec)
    {
        try
        {
            createClip( _vodId, _title,
                    (float) Duration.parse( _vodTime ).toMillis() / 1000,
                    (float) Duration.parse( _durationSec ).toMillis() / 1000 );
        }
        catch (DateTimeParseException e)
        {
            System.err.println( e.getMessage() );
        }
    }

    public void createClip(String _vodId, String _title, float _vodTimeSec, float _durationSec)
    {
        ClipsCreator clipsCreator = new ClipsCreator( DATA_SOURCE, m_allowOverwrite );
        setRootDir( clipsCreator );
        clipsCreator.setAuth( m_auth );
        clipsCreator.createClip( _vodId, _title, _vodTimeSec, _durationSec );
    }
}
