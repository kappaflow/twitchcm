package main.com.twitchcm.clipslog;

import main.com.twitchcm.enums.VerboseEnum;
import main.com.twitchcm.filter.FetchFilterInterface;
import main.com.twitchcm.tool.Utils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;

public class FetchFileLog extends ClipsFileLog
{
    private static final String FETCHED_DISTINCT_CLIPS_FILE = "fetchedClipsDistinct.txt";

    public FetchFileLog(boolean _allowOverwrite)
    {
        setRootDir( DEFAULT_DIR_PATH );

        m_allowOverwrite = _allowOverwrite;
    }

    @Override
    public void distinct()
    {
        File fetchedDistinct = new File( m_fetchedFile.getParent() + "/" + FETCHED_DISTINCT_CLIPS_FILE );

        if (fetchedDistinct.exists())
        {
            try
            {
                fetchedDistinct.delete();
            } catch (SecurityException e)
            {
                System.err.println( "Failed to delete " + fetchedDistinct.getPath() );
                if (VerboseEnum.isShow( VerboseEnum.DEBUG, VERBOSE ))
                    e.printStackTrace();
            }
        }

        distinctClips( m_fetchedFile,
                new File( m_fetchedFile.getParent() + "/" + FETCHED_DISTINCT_CLIPS_FILE ),
                true );
    }

    public boolean isContinue()
    {
        if (m_fetchedFile.length() == 0 || isOnlyFetchHeader() || m_allowOverwrite)
        {
            return false;
        }

        return super.isContinue( m_fetchedFile );
    }

    private boolean isOnlyFetchHeader()
    {

        String lastString = Utils.readLastLine( m_fetchedFile );
        String firstString = null;

        Scanner sourceSc = null;

        try
        {
            FileInputStream fis = new FileInputStream( m_fetchedFile );
            sourceSc = new Scanner( fis );

            if (sourceSc.hasNextLine())
            {
                firstString = sourceSc.nextLine();
            }

        } catch (FileNotFoundException e)
        {
            System.err.println( "Cant read from " + m_fetchedFile.getPath() );
            if (VerboseEnum.isShow( VerboseEnum.DEBUG, VERBOSE ))
                e.printStackTrace();

            return false;
        } finally
        {
            if (sourceSc != null)
            {
                sourceSc.close();
            }
        }

        return lastString.equals( firstString );
    }

    @Override
    public Scanner getLastSourceLineToRead()
    {
        return null;
    }

    @Override
    public void writeLog(Exception e, String _text)
    {

    }

    @Override
    public void writeSkipped(JSONArray _clipsJson)
    {

    }

    @Override
    public void init(FetchFilterInterface _fetchFilter) throws SecurityException
    {
        m_allowOverwrite = true;
        super.init();
        writeHeader( _fetchFilter );
    }

    private void writeHeader(FetchFilterInterface _fetchFilter)
    {
        write( new JSONObject( _fetchFilter.getMap() ), m_fetchedFile );
    }

    @Override
    public void write(JSONArray _clipsJson)
    {
        super.write( _clipsJson, m_fetchedFile );
    }

    @Override
    public JSONObject getLastRecord()
    {
        return super.getLastRecord( m_fetchedFile );
    }

    @Override
    public JSONObject readFetchFilter()
    {
        String headerInline = "";
        Scanner sc = null;
        try
        {
            FileInputStream fis = new FileInputStream( m_fetchedFile );
            sc = new Scanner( fis );
            if (sc.hasNextLine())
            {
                headerInline = sc.nextLine();
            }
        } catch (IOException e)
        {
            System.err.println( "Cant open file " + m_fetchedFile.getPath() + " to read header" );
            if (VerboseEnum.isShow( VerboseEnum.DEBUG, VERBOSE ))
                e.printStackTrace();
        } finally
        {
            if (sc != null)
            {
                sc.close();
            }
        }

        return Utils.parseJsonObj( headerInline );
    }

    @Override
    public File getClipsDir()
    {
        return null;
    }

    @Override
    public void clean()
    {
        if (m_fetchedFile.exists())
        {
            try
            {
                m_fetchedFile.delete();
            } catch (SecurityException e)
            {
                System.err.println( "Cant delete " + m_fetchedFile.getPath() );
                if (VerboseEnum.isShow( VerboseEnum.DEBUG, VERBOSE ))
                    e.printStackTrace();
            }
        }
    }
}
