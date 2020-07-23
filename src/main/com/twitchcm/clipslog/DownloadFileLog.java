package main.com.twitchcm.clipslog;

import main.com.twitchcm.enums.VerboseEnum;
import main.com.twitchcm.filter.FetchFilterInterface;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

public class DownloadFileLog extends ClipsFileLog
{
    final static protected String CLIPS_DIR_NAME = "clips";

    private static final String DOWNLOADED_CLIPS_FILE = "downloadedClips.txt";
    private final static String SKIPPED_DOWNLOADED_CLIPS_FILE = "downloadSkippedClips.log";

    private File m_downloadedFile;
    private File m_downloadSkippedFile;

    private File m_clipsDir;

    public void setClipsDir(String _parentDir)
    {
        m_clipsDir = new File( _parentDir + "/" + CLIPS_DIR_NAME );
    }

    public File getClipsDir()
    {
        return m_clipsDir;
    }

    public DownloadFileLog(boolean _allowOverwrite)
    {
        setRootDir( DEFAULT_DIR_PATH );

        m_downloadedFile = new File( m_outputDir + "/" + DOWNLOADED_CLIPS_FILE );
        m_downloadSkippedFile = new File( m_outputDir + "/" + SKIPPED_DOWNLOADED_CLIPS_FILE );

        if (_allowOverwrite)
        {
            clean();
        }
    }

    public void setRootDir(String _parentDir)
    {
        super.setRootDir( _parentDir );

        m_downloadedFile = new File( m_rootDir.getPath() + "/" + DEFAULT_DIR_NAME + "/" + DOWNLOADED_CLIPS_FILE );
        m_downloadSkippedFile =
                new File( m_rootDir.getPath() + "/" + DEFAULT_DIR_NAME + "/" + SKIPPED_DOWNLOADED_CLIPS_FILE );

        m_clipsDir = new File( m_rootDir.getPath() + "/" + DEFAULT_DIR_NAME + "/" + CLIPS_DIR_NAME );
    }

    public void init(FetchFilterInterface _fetchFilter)
    {

    }

    @Override
    public void write(JSONArray _clipsJson)
    {
        super.write( _clipsJson, m_downloadedFile );
    }

    public JSONObject readFetchFilter()
    {
        return null;
    }

    @Override
    public JSONObject getLastRecord()
    {
        return null;
    }

    @Override
    public void init() throws SecurityException
    {
        super.init();
        if (!this.m_clipsDir.exists())
        {
            this.m_clipsDir.mkdirs();
        }
    }

    @Override
    public Scanner getLastSourceLineToRead()
    {
        return super.getLastSourceLineToRead( m_downloadedFile, this.m_fetchedFile, "id" );
    }

    @Override
    public synchronized void writeLog(Exception _ex, String _url)
    {
        String currentDateTime = LocalDateTime.now().format( DateTimeFormatter.ofPattern( "yyyy-MM-dd HH:mm:ss" ) );
        String textLine = String.format( "[%s] %s | %s", currentDateTime, _ex.getMessage(), _url );

        super.write( textLine, m_downloadSkippedFile );
    }

    @Override
    public void writeSkipped(JSONArray _clipsJson)
    {

    }

    @Override
    public void clean()
    {

        if (m_downloadedFile.exists())
        {
            try
            {
                m_downloadedFile.delete();
            } catch (SecurityException e)
            {
                System.err.println( "Cant delete " + m_downloadedFile.getPath() );
                if (VerboseEnum.isShow( VerboseEnum.DEBUG, VERBOSE ))
                    e.printStackTrace();
            }
        }
        if (m_downloadSkippedFile.exists())
        {
            try
            {
                m_downloadSkippedFile.delete();
            } catch (SecurityException e)
            {
                System.err.println( "Cant delete " + m_downloadSkippedFile.getPath() );
                if (VerboseEnum.isShow( VerboseEnum.DEBUG, VERBOSE ))
                    e.printStackTrace();
            }
        }

    }

    @Override
    public void distinct()
    {

    }
}
