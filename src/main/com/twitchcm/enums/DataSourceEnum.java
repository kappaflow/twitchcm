package main.com.twitchcm.enums;

public enum DataSourceEnum
{
    FILES("Files"),
    DATABASE("Database");

    private final String m_jsonKey;

    DataSourceEnum(String _jsonKey) {
        m_jsonKey = _jsonKey;
    }

    @Override
    public String toString() {
        return m_jsonKey;
    }
}
