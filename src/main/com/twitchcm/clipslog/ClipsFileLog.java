package main.com.twitchcm.clipslog;

import main.com.twitchcm.enums.ClipsJsonEnum;
import main.com.twitchcm.enums.VerboseEnum;
import main.com.twitchcm.tool.Utils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public abstract class ClipsFileLog extends ClipsLog implements ClipsLogInterface
{
    protected static final VerboseEnum VERBOSE = VerboseEnum.DEBUG;

    protected static final String DEFAULT_DIR_PATH = System.getProperty( "user.dir" );
    protected static final String DEFAULT_DIR_NAME = "outputDir";

    protected static final String FETCHED_CLIPS_FILE = "fetchedClips.txt";

    protected boolean m_allowOverwrite;

    protected File m_rootDir;
    protected File m_outputDir;

    protected File m_fetchedFile;

    public ClipsFileLog(boolean _allowOverwrite)
    {
        setRootDir( DEFAULT_DIR_PATH );
        if (_allowOverwrite)
        {
            clean();
        }
    }

    protected ClipsFileLog()
    {
    }

    public void setRootDir(String _parentDir)
    {
        m_rootDir = new File( _parentDir );
        m_outputDir = new File( m_rootDir.getPath() + "/" + DEFAULT_DIR_NAME );

        m_fetchedFile = new File( m_rootDir.getPath() + "/" + DEFAULT_DIR_NAME + "/" + FETCHED_CLIPS_FILE );
    }

    public File getRootDir()
    {
        return m_rootDir;
    }

    public File getOutputDir()
    {
        return m_outputDir;
    }

    public void write(JSONObject _jsonObj, File _file)
    {
        BufferedWriter bufferedWriter = null;
        try
        {
            FileOutputStream outputStream = new FileOutputStream( _file, true );
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter( outputStream, StandardCharsets.UTF_8 );
            bufferedWriter = new BufferedWriter( outputStreamWriter );
            if (!_jsonObj.isEmpty())
            {
                if (_file.length() != 0)
                {
                    bufferedWriter.write( "\n" );
                }
                _jsonObj.writeJSONString( bufferedWriter );
            }
        } catch (IOException | SecurityException e)
        {
            System.err.println( "Failed to write to " + _file.getName() + " !" );
            if (VerboseEnum.isShow( VerboseEnum.DEBUG, VERBOSE ))
                e.printStackTrace();
        } finally
        {
            try
            {
                if (bufferedWriter != null)
                {
                    bufferedWriter.close();
                }
            } catch (IOException e)
            {
                System.err.println( "Failed to close!" );
                if (VerboseEnum.isShow( VerboseEnum.DEBUG, VERBOSE ))
                    e.printStackTrace();
            }
        }
    }

    public void write(JSONArray _jsonArr, File _file)
    {
        JSONObject jsonObject;
        BufferedWriter bufferedWriter = null;
        try
        {
            FileOutputStream outputStream = new FileOutputStream( _file, true );
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter( outputStream, StandardCharsets.UTF_8 );
            bufferedWriter = new BufferedWriter( outputStreamWriter );

            for (int i = 0; i < _jsonArr.size(); i++)
            {
                jsonObject = (JSONObject) _jsonArr.get( i );
                if (!jsonObject.isEmpty())
                {
                    if (_file.length() != 0)
                    {
                        bufferedWriter.write( "\n" );
                        jsonObject.writeJSONString( bufferedWriter );
                    }
                    else
                    {
                        jsonObject.writeJSONString( bufferedWriter );
                        bufferedWriter.flush();
                    }
                }
            }
        } catch (IOException e)
        {
            System.err.println( "Failed to write to " + _file.getName() + "!" );
            if (VerboseEnum.isShow( VerboseEnum.DEBUG, VERBOSE ))
                e.printStackTrace();
        } finally
        {
            try
            {
                if (bufferedWriter != null)
                {
                    bufferedWriter.close();
                }
            } catch (IOException e)
            {
                System.err.println( "Failed to close!" );
                if (VerboseEnum.isShow( VerboseEnum.DEBUG, VERBOSE ))
                    e.printStackTrace();
            }
        }
    }

    public void write(String _text, File _file)
    {
        BufferedWriter bufferedWriter = null;
        try
        {
            FileOutputStream outputStream = new FileOutputStream( _file, true );
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter( outputStream, StandardCharsets.UTF_8 );
            bufferedWriter = new BufferedWriter( outputStreamWriter );

            if (_file.length() != 0)
            {
                bufferedWriter.write( "\n" );
            }
            bufferedWriter.write( _text );

        } catch (IOException e)
        {
            System.err.println( "Failed to write to " + _file.getName() + " !" );
            if (VerboseEnum.isShow( VerboseEnum.DEBUG, VERBOSE ))
                e.printStackTrace();
        } finally
        {
            try
            {
                if (bufferedWriter != null)
                {
                    bufferedWriter.close();
                }
            } catch (IOException e)
            {
                System.err.println( "Failed to close!" );
                if (VerboseEnum.isShow( VerboseEnum.DEBUG, VERBOSE ))
                    e.printStackTrace();
            }
        }
    }

    public void distinctClips(File _sourceFile, File _destFile, boolean isSkipHeader)
    {
        Map<String,JSONObject> distinctMap = new LinkedHashMap<>();
        JSONArray distinctArray = new JSONArray();
        Scanner sourceSc = null;
        JSONObject jsonObj;
        int sourceCounter = 0;

        if (!_sourceFile.exists() || _sourceFile.length() == 0)
        {
            throw new RuntimeException( "The " + _sourceFile.getName() + " empty or doesnt exist!" );
        }

        try
        {
            FileInputStream fis = new FileInputStream( _sourceFile );
            sourceSc = new Scanner( fis );

            while (sourceSc.hasNextLine())
            {
                if (sourceCounter == 0 && isSkipHeader)
                {
                    sourceSc.nextLine();
                }
                jsonObj = Utils.parseJsonObj( sourceSc.nextLine() );
                distinctMap.put( jsonObj.get( ClipsJsonEnum.ID.getFetchedJsonKey() ).toString(), jsonObj );
                sourceCounter++;
            }

        } catch (IOException e)
        {
            System.err.println( "Cant read from the file:\n" + _sourceFile.getPath() );
            if (VerboseEnum.isShow( VerboseEnum.DEBUG, VERBOSE ))
                e.printStackTrace();
        }
        if (sourceSc != null)
        {
            sourceSc.close();
        }

        if (!distinctMap.isEmpty())
        {
            distinctArray.addAll( distinctMap.values() );
            write( distinctArray, _destFile );
        }

        System.out.println( "Source entries: " + sourceCounter );
        System.out.println( "Destination entries: " + distinctArray.size() );
    }

    public Scanner getLastSourceLineToRead(File _destFile, File _sourceFile, String _searchKey)
    {
        Scanner sourceSc = null;

        if (!_sourceFile.exists() || _sourceFile.length() == 0)
        {
            if (VerboseEnum.isShow( VerboseEnum.DEBUG, VERBOSE ))
                System.err.println( "The " + _sourceFile.getName() + " empty or doesnt exist!" );
            return null;
        }

        if (_destFile.exists() && _destFile.length() != 0)
        {
            sourceSc = getSourceLineMatchFromDest( _destFile, _sourceFile, _searchKey );

            if (sourceSc.hasNextLine())
            {
                sourceSc.nextLine(); //skip the last proceed line
            }
        }
        else
        {
            try
            {
                FileInputStream fis = new FileInputStream( _sourceFile );
                sourceSc = new Scanner( fis );
                sourceSc.nextLine(); //skip header

            } catch (IOException e)
            {
                System.err.println( "Cant open " + _sourceFile.getName() + " to read header" );
                if (VerboseEnum.isShow( VerboseEnum.DEBUG, VERBOSE ))
                    e.printStackTrace();
            }
        }

        return sourceSc;
    }

    private Scanner getSourceLineMatchFromDest(File _destFile, File _sourceFile, String _jsonKey)
    {
        String lastOutputLine;
        String searchStr;
        JSONObject lastOutputLineJSON;

        lastOutputLine = Utils.readLastLine( _destFile );
        lastOutputLineJSON = Utils.parseJsonObj( lastOutputLine );
        searchStr = lastOutputLineJSON.get( _jsonKey ).toString();

        Scanner sourceSc = null;

        try
        {
            FileInputStream fis = new FileInputStream( _sourceFile );
            sourceSc = new Scanner( fis );

            while (sourceSc.hasNextLine())
            {
                if (sourceSc.nextLine().contains( searchStr ))
                {
                    break;
                }
            }
        } catch (IOException e)
        {
            System.err.println( "Cant open " + _sourceFile.getName() + " to read header" );
            if (VerboseEnum.isShow( VerboseEnum.DEBUG, VERBOSE ))
                e.printStackTrace();
        }

        return sourceSc;
    }

    @Override
    public void init() throws SecurityException
    {
        if (m_allowOverwrite)
        {
            clean();
        }

        if (!m_outputDir.exists())
        {
            m_outputDir.mkdirs();
        }
    }

    public JSONObject getLastRecord(File _file)
    {
        return Utils.parseJsonObj( Utils.readLastLine( _file ) );
    }

    @Override
    public boolean isContinue()
    {
        return false;
    }

    public boolean isContinue(File _file)
    {
        return _file.exists() && _file.length() != 0;
    }

    @Override
    public void clean()
    {
        if (m_outputDir.exists())
        {
            try
            {
                m_outputDir.delete();
            } catch (SecurityException e)
            {
                System.err.println( "Cant delete " + m_outputDir.getPath() );
                if (VerboseEnum.isShow( VerboseEnum.DEBUG, VERBOSE ))
                    e.printStackTrace();
            }
        }
    }
}
