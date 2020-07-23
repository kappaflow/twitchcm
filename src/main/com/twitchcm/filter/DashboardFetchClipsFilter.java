package main.com.twitchcm.filter;

import java.util.HashMap;

public class DashboardFetchClipsFilter implements FetchFilterInterface
{
    public enum DashboardClipsFilterEnum
    {
        USER_ID(        "curatorID" ),
        CHUNK_LIMIT(    "limit" ),  //1-99 def:20
        SORT(           "sort" ),   //"CREATED_AT_DESC"; "CREATED_AT_ASC"; "VIEWS_DESC"; "VIEWS_ASC".
        PERIOD(         "period" ); //ALL_TIME

        private final String m_jsonKey;

        DashboardClipsFilterEnum(String _jsonKey)
        {
            m_jsonKey = _jsonKey;
        }

        @Override
        public String toString()
        {
            return m_jsonKey;
        }
    }

    public enum SortClipsEnum
    {
        CREATED_AT_DESC(    "CREATED_AT_DESC" ),
        CREATED_AT_ASC(     "CREATED_AT_ASC" ),
        VIEWS_DESC(         "VIEWS_DESC" ),
        VIEWS_ASC(          "VIEWS_ASC" );

        private final String m_sortOrder;

        SortClipsEnum(String _sortOrder)
        {
            m_sortOrder = _sortOrder;
        }

        @Override
        public String toString()
        {
            return m_sortOrder;
        }

        public static SortClipsEnum getEnumByString(String _value)
        {
            for (SortClipsEnum e : SortClipsEnum.values())
            {
                if (e.toString().equals( _value ))
                    return e;
            }
            return null;
        }
    }

    public enum FetchPeriodEnum
    {
        ALL_TIME(       "ALL_TIME" ),
        LAST_MONTH(     "LAST_MONTH" ),
        LAST_WEEK(      "LAST_WEEK" ),
        LAST_DAY(       "LAST_DAY" );

        private final String m_fetchPeriod;

        FetchPeriodEnum(String _sortOrder)
        {
            m_fetchPeriod = _sortOrder;
        }

        @Override
        public String toString()
        {
            return m_fetchPeriod;
        }

        public static FetchPeriodEnum getEnumByString(String _value)
        {
            for (FetchPeriodEnum e : FetchPeriodEnum.values())
            {
                if (e.toString().equals( _value ))
                    return e;
            }
            return null;
        }
    }

    private String m_userId;
    private SortClipsEnum m_sortOrderEnum = SortClipsEnum.CREATED_AT_DESC;
    private FetchPeriodEnum m_periodEnum = FetchPeriodEnum.ALL_TIME;
    private Integer m_clipsChunk = 20;

    public DashboardFetchClipsFilter(String _userId)
    {
        if (_userId == null)
        {
            throw new NullPointerException("User id must be specified");
        }

        m_userId = _userId;
    }

    public void setSortOrder(SortClipsEnum _sortOrder)
    {
        m_sortOrderEnum = _sortOrder;
    }

    public void setClipsChunkLimit(int _clipsChunkLimit)
    {
        if (_clipsChunkLimit > 100 || _clipsChunkLimit < 1)
        {
            throw new RuntimeException( "clipsChunkLimit must be in 1-100 range" );
        }
        else
        {
            m_clipsChunk = _clipsChunkLimit;
        }
    }

    public void setFilter(String _sortOrderEnum, String _clipsChunk) throws NumberFormatException
    {
        if (_sortOrderEnum != null)
        {
            m_sortOrderEnum = SortClipsEnum.getEnumByString( _sortOrderEnum );

            if(m_sortOrderEnum == null)
            {
                throw new RuntimeException( _sortOrderEnum + " sorting order is not supported!" );
            }
        }
        if (_clipsChunk != null)
        {
            setClipsChunkLimit( Integer.parseInt( _clipsChunk ) );
        }
    }

    public String getUserId()
    {
        return m_userId;
    }

    public SortClipsEnum getSortOrder()
    {
        return m_sortOrderEnum;
    }

    public FetchPeriodEnum getPeriod()
    {
        return m_periodEnum;
    }

    public int getClipsChunk()
    {
        return m_clipsChunk;
    }

    public HashMap<String,Object> getMap()
    {
        HashMap<String,Object> map = new HashMap<>();

        map.put( DashboardClipsFilterEnum.USER_ID.toString(), m_userId );
        map.put( DashboardClipsFilterEnum.SORT.toString(), m_sortOrderEnum.toString() );
        map.put( DashboardClipsFilterEnum.PERIOD.toString(), m_periodEnum.toString() );
        map.put( DashboardClipsFilterEnum.CHUNK_LIMIT.toString(), m_clipsChunk );

        return map;
    }
}
