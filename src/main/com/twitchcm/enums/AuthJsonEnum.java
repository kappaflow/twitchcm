package main.com.twitchcm.enums;

public enum AuthJsonEnum
{
    CLIENT_ID( "clientId" ),
    CLIENT_SECRET( "client_secret" ),

    TOKEN_TYPE( "token_type" ), //for access_token
    ACCESS_TOKEN( "access_token" ), //used for api requests
    EXPIRES_IN( "expires_in" ),

    AUTH_TOKEN( "authToken" ); //from cookies

    private final String m_jsonKey;

    AuthJsonEnum(String _jsonKey)
    {
        m_jsonKey = _jsonKey;
    }

    @Override
    public String toString()
    {
        return m_jsonKey;
    }

    public static AuthJsonEnum getEnumByString(String _value)
    {
        for (AuthJsonEnum e : AuthJsonEnum.values())
        {
            if (e.toString().equals( _value ))
                return e;
        }
        return null;
    }
}
