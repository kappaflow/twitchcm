package main.com.twitchcm.filter;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.HashMap;

public class FetchClipsFilter implements FetchFilterInterface
{
    public enum PublicApiClipsFilterEnum
    {
        BROADCASTER_ID(     "broadcasterId" ),
        STARTED_AT(         "startedAt" ),
        ENDED_AT(           "endedAt" ),
        DELTA_PERIOD(       "deltaPeriod" ), //sub periods for fetching
        CLIPS_CHUNK_LIMIT(  "clipsChunkLimit" ); //1-100 def:20

        private final String m_jsonKey;

        PublicApiClipsFilterEnum(String _jsonKey)
        {
            m_jsonKey = _jsonKey;
        }

        @Override
        public String toString()
        {
            return m_jsonKey;
        }
    }

    private String          m_broadcasterId = "";
    private ZonedDateTime   m_startedAt = ZonedDateTime.of( LocalDateTime.now().minusWeeks( 1 ), ZoneOffset.UTC );
    private ZonedDateTime   m_endedAt = ZonedDateTime.of( LocalDateTime.now(), ZoneOffset.UTC );
    private Duration        m_deltaPeriod = Duration.ofDays( 1 );
    private Integer         m_clipsChunkLimit = 20;

    public FetchClipsFilter()
    {
    }

    public FetchClipsFilter(String _broadcasterId)
    {
        m_broadcasterId = _broadcasterId;
    }

    public String getBroadcasterId()
    {
        return m_broadcasterId;
    }

    public ZonedDateTime getStartedAt()
    {
        return m_startedAt;
    }

    public ZonedDateTime getEndedAt()
    {
        return m_endedAt;
    }

    public Duration getDeltaPeriod()
    {
        return m_deltaPeriod;
    }

    public Integer getClipsChunkLimit()
    {
        return m_clipsChunkLimit;
    }

    public HashMap<String,Object> getMap()
    {
        HashMap<String,Object> map = new HashMap<>();

        map.put( PublicApiClipsFilterEnum.BROADCASTER_ID.toString(), m_broadcasterId );
        map.put( PublicApiClipsFilterEnum.STARTED_AT.toString(), m_startedAt.toString() );
        map.put( PublicApiClipsFilterEnum.ENDED_AT.toString(), m_endedAt.toString() );
        map.put( PublicApiClipsFilterEnum.DELTA_PERIOD.toString(), m_deltaPeriod.toString() );
        map.put( PublicApiClipsFilterEnum.CLIPS_CHUNK_LIMIT.toString(), m_clipsChunkLimit );

        return map;
    }

    public void setClipsChunkLimit(Integer _clipsChunkLimit)
    {
        if (_clipsChunkLimit > 100 || _clipsChunkLimit < 1)
        {
            throw new RuntimeException( "clipsChunkLimit must be in 1-100 range" );
        }
        else
        {
            m_clipsChunkLimit = _clipsChunkLimit;
        }
    }

    public void setFilter(String _broadcasterId, ZonedDateTime _startedAt, ZonedDateTime _endedAt,
                          Duration _deltaPeriod, Integer _clipsChunkLimit)
    {
        m_broadcasterId = _broadcasterId;
        m_startedAt     = _startedAt;
        m_endedAt       = _endedAt;
        m_deltaPeriod   = _deltaPeriod;
        setClipsChunkLimit( _clipsChunkLimit );
    }

    public void setFilter(String _broadcasterId, ZonedDateTime _startedAt, ZonedDateTime _endedAt,
                          Duration _deltaPeriod)
    {
        m_broadcasterId = _broadcasterId;
        m_startedAt     = _startedAt;
        m_endedAt       = _endedAt;
        m_deltaPeriod   = _deltaPeriod;
    }

    public void setFilter(String _broadcasterId, ZonedDateTime _startedAt, ZonedDateTime _endedAt)
    {
        m_broadcasterId = _broadcasterId;
        m_startedAt     = _startedAt;
        m_endedAt       = _endedAt;
        m_deltaPeriod   = Duration.between( _startedAt, _endedAt );
    }

    public void setFilter(String _broadcasterId)
    {
        m_broadcasterId = _broadcasterId;
    }

    public void setFilter(String _broadcasterId,
                          String _startedAt,
                          String _endedAt,
                          String _deltaPeriod,
                          String _clipsChunkLimit) throws DateTimeParseException, NumberFormatException
    {
        if (_broadcasterId != null)
        {
            m_broadcasterId = _broadcasterId;
        }
        else
        {
            throw new RuntimeException( "broadcasterId must be set" );
        }
        if (_startedAt != null)
        {
            m_startedAt = ZonedDateTime.parse( _startedAt );
        }
        if (_endedAt != null)
        {
            m_endedAt = ZonedDateTime.parse( _endedAt );
        }
        if (_deltaPeriod != null)
        {
            m_deltaPeriod = Duration.parse( _deltaPeriod );
        }
        if (_clipsChunkLimit != null)
        {
            setClipsChunkLimit( Integer.parseInt( _clipsChunkLimit ) );
        }
    }
}
