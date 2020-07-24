package main.com.twitchcm;

import main.com.twitchcm.enums.InputArgsEnum;
import main.com.twitchcm.enums.VerboseEnum;
import main.com.twitchcm.filter.DashboardFetchClipsFilter;
import main.com.twitchcm.filter.FetchClipsFilter;
import main.com.twitchcm.filter.ProceedClipsFilter;
import org.apache.commons.cli.*;

import javax.security.auth.login.LoginException;
import java.time.format.DateTimeParseException;

import static main.com.twitchcm.enums.InputArgsEnum.*;

public class InputArgsProcessor
{
    protected static final VerboseEnum VERBOSE = VerboseEnum.DEBUG;
    private static boolean m_exist = false;

    private final String[] m_args;
    private final Options m_actionOptions = new Options();
    private final Options m_secOptions = new Options();
    private CommandLine m_cmd;
    private InputArgsEnum m_inputAction;

    public InputArgsProcessor(String[] args)
    {
        m_args = args;

        if (m_exist)
        {
            throw new RuntimeException( "Error! MyInputArgs object already exist" );
        }
        else
        {
            m_exist = true;
        }
    }

    private void initSecondaryOptions()
    {
        if (m_cmd.hasOption( FETCH_CLIPS.getOpt() ))
        {
            m_inputAction = FETCH_CLIPS;

            initFetchClipOptions();
        }
        else if (m_cmd.hasOption( FETCH_TOP_CLIPS.getOpt() ))
        {
            m_inputAction = FETCH_TOP_CLIPS;

            initFetchTopClipOptions();
        }
        else if (m_cmd.hasOption( FETCH_DASHBOARD_CLIPS.getOpt() ))
        {
            m_inputAction = FETCH_DASHBOARD_CLIPS;

            initFetchDashboardClipOptions();
        }
        else if (m_cmd.hasOption( DISTINCT_FETCHED_CLIPS.getOpt() ))
        {
            m_inputAction = DISTINCT_FETCHED_CLIPS;

            initDistinctFetchedClipOptions();
        }
        else if (m_cmd.hasOption( DOWNLOAD_CLIPS.getOpt() ))
        {
            m_inputAction = DOWNLOAD_CLIPS;

            initDownloadClipOptions();
        }
        else if (m_cmd.hasOption( DELETE_CLIPS.getOpt() ))
        {
            m_inputAction = DELETE_CLIPS;

            initDeleteClipOptions();
        }
        else if (m_cmd.hasOption( CREATE_CLIP.getOpt() ))
        {
            m_inputAction = CREATE_CLIP;

            initCreateClipOptions();
        }
        else if (m_cmd.hasOption( UTILS.getOpt() ))
        {
            m_inputAction = UTILS;

            initUtilsOptions();
        }
    }

    private void runAction()
    {
        switch (m_inputAction)
        {
            case FETCH_CLIPS:
                runFetch();
                break;

            case FETCH_TOP_CLIPS:
                runTopFetch();
                break;

            case FETCH_DASHBOARD_CLIPS:
                runFetchDashboard();
                break;

            case DISTINCT_FETCHED_CLIPS:
                runDistinctFetched();
                break;

            case DOWNLOAD_CLIPS:
                runDownloadClips();
                break;

            case DELETE_CLIPS:
                runDeleteClips();
                break;

            case CREATE_CLIP:
                runCreateClips();
                break;

            case UTILS:
                runUtils();
                break;

            default:
        }
    }

    private FetchClipsFilter initFetchFilter()
    {
        FetchClipsFilter fetchFilter = new FetchClipsFilter();
        try
        {
            fetchFilter.setFilter( m_cmd.getOptionValue( BROADCASTER_ID.getOpt() ),
                    m_cmd.getOptionValue( STARTED_AT.getOpt() ),
                    m_cmd.getOptionValue( ENDED_AT.getOpt() ),
                    m_cmd.getOptionValue( DELTA.getOpt() ),
                    m_cmd.getOptionValue( CHUNK_LIMIT.getOpt() ) );
        }
        catch (RuntimeException e)
        {
            System.err.println( e.toString() );
            if (VerboseEnum.isShow( VerboseEnum.DEBUG, VERBOSE ))
                e.printStackTrace();

            return null;
        }

        return fetchFilter;
    }

    private DashboardFetchClipsFilter initDashboardFetchFilter()
    {
        DashboardFetchClipsFilter fetchFilter;

        try
        {
            fetchFilter = new DashboardFetchClipsFilter( m_cmd.getOptionValue( USER_ID.getOpt() ) );

            fetchFilter.setFilter( m_cmd.getOptionValue( SORT_ORDER.getOpt() ),
                    m_cmd.getOptionValue( CHUNK_LIMIT.getOpt() ) );
        }
        catch (RuntimeException e)
        {
            System.err.println( e.toString() );
            if (VerboseEnum.isShow( VerboseEnum.DEBUG, VERBOSE ))
                e.printStackTrace();

            return null;
        }

        return fetchFilter;
    }

    private ProceedClipsFilter initProceedFilter()
    {
        ProceedClipsFilter proceedFilter = new ProceedClipsFilter();
        try
        {
            proceedFilter.setFilter( m_cmd.getOptionValue( MIN_VIEW_COUNT.getOpt() ),
                    m_cmd.getOptionValue( MAX_VIEW_COUNT.getOpt() ),
                    m_cmd.getOptionValue( CREATED_AT_FROM.getOpt() ),
                    m_cmd.getOptionValue( CREATED_AT_TO.getOpt() ),
                    m_cmd.getOptionValue( GAME_ID.getOpt() ),
                    m_cmd.getOptionValue( VIDEO_ID.getOpt() ),
                    m_cmd.getOptionValue( CREATOR_ID.getOpt() ),
                    m_cmd.getOptionValue( CREATOR_NAME.getOpt() ),
                    m_cmd.getOptionValue( TITLE.getOpt() ) );
        }
        catch (DateTimeParseException | NumberFormatException e)
        {
            System.err.println( e.toString() );
            if (VerboseEnum.isShow( VerboseEnum.DEBUG, VERBOSE ))
                e.printStackTrace();

            return null;
        }

        return proceedFilter;
    }

    private void runFetch()
    {
        ClipsManager clipsManager = new ClipsManager();

        clipsManager.allowOverwrite( m_cmd.hasOption( ALLOW_OVERWRITE.getOpt() ) );
        clipsManager.setClientAuth( m_cmd.getOptionValue( CLIENT_ID.getOpt() ),
                m_cmd.getOptionValue( CLIENT_SECRET.getOpt() ) );
        clipsManager.setCustomRootDir( m_cmd.getOptionValue( ROOT_DIR.getOpt() ) );
        clipsManager.fetchClips( initFetchFilter() );
    }

    private void runTopFetch()
    {
        ClipsManager clipsManager = new ClipsManager();

        clipsManager.allowOverwrite( m_cmd.hasOption( ALLOW_OVERWRITE.getOpt() ) );
        clipsManager.setClientAuth( m_cmd.getOptionValue( CLIENT_ID.getOpt() ),
                m_cmd.getOptionValue( CLIENT_SECRET.getOpt() ) );
        clipsManager.setCustomRootDir( m_cmd.getOptionValue( ROOT_DIR.getOpt() ) );
        clipsManager.fetchTopClips( initFetchFilter() );
    }

    private void runFetchDashboard()
    {
        ClipsManager clipsManager = new ClipsManager();

        clipsManager.allowOverwrite( m_cmd.hasOption( ALLOW_OVERWRITE.getOpt() ) );
        clipsManager.setClientAuth( m_cmd.getOptionValue( CLIENT_ID.getOpt() ),
                m_cmd.getOptionValue( CLIENT_SECRET.getOpt() ) );
        clipsManager.setAuthToken( m_cmd.getOptionValue( AUTH_TOKEN.getOpt() ) );
        clipsManager.setCustomRootDir( m_cmd.getOptionValue( ROOT_DIR.getOpt() ) );
        clipsManager.fetchDashboardClips( initDashboardFetchFilter() );
    }

    private void runDistinctFetched()
    {
        ClipsManager clipsManager = new ClipsManager();

        clipsManager.allowOverwrite( m_cmd.hasOption( ALLOW_OVERWRITE.getOpt() ) );
        clipsManager.setCustomRootDir( m_cmd.getOptionValue( ROOT_DIR.getOpt() ) );
        clipsManager.distinctFetchedClips();
    }

    private void runDownloadClips()
    {
        ClipsManager clipsManager = new ClipsManager();

        clipsManager.allowOverwrite( m_cmd.hasOption( ALLOW_OVERWRITE.getOpt() ) );
        clipsManager.setCustomRootDir( m_cmd.getOptionValue( ROOT_DIR.getOpt() ) );
        clipsManager.downloadClips( m_cmd.getOptionValue( NUM_PER_TIME.getOpt(), "2" ), initProceedFilter() );
    }

    private void runDeleteClips()
    {
        ClipsManager clipsManager = new ClipsManager();

        clipsManager.allowOverwrite( m_cmd.hasOption( ALLOW_OVERWRITE.getOpt() ) );
        clipsManager.setClientAuth( m_cmd.getOptionValue( CLIENT_ID.getOpt() ), null );
        clipsManager.setAuthToken( m_cmd.getOptionValue( AUTH_TOKEN.getOpt() ) );
        clipsManager.setCustomRootDir( m_cmd.getOptionValue( ROOT_DIR.getOpt() ) );
        clipsManager.deleteClips( m_cmd.getOptionValue( NUM_PER_TIME.getOpt(), "100" ), initProceedFilter() );
    }

    private void runCreateClips()
    {
        ClipsManager clipsManager = new ClipsManager();

        clipsManager.allowOverwrite( m_cmd.hasOption( ALLOW_OVERWRITE.getOpt() ) );
        clipsManager.setClientAuth( m_cmd.getOptionValue( CLIENT_ID.getOpt() ),
                m_cmd.getOptionValue( CLIENT_SECRET.getOpt() ) );
        clipsManager.setAuthToken( m_cmd.getOptionValue( AUTH_TOKEN.getOpt() ) );
        clipsManager.setCustomRootDir( m_cmd.getOptionValue( ROOT_DIR.getOpt() ) );
        clipsManager.createClip( m_cmd.getOptionValue( VIDEO_ID.getOpt() ),
                m_cmd.getOptionValue( TITLE.getOpt() ),
                m_cmd.getOptionValue( VOD_TIME.getOpt() ),
                m_cmd.getOptionValue( CLIP_DURATION.getOpt() ) );
    }

    private void runUtils()
    {
        ClipsManager clipsManager = new ClipsManager();
        clipsManager.setClientAuth( m_cmd.getOptionValue( CLIENT_ID.getOpt() ),
                m_cmd.getOptionValue( CLIENT_SECRET.getOpt() ) );
        try
        {
            clipsManager.gameIdToName( m_cmd.getOptionValue( GAME_ID_TO_NAME.getOpt() ) );
            clipsManager.gameNameToId( m_cmd.getOptionValue( GAME_NAME_TO_ID.getOpt() ) );
            clipsManager.channelIdToName( m_cmd.getOptionValue( BROADCASTER_ID_TO_NAME.getOpt() ) );
            clipsManager.channelNameToId( m_cmd.getOptionValue( BROADCASTER_NAME_TO_ID.getOpt() ) );
        }
        catch (LoginException e)
        {
            System.err.println( e.toString() );
            if (VerboseEnum.isShow( VerboseEnum.DEBUG, VERBOSE ))
                e.printStackTrace();
        }
    }

    private void initActionGroupOptions()
    {
        OptionGroup actionGroup = new OptionGroup();

        Option fetch = new Option( FETCH_CLIPS.getOpt(), null, FETCH_CLIPS.hasArg(), FETCH_CLIPS.getDesc() );
        fetch.setRequired( false );
        actionGroup.addOption( fetch );

        Option fetchTop = new Option( FETCH_TOP_CLIPS.getOpt(), null, FETCH_TOP_CLIPS.hasArg(),
                FETCH_TOP_CLIPS.getDesc() );
        fetchTop.setRequired( false );
        actionGroup.addOption( fetchTop );

        Option fetchDashboard = new Option( FETCH_DASHBOARD_CLIPS.getOpt(), null, FETCH_DASHBOARD_CLIPS.hasArg(),
                FETCH_DASHBOARD_CLIPS.getDesc() );
        fetchDashboard.setRequired( false );
        actionGroup.addOption( fetchDashboard );

        Option distinctFetched = new Option( DISTINCT_FETCHED_CLIPS.getOpt(), null, DISTINCT_FETCHED_CLIPS.hasArg(),
                DISTINCT_FETCHED_CLIPS.getDesc() );
        distinctFetched.setRequired( false );
        actionGroup.addOption( distinctFetched );

        Option download = new Option( DOWNLOAD_CLIPS.getOpt(), null, DOWNLOAD_CLIPS.hasArg(),
                DOWNLOAD_CLIPS.getDesc() );
        download.setRequired( false );
        actionGroup.addOption( download );

        Option delete = new Option( DELETE_CLIPS.getOpt(), null, DELETE_CLIPS.hasArg(), DELETE_CLIPS.getDesc() );
        delete.setRequired( false );
        actionGroup.addOption( delete );

        Option create = new Option( CREATE_CLIP.getOpt(), null, CREATE_CLIP.hasArg(), CREATE_CLIP.getDesc() );
        create.setRequired( false );
        actionGroup.addOption( create );

        Option utils = new Option( UTILS.getOpt(), null, UTILS.hasArg(), UTILS.getDesc() );
        utils.setRequired( false );
        actionGroup.addOption( utils );

        actionGroup.setRequired( true );
        m_actionOptions.addOptionGroup( actionGroup );
    }

    private void initFetchFilterOptions()
    {
        Option broadcasterId = new Option( BROADCASTER_ID.getOpt(), null, BROADCASTER_ID.hasArg(),
                BROADCASTER_ID.getDesc() );
        broadcasterId.setRequired( true );
        m_secOptions.addOption( broadcasterId );

        Option startedAt = new Option( STARTED_AT.getOpt(), null, STARTED_AT.hasArg(), STARTED_AT.getDesc() );
        startedAt.setRequired( false );
        m_secOptions.addOption( startedAt );

        Option endedAt = new Option( ENDED_AT.getOpt(), null, ENDED_AT.hasArg(), ENDED_AT.getDesc() );
        endedAt.setRequired( false );
        m_secOptions.addOption( endedAt );

        Option deltaPeriod = new Option( DELTA.getOpt(), null, DELTA.hasArg(), DELTA.getDesc() );
        deltaPeriod.setRequired( false );
        m_secOptions.addOption( deltaPeriod );

        Option chunkLimit = new Option( CHUNK_LIMIT.getOpt(), null, CHUNK_LIMIT.hasArg(), CHUNK_LIMIT.getDesc() );
        chunkLimit.setRequired( false );
        m_secOptions.addOption( chunkLimit );
    }

    private void initFetchTopFilterOptions()
    {
        Option broadcasterId = new Option( BROADCASTER_ID.getOpt(), null, BROADCASTER_ID.hasArg(),
                BROADCASTER_ID.getDesc() );
        broadcasterId.setRequired( true );
        m_secOptions.addOption( broadcasterId );

        Option chunkLimit = new Option( CHUNK_LIMIT.getOpt(), null, CHUNK_LIMIT.hasArg(), CHUNK_LIMIT.getDesc() );
        chunkLimit.setRequired( false );
        m_secOptions.addOption( chunkLimit );
    }

    private void initFetchDashboardFilterOptions()
    {
        Option userId = new Option( USER_ID.getOpt(), null, USER_ID.hasArg(),
                USER_ID.getDesc() );
        userId.setRequired( true );
        m_secOptions.addOption( userId );

        Option sortOrder = new Option( SORT_ORDER.getOpt(), null, SORT_ORDER.hasArg(), SORT_ORDER.getDesc() );
        sortOrder.setRequired( false );
        m_secOptions.addOption( sortOrder );

        Option chunkLimit = new Option( CHUNK_LIMIT.getOpt(), null, CHUNK_LIMIT.hasArg(), CHUNK_LIMIT.getDesc() );
        chunkLimit.setRequired( false );
        m_secOptions.addOption( chunkLimit );
    }

    private void initProceedFilterOptions()
    {
        Option minView = new Option( MIN_VIEW_COUNT.getOpt(), null, MIN_VIEW_COUNT.hasArg(), MIN_VIEW_COUNT.getDesc() );
        minView.setRequired( false );
        m_secOptions.addOption( minView );

        Option maxView = new Option( MAX_VIEW_COUNT.getOpt(), null, MAX_VIEW_COUNT.hasArg(), MAX_VIEW_COUNT.getDesc() );
        maxView.setRequired( false );
        m_secOptions.addOption( maxView );

        Option createdFrom = new Option( CREATED_AT_FROM.getOpt(), null, CREATED_AT_FROM.hasArg(),
                CREATED_AT_FROM.getDesc() );
        createdFrom.setRequired( false );
        m_secOptions.addOption( createdFrom );

        Option createdTo = new Option( CREATED_AT_TO.getOpt(), null, CREATED_AT_TO.hasArg(), CREATED_AT_TO.getDesc() );
        createdTo.setRequired( false );
        m_secOptions.addOption( createdTo );

        Option gameId = new Option( GAME_ID.getOpt(), null, GAME_ID.hasArg(), GAME_ID.getDesc() );
        gameId.setRequired( false );
        m_secOptions.addOption( gameId );

        Option vodId = new Option( VIDEO_ID.getOpt(), null, VIDEO_ID.hasArg(), VIDEO_ID.getDesc() );
        vodId.setRequired( false );
        m_secOptions.addOption( vodId );

        Option creatorId = new Option( CREATOR_ID.getOpt(), null, CREATOR_ID.hasArg(), CREATOR_ID.getDesc() );
        creatorId.setRequired( false );
        m_secOptions.addOption( creatorId );

        Option creatorName = new Option( CREATOR_NAME.getOpt(), null, CREATOR_NAME.hasArg(), CREATOR_NAME.getDesc() );
        creatorName.setRequired( false );
        m_secOptions.addOption( creatorName );

        Option title = new Option( TITLE.getOpt(), null, TITLE.hasArg(), TITLE.getDesc() );
        title.setRequired( false );
        m_secOptions.addOption( title );
    }

    private void initClientIdOption()
    {
        Option clientId = new Option( CLIENT_ID.getOpt(), null, CLIENT_ID.hasArg(), CLIENT_ID.getDesc() );
        clientId.setRequired( false );
        m_secOptions.addOption( clientId );
    }

    private void initClientSecretOption()
    {
        Option clientSecret = new Option( CLIENT_SECRET.getOpt(), null, CLIENT_SECRET.hasArg(),
                CLIENT_SECRET.getDesc() );
        clientSecret.setRequired( false );
        m_secOptions.addOption( clientSecret );
    }

    private void initClientAuthToken()
    {
        Option authToken = new Option( AUTH_TOKEN.getOpt(), null, AUTH_TOKEN.hasArg(), AUTH_TOKEN.getDesc() );
        authToken.setRequired( false );
        m_secOptions.addOption( authToken );
    }

    private void initRootDirOption()
    {
        Option rootDir = new Option( ROOT_DIR.getOpt(), null, ROOT_DIR.hasArg(), ROOT_DIR.getDesc() );
        rootDir.setRequired( false );
        m_secOptions.addOption( rootDir );
    }

    private void initAllowOverwriteOption()
    {
        Option allowOverwrite = new Option( ALLOW_OVERWRITE.getOpt(), null, ALLOW_OVERWRITE.hasArg(),
                ALLOW_OVERWRITE.getDesc() );
        allowOverwrite.setRequired( false );
        m_secOptions.addOption( allowOverwrite );
    }

    private void initNumPerTimeOption()
    {
        Option numPerTime = new Option( NUM_PER_TIME.getOpt(), null, NUM_PER_TIME.hasArg(), NUM_PER_TIME.getDesc() );
        numPerTime.setRequired( false );
        m_secOptions.addOption( numPerTime );
    }

    private void initFetchClipOptions()
    {
        Option fetch = new Option( FETCH_CLIPS.getOpt(), null, FETCH_CLIPS.hasArg(), FETCH_CLIPS.getDesc() );
        fetch.setRequired( true );
        m_secOptions.addOption( fetch );

        initAllowOverwriteOption();
        initClientIdOption();
        initClientSecretOption();
        initRootDirOption();
        initFetchFilterOptions();
    }

    private void initFetchTopClipOptions()
    {
        Option fetchTop = new Option( FETCH_TOP_CLIPS.getOpt(), null, FETCH_TOP_CLIPS.hasArg(),
                FETCH_TOP_CLIPS.getDesc() );
        fetchTop.setRequired( true );
        m_secOptions.addOption( fetchTop );

        initAllowOverwriteOption();
        initClientIdOption();
        initClientSecretOption();
        initRootDirOption();
        initFetchTopFilterOptions();
    }

    private void initFetchDashboardClipOptions()
    {
        Option fetchDashboard = new Option( FETCH_DASHBOARD_CLIPS.getOpt(), null, FETCH_DASHBOARD_CLIPS.hasArg(),
                FETCH_DASHBOARD_CLIPS.getDesc() );
        fetchDashboard.setRequired( true );
        m_secOptions.addOption( fetchDashboard );

        initAllowOverwriteOption();
        initClientIdOption();
        initClientSecretOption();
        initClientAuthToken();
        initRootDirOption();
        initFetchDashboardFilterOptions();
    }

    private void initDistinctFetchedClipOptions()
    {
        Option distinctFetched = new Option( DISTINCT_FETCHED_CLIPS.getOpt(), null, DISTINCT_FETCHED_CLIPS.hasArg(),
                DISTINCT_FETCHED_CLIPS.getDesc() );
        distinctFetched.setRequired( true );
        m_secOptions.addOption( distinctFetched );

        initAllowOverwriteOption();
        initRootDirOption();
    }

    private void initDownloadClipOptions()
    {
        Option download = new Option( DOWNLOAD_CLIPS.getOpt(), null, DOWNLOAD_CLIPS.hasArg(),
                DOWNLOAD_CLIPS.getDesc() );
        download.setRequired( true );
        m_secOptions.addOption( download );

        initAllowOverwriteOption();
        initRootDirOption();
        initProceedFilterOptions();
        initNumPerTimeOption();
    }

    private void initDeleteClipOptions()
    {
        Option delete = new Option( DELETE_CLIPS.getOpt(), null, DELETE_CLIPS.hasArg(), DELETE_CLIPS.getDesc() );
        delete.setRequired( true );
        m_secOptions.addOption( delete );

        initAllowOverwriteOption();
        initClientIdOption();
        initClientAuthToken();
        initRootDirOption();
        initProceedFilterOptions();
        initNumPerTimeOption();
    }

    private void initCreateClipOptions()
    {
        Option create = new Option( CREATE_CLIP.getOpt(), null, CREATE_CLIP.hasArg(), CREATE_CLIP.getDesc() );
        create.setRequired( true );
        m_secOptions.addOption( create );

        initAllowOverwriteOption();
        initClientIdOption();
        initClientSecretOption();
        initClientAuthToken();
        initRootDirOption();

        Option vodId = new Option( VIDEO_ID.getOpt(), null, VIDEO_ID.hasArg(), VIDEO_ID.getDesc() );
        vodId.setRequired( true );
        m_secOptions.addOption( vodId );

        Option title = new Option( TITLE.getOpt(), null, TITLE.hasArg(), TITLE.getDesc() );
        title.setRequired( false );
        m_secOptions.addOption( title );

        Option vodTime = new Option( VOD_TIME.getOpt(), null, VOD_TIME.hasArg(), VOD_TIME.getDesc() );
        vodTime.setRequired( true );
        m_secOptions.addOption( vodTime );

        Option clipDuration = new Option( CLIP_DURATION.getOpt(), null, CLIP_DURATION.hasArg(),
                CLIP_DURATION.getDesc() );
        clipDuration.setRequired( true );
        m_secOptions.addOption( clipDuration );
    }

    private void initUtilsOptions()
    {
        OptionGroup utilsGroup = new OptionGroup();

        Option utils = new Option( UTILS.getOpt(), null, UTILS.hasArg(), UTILS.getDesc() );
        utils.setRequired( true );
        m_secOptions.addOption( utils );

        initClientIdOption();
        initClientSecretOption();

        Option gameIdToName = new Option( GAME_ID_TO_NAME.getOpt(), null, GAME_ID_TO_NAME.hasArg(),
                GAME_ID_TO_NAME.getDesc() );
        gameIdToName.setRequired( false );
        utilsGroup.addOption( gameIdToName );

        Option gameNameToId = new Option( GAME_NAME_TO_ID.getOpt(), null, GAME_NAME_TO_ID.hasArg(),
                GAME_NAME_TO_ID.getDesc() );
        gameNameToId.setRequired( false );
        utilsGroup.addOption( gameNameToId );

        Option chIdToName = new Option( BROADCASTER_ID_TO_NAME.getOpt(), null, BROADCASTER_ID_TO_NAME.hasArg(),
                BROADCASTER_ID_TO_NAME.getDesc() );
        chIdToName.setRequired( false );
        utilsGroup.addOption( chIdToName );

        Option chNameToId = new Option( BROADCASTER_NAME_TO_ID.getOpt(), null, BROADCASTER_NAME_TO_ID.hasArg(),
                BROADCASTER_NAME_TO_ID.getDesc() );
        chNameToId.setRequired( false );
        utilsGroup.addOption( chNameToId );

        utilsGroup.setRequired( true );
        m_secOptions.addOptionGroup( utilsGroup );
    }

    public void run()
    {
        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();

        initActionGroupOptions();

        try
        {
            m_cmd = parser.parse( m_actionOptions, m_args, true );
            initSecondaryOptions();
            m_cmd = parser.parse( m_secOptions, m_args );
        }
        catch (ParseException e)
        {
            System.err.println( e.getMessage() );
            if (m_secOptions.getOptions().isEmpty())
            {
                formatter.printHelp( "twitchcm -[action]", m_actionOptions );
            }
            else
            {
                formatter.printHelp( "twitchcm -[action] ...", m_secOptions );
            }

            System.exit( 1 );
            return;
        }

        runAction();
    }
}
