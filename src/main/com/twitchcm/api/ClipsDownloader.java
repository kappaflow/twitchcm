package main.com.twitchcm.api;

import main.com.twitchcm.clipslog.DownloadFileLog;
import main.com.twitchcm.enums.ClipsJsonEnum;
import main.com.twitchcm.enums.DataSourceEnum;
import main.com.twitchcm.enums.VerboseEnum;
import main.com.twitchcm.filter.ProceedClipsFilter;
import main.com.twitchcm.tool.ClipsMultiDownloader;
import main.com.twitchcm.tool.Utils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

public class ClipsDownloader extends ClipsActionAbstract
{
    private ProceedClipsFilter m_proceedFilter = new ProceedClipsFilter();

    public ClipsDownloader(DataSourceEnum _dataSource, boolean _allowOverwrite)
    {
        if (_dataSource == DataSourceEnum.FILES)
        {
            m_clipsLog = new DownloadFileLog( _allowOverwrite );
        }
        else
        {
            throw new RuntimeException( "Incorrect clipsHandler" );
        }
    }

    public void setProceedFilter(ProceedClipsFilter _proceedFilter)
    {
        m_proceedFilter = _proceedFilter;
    }

    public void downloadClips(int _eachTime)
    {
        JSONArray downloadList = new JSONArray();
        JSONObject jsonObj;
        String urlStr;
        String fileDir;

        if (m_clipsLog == null)
        {
            System.err.println( "Warning! ClipsLogInterface isn't set" );
            return;
        }

        try
        {
            m_clipsLog.init();
        }
        catch (SecurityException e)
        {
            System.err.println( "Failed to create directory!" );
            if (VerboseEnum.isShow( VerboseEnum.DEBUG, VERBOSE ))
                e.printStackTrace();
        }

        ClipsMultiDownloader clipsDownloader = new ClipsMultiDownloader( _eachTime );
        clipsDownloader.setDownloadLog( m_clipsLog );
        Scanner fetchedSc = m_clipsLog.getLastSourceLineToRead();

        if (!fetchedSc.hasNextLine())
        {
            System.err.println( "Warning! All fetched clips were already downloaded!" );
            return;
        }

        while (fetchedSc.hasNextLine())
        {
            jsonObj = Utils.parseJsonObj( fetchedSc.nextLine() );
            if (m_proceedFilter.isSatisfy( jsonObj ))
            {
                urlStr = Utils.parseClipDownloadURL( jsonObj.get( ClipsJsonEnum.THUMBNAIL_URL.getFetchedJsonKey() ).toString() );
                fileDir = getFileName( jsonObj );

                downloadList.add( jsonObj );
                try
                {
                    clipsDownloader.addTargetFile( new URL( urlStr ), new File( fileDir ) );
                }
                catch (NullPointerException | MalformedURLException e)
                {
                    System.err.println( e.toString() );
                    if (VerboseEnum.isShow( VerboseEnum.DEBUG, VERBOSE ))
                        e.printStackTrace();
                }

                if (downloadList.size() != clipsDownloader.size() || clipsDownloader.size() > _eachTime)
                {
                    throw new RuntimeException( "Download lists don't match or overflow" );
                }
            }

            if (downloadList.size() == _eachTime || (!fetchedSc.hasNextLine() && downloadList.size() > 0))
            {
                clipsDownloader.get();
                m_clipsLog.write( downloadList );

                clipsDownloader.removeAll();
                downloadList.clear();
            }
        }
        fetchedSc.close();
    }

    private String getFileName(JSONObject _clipJson)
    {
        String path = m_clipsLog.getClipsDir().getPath();
        String prefix = _clipJson.get( ClipsJsonEnum.CREATED_AT.getFetchedJsonKey() ).toString().replace( ":", "-" );
        String name = _clipJson.get( ClipsJsonEnum.ID.getFetchedJsonKey() ) + ".mp4";

        return path + "/" + prefix + "_" + name;
    }
}
