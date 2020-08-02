package main.com.twitchcm.tool;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Properties;

public final class ConfigReader
{
    public enum ConfigEnum
    {
        CLIENT_ID( "clientId" ),
        CLIENT_SECRET( "clientSecret" ),

        AUTH_TOKEN( "authToken" ); //from cookies

        private final String m_jsonKey;

        ConfigEnum(String _jsonKey)
        {
            m_jsonKey = _jsonKey;
        }

        @Override
        public String toString()
        {
            return m_jsonKey;
        }
    }

    private static final String PROP_FILE = "config.properties";
    private static final String EXT_PROP_FILE = System.getProperty( "user.dir" ) + "/" + PROP_FILE;

    private static String m_clientIdProp;
    private static String m_clientSecretProp;
    private static String m_authTokenProp;

    private static boolean m_is_read = false;

    private ConfigReader()
    {
        //singleton
    }

    private static void read()
    {
        readEmbedded();
        readExternal();

        m_is_read = true;
    }

    private static void readEmbedded()     //primary config
    {
        InputStream inputStream = null;

        try
        {
            Properties prop = new Properties();

            inputStream = ConfigReader.class.getClassLoader().getResourceAsStream( PROP_FILE );

            if (inputStream != null)
            {
                prop.load( inputStream );
            }
            else
            {
                throw new FileNotFoundException( "Property file '" + PROP_FILE + "' not found in the classpath" );
            }

            m_clientIdProp = prop.getProperty( ConfigEnum.CLIENT_ID.toString() );
            m_clientSecretProp = prop.getProperty( ConfigEnum.CLIENT_SECRET.toString() );
            m_authTokenProp = prop.getProperty( ConfigEnum.AUTH_TOKEN.toString() );
        }
        catch (Exception e)
        {
            System.out.println( "Exception: " + e );
        }
        finally
        {
            if (inputStream != null)
            {
                try
                {
                    inputStream.close();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }
    }

    private static void readExternal()
    {
        InputStream inputStream = null;

        try
        {
            Properties prop = new Properties();

            try
            {
                inputStream = new FileInputStream( EXT_PROP_FILE );
                prop.load( inputStream );
            }
            catch (FileNotFoundException e)
            {
                System.err.println( "Warning! Property file '" + EXT_PROP_FILE + "' not found" );
                return;
            }

            m_clientIdProp = Objects.isNull( m_clientIdProp )
                    ? prop.getProperty( ConfigEnum.CLIENT_ID.toString() ) : m_clientIdProp;
            m_clientSecretProp = Objects.isNull( m_clientSecretProp )
                    ? prop.getProperty( ConfigEnum.CLIENT_SECRET.toString() ) : m_clientSecretProp;
            m_authTokenProp = Objects.isNull( m_authTokenProp )
                    ? prop.getProperty( ConfigEnum.AUTH_TOKEN.toString() ) : m_authTokenProp;
        }
        catch (Exception e)
        {
            System.out.println( "Exception: " + e );
        }
        finally
        {
            if (inputStream != null)
            {
                try
                {
                    inputStream.close();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }
    }

    public static String getClientIdProp()
    {
        if (!m_is_read)
            read();

        return m_clientIdProp;
    }

    public static String getClientSecretProp()
    {
        if (!m_is_read)
            read();

        return m_clientSecretProp;
    }

    public static String getAuthTokenProp()
    {
        if (!m_is_read)
            read();

        return m_authTokenProp;
    }
}
