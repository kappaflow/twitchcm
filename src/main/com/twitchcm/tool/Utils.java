package main.com.twitchcm.tool;

import main.com.twitchcm.enums.AuthJsonEnum;
import main.com.twitchcm.enums.VerboseEnum;
import org.asynchttpclient.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.security.auth.login.CredentialException;
import java.io.*;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class Utils
{

    private static final VerboseEnum VERBOSE = VerboseEnum.DEBUG;

    public static JSONObject parseJsonObj(String _jsonInline)
    {
        JSONObject jsonChunk = new JSONObject();
        JSONParser parse = new JSONParser();
        try
        {
            jsonChunk = (JSONObject) parse.parse( _jsonInline );
        } catch (ParseException e)
        {
            System.err.println( "Cant parse: " + _jsonInline );
            if (VerboseEnum.isShow( VerboseEnum.DEBUG, VERBOSE ))
                e.printStackTrace();
        }

        return jsonChunk;
    }

    public static JSONArray parseJsonArr(String _jsonInline)
    {
        JSONArray jsonChunk = new JSONArray();
        JSONParser parse = new JSONParser();
        try
        {
            jsonChunk = (JSONArray) parse.parse( _jsonInline );
        } catch (ParseException e)
        {
            System.err.println( "Cant parse: " + _jsonInline );
            if (VerboseEnum.isShow( VerboseEnum.DEBUG, VERBOSE ))
                e.printStackTrace();
        }

        return jsonChunk;
    }

    public static String readLastLine(File _file)
    {
        StringBuilder builder = new StringBuilder();
        RandomAccessFile randomAccessFile = null;

        String lastLine = null;

        try
        {
            randomAccessFile = new RandomAccessFile( _file, "r" );
            long fileLength = _file.length() - 1;
            // Set the pointer at the last of the file
            randomAccessFile.seek( fileLength );
            for (long pointer = fileLength; pointer >= 0; pointer--)
            {
                randomAccessFile.seek( pointer );
                char c;
                // read from the last one char at the time
                c = (char) randomAccessFile.read();
                // break when end of the line
                if (c == '\n')
                {
                    break;
                }
                builder.append( c );
            }
            // Since line is read from the last so it
            // is in reverse so use reverse method to make it right
            builder.reverse();
            lastLine = builder.toString();
        } catch (FileNotFoundException e)
        {
            System.err.println( "Cant find " + _file.getPath() );
            if (VerboseEnum.isShow( VerboseEnum.DEBUG, VERBOSE ))
                e.printStackTrace();

            lastLine = null;
        } catch (IOException e)
        {
            System.err.println( e.toString() );
            if (VerboseEnum.isShow( VerboseEnum.DEBUG, VERBOSE ))
                e.printStackTrace();
        } finally
        {
            if (randomAccessFile != null)
            {
                try
                {
                    randomAccessFile.close();
                } catch (IOException e)
                {
                    if (VerboseEnum.isShow( VerboseEnum.DEBUG, VERBOSE ))
                        e.printStackTrace();
                }
            }
        }
        return lastLine;
    }

    public static JSONObject getClipJson(String _slug, Map<AuthJsonEnum,String> _auth)
    {
        String targetURL = "https://api.twitch.tv/helix/clips";
        String urlParameters = "?id=" + _slug;
        JSONArray clipArray;
        JSONObject clipJson;
        String json_inline;

        try
        {
            json_inline = new TwitchWebConnection( targetURL + urlParameters, null, _auth, true ).doGetRequest();
        } catch (CredentialException e)
        {
            System.err.println( e.toString() );
            if (VerboseEnum.isShow( VerboseEnum.DEBUG, VERBOSE ))
                e.printStackTrace();

            return null;
        }

        clipArray = (JSONArray) Utils.parseJsonObj( json_inline ).get( "data" );

        if (!clipArray.isEmpty())
        {
            clipJson = (JSONObject) clipArray.get( 0 );
            return clipJson;
        }

        return null;
    }

    public static String getVodBroadcasterId(String _vodId, Map<AuthJsonEnum,String> _auth)
    {
        String targetURL = "https://api.twitch.tv/helix/videos";
        String urlParameters = "?id=" + _vodId;
        JSONArray dataArray;
        JSONObject userObj;
        String json_inline;

        try
        {
            json_inline = new TwitchWebConnection( targetURL + urlParameters, null, _auth, true ).doGetRequest();
        } catch (CredentialException e)
        {
            System.err.println( e.toString() );
            if (VerboseEnum.isShow( VerboseEnum.DEBUG, VERBOSE ))
                e.printStackTrace();

            return null;
        }

        try
        {
            dataArray = (JSONArray) Utils.parseJsonObj( json_inline ).get( "data" );
            userObj = (JSONObject) dataArray.get( 0 );
        } catch (IndexOutOfBoundsException | NullPointerException | ClassCastException e)
        {
            System.err.println( "Can't get broadcaster id!" );
            if (VerboseEnum.isShow( VerboseEnum.DEBUG, VERBOSE ))
                e.printStackTrace();

            return null;
        }

        return userObj.get( "user_id" ).toString();
    }

    public static String getBroadcasterId(String _broadcasterLogin, Map<AuthJsonEnum,String> _auth)
    {
        String targetURL = "https://api.twitch.tv/helix/users";
        String urlParameters = "?login=" + _broadcasterLogin;
        JSONArray data_array;
        JSONObject user_object;
        String json_inline;

        try
        {
            json_inline = new TwitchWebConnection( targetURL + urlParameters, null, _auth, true ).doGetRequest();
        } catch (CredentialException e)
        {
            System.err.println( e.toString() );
            if (VerboseEnum.isShow( VerboseEnum.DEBUG, VERBOSE ))
                e.printStackTrace();
            return null;
        }
        try
        {
            data_array = (JSONArray) Utils.parseJsonObj( json_inline ).get( "data" );

            user_object = (JSONObject) data_array.get( 0 );
        } catch (IndexOutOfBoundsException | NullPointerException | ClassCastException e)
        {
            System.err.println( "Can't get broadcaster id!" );
            if (VerboseEnum.isShow( VerboseEnum.DEBUG, VERBOSE ))
                e.printStackTrace();

            return null;
        }

        return user_object.get( "id" ).toString();
    }

    public static String getBroadcasterLogin(String _broadcasterId, Map<AuthJsonEnum,String> _auth)
    {
        String targetURL = "https://api.twitch.tv/helix/users";
        String urlParameters = "?id=" + _broadcasterId;
        JSONArray data_array;
        JSONObject user_object;
        String json_inline;

        try
        {
            json_inline = new TwitchWebConnection( targetURL + urlParameters, null, _auth, true ).doGetRequest();
        } catch (CredentialException e)
        {
            System.err.println( e.toString() );
            if (VerboseEnum.isShow( VerboseEnum.DEBUG, VERBOSE ))
                e.printStackTrace();

            return null;
        }

        try
        {
            data_array = (JSONArray) Utils.parseJsonObj( json_inline ).get( "data" );
            user_object = (JSONObject) data_array.get( 0 );
        } catch (IndexOutOfBoundsException | NullPointerException | ClassCastException e)
        {
            System.err.println( "Can't get broadcaster login!" );
            if (VerboseEnum.isShow( VerboseEnum.DEBUG, VERBOSE ))
                e.printStackTrace();

            return null;
        }

        return user_object.get( "login" ).toString();
    }

    public static String getGameId(String _gameName, Map<AuthJsonEnum,String> _auth)
    {
        String targetURL = "https://api.twitch.tv/helix/games";
        String urlParameters = "?name=" + _gameName;
        JSONArray data_array;
        JSONObject user_object;
        String json_inline;

        try
        {
            json_inline = new TwitchWebConnection( targetURL + urlParameters, null, _auth, true ).doGetRequest();
        } catch (CredentialException e)
        {
            System.err.println( e.toString() );
            if (VerboseEnum.isShow( VerboseEnum.DEBUG, VERBOSE ))
                e.printStackTrace();

            return null;
        }

        try
        {
            data_array = (JSONArray) Utils.parseJsonObj( json_inline ).get( "data" );
            user_object = (JSONObject) data_array.get( 0 );
        } catch (IndexOutOfBoundsException | NullPointerException | ClassCastException e)
        {
            System.err.println( "Can't get game id!" );
            if (VerboseEnum.isShow( VerboseEnum.DEBUG, VERBOSE ))
                e.printStackTrace();

            return null;
        }

        return user_object.get( "id" ).toString();
    }

    public static String getGameName(String _gameId, Map<AuthJsonEnum,String> _auth)
    {
        String targetURL = "https://api.twitch.tv/helix/games";
        String urlParameters = "?id=" + _gameId;
        JSONArray data_array;
        JSONObject user_object;
        String json_inline;

        try
        {
            json_inline = new TwitchWebConnection( targetURL + urlParameters, null, _auth, true ).doGetRequest();
        } catch (CredentialException e)
        {
            System.err.println( e.toString() );
            if (VerboseEnum.isShow( VerboseEnum.DEBUG, VERBOSE ))
                e.printStackTrace();

            return null;
        }

        try
        {
            data_array = (JSONArray) Utils.parseJsonObj( json_inline ).get( "data" );
            user_object = (JSONObject) data_array.get( 0 );
        } catch (IndexOutOfBoundsException | NullPointerException | ClassCastException e)
        {
            System.err.println( "Can't get game name!" );
            if (VerboseEnum.isShow( VerboseEnum.DEBUG, VERBOSE ))
                e.printStackTrace();

            return null;
        }

        return user_object.get( "name" ).toString();
    }

    public static void removeLastNewLine(File _file)
    {
        RandomAccessFile f = null;
        try
        {
            RandomAccessFile randomAccessFile = new RandomAccessFile( _file, "rw" );
            byte b;
            long length = randomAccessFile.length();
            if (length != 0)
            {
                do
                {
                    length -= 1;
                    randomAccessFile.seek( length );
                    b = randomAccessFile.readByte();
                } while (b != 10 && length > 0);
                randomAccessFile.setLength( length );
                randomAccessFile.close();
            }
        }
        catch (IOException e)
        {
            System.err.println( "Cant find " + _file.getPath() );
            if (VerboseEnum.isShow( VerboseEnum.DEBUG, VERBOSE ))
                e.printStackTrace();
        }
        finally
        {
            try
            {
                if (f != null)
                {
                    f.close();
                }
            } catch (IOException e)
            {
                if (VerboseEnum.isShow( VerboseEnum.DEBUG, VERBOSE ))
                    e.printStackTrace();
            }
        }
    }

    public static String parseClipDownloadURL(String _thumbnailURL)
    {
        return _thumbnailURL.substring( 0, _thumbnailURL.indexOf( "-preview" ) ) + ".mp4";
    }

    public static void downloadWithAHC(String url, String localFilename) throws ExecutionException,
            InterruptedException, IOException
    {
        FileOutputStream stream = new FileOutputStream( localFilename );
        AsyncHttpClient client = Dsl.asyncHttpClient();

        client.prepareGet( url )
                .execute( new AsyncCompletionHandler<FileOutputStream>()
                {

                    @Override
                    public State onBodyPartReceived(HttpResponseBodyPart bodyPart) throws Exception
                    {
                        stream.getChannel()
                                .write( bodyPart.getBodyByteBuffer() );
                        return State.CONTINUE;
                    }

                    @Override
                    public FileOutputStream onCompleted(Response response)
                    {
                        return stream;
                    }
                } )
                .get();

        stream.getChannel().close();
        client.close();
    }
}
