package main.com.twitchcm.tool;

import main.com.twitchcm.ClipsManager;
import main.com.twitchcm.enums.AuthJsonEnum;
import main.com.twitchcm.enums.VerboseEnum;
import org.json.simple.JSONObject;

import javax.security.auth.login.CredentialException;
import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

//For Twitch API and user requests
public class TwitchWebConnection
{
    private static final VerboseEnum VERBOSE = VerboseEnum.DEBUG;

    private static final int REQUEST_LIMIT_ERR = 3;

    private static final int REQUEST_LIMIT_ERR_500 = 50;
    private static final int SLEEP_MS_ERR_500 = 100;

    private static final String GET = "GET";
    private static final String POST = "POST";

    private int m_reqAttemptCounterErr500 = 0;
    private int m_reqAttemptCounter = 0;

    private static final String AUTHORIZATION = "Authorization";
    private static final String CLIENT_ID = "Client-ID";
    private static final String OAUTH_TOKEN_TYPE = "OAuth";

    private URL m_url;
    private final Map<String,String> m_requestProperty;

    public TwitchWebConnection(String _url, HashMap<String,String> _requestProperty)
    {
        try
        {
            m_url = new URL( _url );
        } catch (MalformedURLException e)
        {
            System.err.println( e.toString() );
            if (VerboseEnum.isShow( VerboseEnum.DEBUG, VERBOSE ))
                e.printStackTrace();
        }

        if (_requestProperty != null)
        {
            m_requestProperty = _requestProperty;
        }
        else
        {
            m_requestProperty = new HashMap<>();
        }
    }

    public TwitchWebConnection(String _url, HashMap<String,String> _requestProperty, Map<AuthJsonEnum,String> _auth,
                               boolean isApiAuth) throws CredentialException
    {
        try
        {
            m_url = new URL( _url );
        } catch (MalformedURLException e)
        {
            System.err.println( e.toString() );
            if (VerboseEnum.isShow( VerboseEnum.DEBUG, VERBOSE ))
                e.printStackTrace();
        }

        if (_requestProperty != null)
        {
            m_requestProperty = _requestProperty;
        }
        else
        {
            m_requestProperty = new HashMap<>();
        }

        try
        {
            m_requestProperty.put( CLIENT_ID, _auth.get( AuthJsonEnum.CLIENT_ID ) );

            if (isApiAuth)
            {
                m_requestProperty.put( AUTHORIZATION,
                        _auth.get( AuthJsonEnum.TOKEN_TYPE ) + " " + _auth.get( AuthJsonEnum.ACCESS_TOKEN ) );
            }
            else
            {
                m_requestProperty.put( AUTHORIZATION, OAUTH_TOKEN_TYPE + " " + _auth.get( AuthJsonEnum.AUTH_TOKEN ) );
            }
        } catch (NullPointerException e)
        {
            if (VerboseEnum.isShow( VerboseEnum.DEBUG, VERBOSE ))
                e.printStackTrace();
            throw new CredentialException( "Authentication is not set!" );
        }
    }

    public String doGetRequest()
    {
        StringBuilder responseInline = new StringBuilder();
        HttpURLConnection connection = null;
        Scanner sc = null;

        try
        {
            connection = (HttpURLConnection) m_url.openConnection();

            /* for random error response testing
            if (new Random().nextInt(1) == 0) {
                connection = (HttpURLConnection) new URL( "https://httpstat.us/500").openConnection();
            } else
            {
                connection = (HttpURLConnection) m_url.openConnection();
            }
            */

            for (Map.Entry<String,String> entry : m_requestProperty.entrySet())
            {
                connection.setRequestProperty( entry.getKey(), entry.getValue() );
            }

            connection.setRequestMethod( GET );
            connection.setUseCaches( false );

            connection.connect();

            int responseCode = connection.getResponseCode();

            if (VerboseEnum.isShow( VerboseEnum.DEBUG, VERBOSE ))
                System.out.println( "Response code: " + responseCode );

            try
            {
                sc = new Scanner( connection.getInputStream() );
                while (sc.hasNext())
                {
                    responseInline.append( sc.nextLine() );
                }

                if (VerboseEnum.isShow( VerboseEnum.DEBUG, VERBOSE ))
                    System.out.println( "Response text:\n" + responseInline );

            } catch (IOException e)
            {
                System.err.println( e.toString() );

                // This means that an error response occurred, read the error from the ErrorStream
                sc = new Scanner( connection.getErrorStream() );
                while (sc.hasNext())
                {
                    responseInline.append( sc.nextLine() );
                }

                if (VerboseEnum.isShow( VerboseEnum.DEBUG, VERBOSE ))
                    System.err.println( "Response text:\n" + responseInline );

                responseInline = new StringBuilder( processError( responseCode, responseInline.toString(), GET, "" ) );
            } finally
            {
                if (sc != null)
                {
                    sc.close();
                }
            }
        } catch (Exception e)
        {
            if (VerboseEnum.isShow( VerboseEnum.DEBUG, VERBOSE ))
                e.printStackTrace();
        } finally
        {
            if (connection != null)
            {
                connection.disconnect();
            }
        }

        return responseInline.toString();
    }

    public String doPostRequest(String _payload)
    {
        StringBuilder responseInline = new StringBuilder();
        HttpURLConnection connection = null;
        Scanner sc = null;

        try
        {
            connection = (HttpURLConnection) m_url.openConnection();

            for (Map.Entry<String,String> entry : m_requestProperty.entrySet())
            {
                connection.setRequestProperty( entry.getKey(), entry.getValue() );
            }

            connection.setRequestMethod( POST );

            connection.setUseCaches( false );
            connection.setDoOutput( true );

            OutputStream os = connection.getOutputStream();
            byte[] input = _payload.getBytes( StandardCharsets.UTF_8 );
            os.write( input, 0, input.length );

            int responseCode = connection.getResponseCode();

            if (VerboseEnum.isShow( VerboseEnum.DEBUG, VERBOSE ))
                System.out.println( "Request body: " + _payload );

            if (VerboseEnum.isShow( VerboseEnum.DEBUG, VERBOSE ))
                System.out.println( "Response code: " + responseCode );

            try
            {
                sc = new Scanner( connection.getInputStream() );
                while (sc.hasNext())
                {
                    responseInline.append( sc.nextLine() );
                }

                if (VerboseEnum.isShow( VerboseEnum.DEBUG, VERBOSE ))
                    System.out.println( "Response text:\n" + responseInline );

            } catch (IOException e)
            {
                System.err.println( e.toString() );

                // This means that an error response occurred, read the error from the ErrorStream
                sc = new Scanner( connection.getErrorStream() );
                while (sc.hasNext())
                {
                    responseInline.append( sc.nextLine() );
                }

                if (VerboseEnum.isShow( VerboseEnum.DEBUG, VERBOSE ))
                    System.err.println( "Response text:\n" + responseInline );

                responseInline = new StringBuilder( processError( responseCode, responseInline.toString(), POST,
                        _payload ) );

            } finally
            {
                if (sc != null)
                {
                    sc.close();
                }
            }
        } catch (Exception e)
        {
            if (VerboseEnum.isShow( VerboseEnum.DEBUG, VERBOSE ))
                e.printStackTrace();
        } finally
        {
            if (connection != null)
            {
                connection.disconnect();
            }
        }

        return responseInline.toString();
    }

    public static Map<AuthJsonEnum,String> auth(Map<AuthJsonEnum,String> _authCred) throws LoginException
    {
        String targetURL = "https://id.twitch.tv/oauth2/token";
        String urlParameters = String.format( "?client_id=%s&client_secret=%s&grant_type=client_credentials",
                _authCred.get( AuthJsonEnum.CLIENT_ID ), _authCred.get( AuthJsonEnum.CLIENT_SECRET ) );

        String json_inline = new TwitchWebConnection( targetURL + urlParameters, null ).doPostRequest( "" );

        try
        {
            JSONObject jsonObject = Utils.parseJsonObj( json_inline );
            jsonObject.forEach( (k, v) -> _authCred.put( AuthJsonEnum.getEnumByString( k.toString() ), v.toString() ) );
        } catch (NullPointerException e)
        {
            if (VerboseEnum.isShow( VerboseEnum.DEBUG, VERBOSE ))
                e.printStackTrace();
            throw new CredentialException( "Authentication is not set!" );
        }

        if (!_authCred.containsKey( AuthJsonEnum.ACCESS_TOKEN ))
        {
            throw new LoginException( "Cant authenticate with the provided credentials!" );
        }
        _authCred.replace( AuthJsonEnum.TOKEN_TYPE,
                _authCred.get( AuthJsonEnum.TOKEN_TYPE ).substring( 0, 1 ).toUpperCase()
                        + _authCred.get( AuthJsonEnum.TOKEN_TYPE ).substring( 1 ) );

        if (VerboseEnum.isShow( VerboseEnum.DEBUG, VERBOSE ))
            System.out.println( _authCred.toString() );

        return _authCred;
    }

    private String processError(int _responseCode, String _responseInline, String _responseMethod, String _payload)
    {
        if (_responseCode == 500)
        {
            return processCode500( _responseMethod, _payload );
        }
        else if (_responseCode == 401)
        {
            return processCode401( _responseMethod, _payload );
        }

        System.err.println( "Cant process error response!" );

        return _responseInline;
    }

    private String processCode500(String _method, String _payload)
    {
        String responseInline = "";

        try
        {
            Thread.sleep( SLEEP_MS_ERR_500 );
        } catch (InterruptedException e)
        {
            System.err.println( e.toString() );
            if (VerboseEnum.isShow( VerboseEnum.DEBUG, VERBOSE ))
                e.printStackTrace();
        }

        m_reqAttemptCounterErr500++;
        if (VerboseEnum.isShow( VerboseEnum.DEBUG, VERBOSE ))
            System.err.println( "Error 500. Attempt # " + m_reqAttemptCounterErr500 );

        if (m_reqAttemptCounterErr500 >= REQUEST_LIMIT_ERR_500)
        {
            throw new RuntimeException( "\n Error 500 request limit " + REQUEST_LIMIT_ERR_500
                    + " reached \nError Response text: " + responseInline );
        }

        if (_method.equals( POST ))
        {
            responseInline = doPostRequest( _payload );
        }
        else if (_method.equals( GET ))
        {
            responseInline = doGetRequest();
        }
        else
        {
            throw new RuntimeException( "Incorrect method to process: " + _method );
        }

        return responseInline;
    }

    private String processCode401(String _method, String _payload)
    {
        String responseInline = "";

        m_reqAttemptCounter++;
        if (VerboseEnum.isShow( VerboseEnum.DEBUG, VERBOSE ))
            System.err.println( "Error 401. Attempt # " + m_reqAttemptCounter );

        if (m_reqAttemptCounter >= REQUEST_LIMIT_ERR)
        {
            throw new RuntimeException( "\n Request limit " + REQUEST_LIMIT_ERR
                    + " reached \nError Response text: " + responseInline );
        }

        Map<AuthJsonEnum,String> authCred = ClipsManager.getAuthCredential();

        if (!authCred.isEmpty())
        {
            try
            {
                auth( authCred );
            } catch (LoginException e)
            {
                System.err.println( e.toString() );
                if (VerboseEnum.isShow( VerboseEnum.DEBUG, VERBOSE ))
                    e.printStackTrace();
            }
            m_requestProperty.put( CLIENT_ID, authCred.get( AuthJsonEnum.CLIENT_ID ) );
            m_requestProperty.put( AUTHORIZATION,
                    authCred.get( AuthJsonEnum.TOKEN_TYPE ) + " " + authCred.get( AuthJsonEnum.ACCESS_TOKEN ) );
        }
        else
        {
            throw new RuntimeException( "Auth credential isn't set" );
        }

        if (_method.equals( POST ))
        {
            responseInline = doPostRequest( _payload );
        }
        else if (_method.equals( GET ))
        {
            responseInline = doGetRequest();
        }
        else
        {
            throw new RuntimeException( "Incorrect method to process: " + _method );
        }

        m_reqAttemptCounter = 0;

        return responseInline;
    }

}
