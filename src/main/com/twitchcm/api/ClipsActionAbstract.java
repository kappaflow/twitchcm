package main.com.twitchcm.api;

import main.com.twitchcm.clipslog.ClipsLogInterface;
import main.com.twitchcm.enums.AuthJsonEnum;
import main.com.twitchcm.enums.VerboseEnum;
import main.com.twitchcm.tool.TwitchWebConnection;

import javax.security.auth.login.LoginException;
import java.io.File;
import java.util.Map;

public abstract class ClipsActionAbstract
{
    protected static final VerboseEnum VERBOSE = VerboseEnum.DEBUG;

    protected Map<AuthJsonEnum,String> m_auth;
    protected ClipsLogInterface m_clipsLog;

    public void setAuth(Map<AuthJsonEnum,String> _auth)
    {
        try
        {
            m_auth = TwitchWebConnection.auth( _auth );
        }
        catch (LoginException e)
        {
            if (VerboseEnum.isShow( VerboseEnum.DEBUG, VERBOSE ))
                System.err.println( e.getMessage() );
        }
    }

    public void setRootDir(String _parentDir)
    {
        m_clipsLog.setRootDir( _parentDir );
    }

    public File getRootDir()
    {
        return m_clipsLog.getRootDir();
    }
}
