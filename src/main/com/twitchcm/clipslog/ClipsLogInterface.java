package main.com.twitchcm.clipslog;

import main.com.twitchcm.filter.FetchFilterInterface;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.File;
import java.util.Scanner;

public interface ClipsLogInterface {

    void setRootDir(String _parentDir);
    File getRootDir();
    File getClipsDir();
    void clean();
    void distinct();

    //FetchFileLog
    void init(FetchFilterInterface _fetchFilter) throws SecurityException;
    void write(JSONArray _clipsJson);
    JSONObject readFetchFilter();
    JSONObject getLastRecord();
    boolean isContinue();

    //DownloadFileLog
    void init() throws SecurityException;
    Scanner getLastSourceLineToRead();
    void writeLog(Exception e, String _text);

    //DeleteFileLog
    void writeSkipped(JSONArray _clipsJson);
}
