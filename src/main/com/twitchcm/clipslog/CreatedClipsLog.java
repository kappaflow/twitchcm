package main.com.twitchcm.clipslog;

import main.com.twitchcm.enums.VerboseEnum;
import main.com.twitchcm.filter.FetchFilterInterface;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.File;
import java.util.Scanner;

public class CreatedClipsLog extends ClipsFileLog {
    private static final String CREATED_CLIPS_FILE = "createdClips.txt";

    private final File m_createdFile;

    public CreatedClipsLog(boolean _allowOverwrite) {
        setRootDir(DEFAULT_DIR_PATH);

        m_createdFile = new File(m_outputDir + "/" + CREATED_CLIPS_FILE);

        if(_allowOverwrite)
        {
            clean();
        }
    }

    @Override
    public File getClipsDir() {
        return null;
    }

    @Override
    public void init(FetchFilterInterface _fetchFilter) throws SecurityException {
    }

    @Override
    public void init() throws SecurityException {
        super.init();
    }

    @Override
    public void write(JSONArray _clipsJson) {
        super.write(_clipsJson, m_createdFile);
    }

    @Override
    public JSONObject readFetchFilter() {
        return null;
    }

    @Override
    public JSONObject getLastRecord() {
        return null;
    }

    @Override
    public Scanner getLastSourceLineToRead() {
        return null;
    }

    @Override
    public void writeLog(Exception e, String _text) {

    }

    @Override
    public void writeSkipped(JSONArray _clipsJson) {
    }

    @Override
    public void clean() {
        if (m_createdFile.exists()) {
            try {
                m_createdFile.delete();
            } catch (SecurityException e) {
                System.err.println("Cant delete " + m_createdFile.getPath());
                if (VerboseEnum.isShow( VerboseEnum.DEBUG, VERBOSE ))
                    e.printStackTrace();
            }
        }
    }

    @Override
    public void distinct() {

    }
}
