package main.com.twitchcm.filter;

import main.com.twitchcm.enums.ClipsJsonEnum;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.Iterator;

public class ProceedClipsFilter
{

    private int m_minViewCount;
    private int m_maxViewCount;

    private ZonedDateTime m_createdAtFrom;
    private ZonedDateTime m_createdAtTo;

    private String m_gameId;
    private String m_videoId;
    private String m_creatorId;

    private String m_creatorName;
    private String m_title;

    public ProceedClipsFilter()
    {
        clear();
    }

    public JSONArray doFilter(JSONArray _jsonArr)
    {
        JSONArray filteredArr = new JSONArray();
        Iterator<JSONObject> iter = _jsonArr.iterator();
        JSONObject current;

        while (iter.hasNext())
        {
            current = iter.next();
            if (isSatisfy( current ))
            {
                filteredArr.add( current );
            }
        }

        return filteredArr;
    }

    public boolean isSatisfy(JSONObject _jsonObj)
    {
        return isSatisfyViewCount( Integer.parseInt( _jsonObj.get( ClipsJsonEnum.VIEWER_COUNT.getFetchedJsonKey() ).toString() ) )
                && isSatisfyCreatedAt( ZonedDateTime.parse( _jsonObj.get( ClipsJsonEnum.CREATED_AT.getFetchedJsonKey() ).toString() ) )
                && isSatisfyGameId( _jsonObj.get( ClipsJsonEnum.GAME_ID.getFetchedJsonKey() ).toString() )
                && isSatisfyVideoId( _jsonObj.get( ClipsJsonEnum.VIDEO_ID.getFetchedJsonKey() ).toString() )
                && isSatisfyCreatorId( _jsonObj.get( ClipsJsonEnum.CREATOR_ID.getFetchedJsonKey() ).toString() )
                && isSatisfyCreatorName( _jsonObj.get( ClipsJsonEnum.CREATOR_NAME.getFetchedJsonKey() ).toString() )
                && isSatisfyTitle( _jsonObj.get( ClipsJsonEnum.TITLE.getFetchedJsonKey() ).toString() );
    }

    private boolean isSatisfyViewCount(int _viewCount)
    {
        boolean minFlag = false;
        boolean maxFlag = false;

        if (m_minViewCount == -1 || _viewCount >= m_minViewCount)
        {
            minFlag = true;
        }

        if (m_maxViewCount == -1 || _viewCount <= m_maxViewCount)
        {
            maxFlag = true;
        }

        return minFlag && maxFlag;
    }

    public void setMinViewCount(int _viewCount)
    {
        if (_viewCount >= 0)
        {
            m_minViewCount = _viewCount;
        }
        else
        {
            System.err.println( "minViewCount must be >=0" );
        }
    }

    public void setMaxViewCount(int _viewCount)
    {
        if (_viewCount >= 0)
        {
            m_maxViewCount = _viewCount;
        }
        else
        {
            System.err.println( "maxViewCount must be >=0" );
        }
    }

    private boolean isSatisfyCreatedAt(ZonedDateTime _createdAt)
    {
        boolean fromFlag = false;
        boolean toFlag = false;

        if (m_createdAtFrom == null || _createdAt.isAfter( m_createdAtFrom ))
        {
            fromFlag = true;
        }

        if (m_createdAtTo == null || _createdAt.isBefore( m_createdAtTo ))
        {
            toFlag = true;
        }

        return fromFlag && toFlag;
    }

    public void setCreatedAtFrom(ZonedDateTime _createdAtFrom)
    {
        m_createdAtFrom = _createdAtFrom;
    }

    public void setCreatedAtTo(ZonedDateTime _createdAtTo)
    {
        m_createdAtTo = _createdAtTo;
    }

    private boolean isSatisfyGameId(String _gameId)
    {
        return m_gameId.equals( "" ) || _gameId.equals( m_gameId );
    }

    public void setGameId(String _gameId)
    {
        if (_gameId != null)
        {
            m_gameId = _gameId;
        }
        else
        {
            System.err.println( "gameId must be not null" );
        }
    }

    private boolean isSatisfyVideoId(String _videoId)
    {
        return m_videoId.equals( "" ) || _videoId.equals( m_videoId );
    }

    public void setVideoId(String _videoId)
    {
        if (_videoId != null)
        {
            m_videoId = _videoId;
        }
        else
        {
            System.err.println( "videoId must be not null" );
        }
    }

    private boolean isSatisfyCreatorId(String _creatorId)
    {
        return m_creatorId.equals( "" ) || _creatorId.equals( m_creatorId );
    }

    public void setCreatorId(String _creatorId)
    {
        if (_creatorId != null)
        {
            m_creatorId = _creatorId;
        }
        else
        {
            System.err.println( "creatorId must be not null" );
        }
    }

    private boolean isSatisfyCreatorName(String _creatorName)
    {
        return m_creatorName.equals( "" ) || _creatorName.equals( m_creatorName );
    }

    public void setCreatorName(String _creatorName)
    {
        if (_creatorName != null)
        {
            m_creatorName = _creatorName;
        }
        else
        {
            System.err.println( "creatorName must be not null" );
        }
    }

    private boolean isSatisfyTitle(String _title)
    {
        return m_title.equals( "" ) || _title.contains( m_title );
    }

    public void setTitle(String _title)
    {
        if (_title != null)
        {
            m_title = _title;
        }
        else
        {
            System.err.println( "title must be not null" );
        }
    }

    public void clear()
    {
        m_minViewCount = -1;
        m_maxViewCount = -1;

        m_createdAtFrom = null;
        m_createdAtTo = null;

        m_gameId = "";
        m_videoId = "";
        m_creatorId = "";

        m_creatorName = "";
        m_title = "";
    }

    public void setFilter(String _minViewCount, String _maxViewCount,
                          String _createdAtFrom, String _createdAtTo,
                          String _gameId, String _videoId, String _creatorId,
                          String _creatorName, String _title) throws DateTimeParseException, NumberFormatException
    {
        if (_minViewCount != null)
        {
            setMinViewCount( Integer.parseInt( _minViewCount ) );
        }
        if (_maxViewCount != null)
        {
            setMaxViewCount( Integer.parseInt( _maxViewCount ) );
        }
        if (_createdAtFrom != null)
        {
            setCreatedAtFrom( ZonedDateTime.parse( _createdAtFrom ) );
        }
        if (_createdAtTo != null)
        {
            setCreatedAtTo( ZonedDateTime.parse( _createdAtTo ) );
        }
        if (_gameId != null)
        {
            setGameId( _gameId );
        }
        if (_videoId != null)
        {
            setVideoId( _videoId );
        }
        if (_creatorId != null)
        {
            setCreatorId( _creatorId );
        }
        if (_creatorName != null)
        {
            setCreatorName( _creatorName );
        }
        if (_title != null)
        {
            setTitle( _title );
        }
    }
}
