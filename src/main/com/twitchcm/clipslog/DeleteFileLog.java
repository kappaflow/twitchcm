package main.com.twitchcm.clipslog;

import main.com.twitchcm.enums.ClipsJsonEnum;
import main.com.twitchcm.enums.VerboseEnum;
import main.com.twitchcm.filter.FetchFilterInterface;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.File;
import java.util.Scanner;

public class DeleteFileLog extends ClipsFileLog
{
    private static final String DELETED_CLIPS_FILE = "deletedClips.txt";
    private static final String SKIPPED_DELETED_CLIPS_FILE = "deleteSkippedClips.log";

    private final File m_deletedFile;
    private final File m_deleteSkippedFile;

    public DeleteFileLog(boolean _allowOverwrite)
    {
        setRootDir( DEFAULT_DIR_PATH );

        m_deletedFile = new File( m_outputDir + "/" + DELETED_CLIPS_FILE );
        m_deleteSkippedFile = new File( m_outputDir + "/" + SKIPPED_DELETED_CLIPS_FILE );

        if (_allowOverwrite)
        {
            clean();
        }
    }

    @Override
    public File getClipsDir()
    {
        return null;
    }

    @Override
    public void init() throws SecurityException
    {
        super.init();
    }

    @Override
    public void write(JSONArray _clipsJson)
    {
        super.write( _clipsJson, m_deletedFile );
    }

    @Override
    public void writeSkipped(JSONArray _clipsJson)
    {
        super.write( _clipsJson, m_deleteSkippedFile );
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
    public Scanner getLastSourceLineToRead()
    {
        return super.getLastSourceLineToRead( m_deletedFile, this.m_fetchedFile, ClipsJsonEnum.ID.getFetchedJsonKey() );
    }

    @Override
    public void writeLog(Exception e, String _text)
    {

    }

    @Override
    public void clean()
    {
        if (m_deletedFile.exists())
        {
            try
            {
                m_deletedFile.delete();
            }
            catch (SecurityException e)
            {
                System.err.println( "Cant delete " + m_deletedFile.getPath() );
                if (VerboseEnum.isShow( VerboseEnum.DEBUG, VERBOSE ))
                    e.printStackTrace();
            }
        }
        if (m_deleteSkippedFile.exists())
        {
            try
            {
                m_deleteSkippedFile.delete();
            }
            catch (SecurityException e)
            {
                System.err.println( "Cant delete " + m_deleteSkippedFile.getPath() );
                if (VerboseEnum.isShow( VerboseEnum.DEBUG, VERBOSE ))
                    e.printStackTrace();
            }
        }
    }

    @Override
    public void distinct()
    {

    }

    public void init(FetchFilterInterface _fetchFilter) throws SecurityException
    {

    }
}
