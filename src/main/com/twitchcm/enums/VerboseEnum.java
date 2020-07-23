package main.com.twitchcm.enums;

public enum VerboseEnum
{
    SILENT("Silent", 0),
    NORMAL("Normal", 1),
    DEBUG("Debug", 2);

    private final String m_verboseName;
    private final int m_verboseLevel;

    VerboseEnum(String _verboseName, int _verboseLevel)
    {
        m_verboseLevel = _verboseLevel;
        m_verboseName = _verboseName;
    }

    public static boolean isShow(VerboseEnum _verbose, VerboseEnum _classVerbose)
    {
        return _classVerbose.getLvl() >= _verbose.getLvl();
    }

    public int getLvl() {
        return m_verboseLevel;
    }

    @Override
    public String toString() {
        return m_verboseName;
    }
}