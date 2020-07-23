package main.com.twitchcm.tool;

import io.netty.handler.codec.http.HttpHeaders;
import main.com.twitchcm.clipslog.ClipsLogInterface;
import main.com.twitchcm.enums.VerboseEnum;
import main.com.twitchcm.exceptions.GettingClipsException;
import org.asynchttpclient.*;
import org.asynchttpclient.filter.FilterContext;
import org.asynchttpclient.filter.FilterException;
import org.asynchttpclient.filter.IOExceptionFilter;
import org.asynchttpclient.filter.ResponseFilter;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ClipsMultiDownloader
{
    private static class TargetFile
    {
        private final URL m_url;
        private final File m_file;

        public URL getUrl()
        {
            return m_url;
        }

        public File getFile()
        {
            return m_file;
        }

        public TargetFile(URL _url, File _file)
        {
            m_url = _url;
            m_file = _file;
        }
    }

    protected static final VerboseEnum VERBOSE = VerboseEnum.DEBUG;

    private final static int MAX_DOWNLOADING_TIME_MS = 120000; //per file
    private final static String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, " +
            "like Gecko) Chrome/83.0.4103.106 Safari/537.36\")";

    private final static String CONTENT_TYPE = "Content-Type";
    private final static String CONTENT_LENGTH = "Content-Length";

    private static final int RETRY_ATT_NUMBER = 5;
    private static final int RETRY_IOEx_ATT_NUMBER = 5;
    private static final int RETRY_WAIT_TIME_MS = 1000;

    private int m_retryAttemptCounter = 0;
    private int m_retryIOExAttemptCounter = 0;
    private String m_currentRespCode = "";

    private final List<TargetFile> m_fileList;
    private boolean m_exitFlag = false;
    private ClipsLogInterface m_downloadLog;

    public ClipsMultiDownloader()
    {
        m_fileList = new ArrayList<>();
    }

    public ClipsMultiDownloader(int _threadLimit)
    {
        m_fileList = new ArrayList<>( _threadLimit );
    }

    public void addTargetFile(URL _url, File _file)
    {
        m_fileList.add( new TargetFile( _url, _file ) );
    }

    public void removeAll()
    {
        m_fileList.clear();
    }

    public int size()
    {
        return m_fileList.size();
    }

    public void setDownloadLog(ClipsLogInterface _downloadLog)
    {
        m_downloadLog = _downloadLog;
    }

    public void get()
    {
        if (m_fileList.isEmpty())
        {
            return;
        }

        if (m_downloadLog != null)
        {
            m_downloadLog.init();
        }

        ExecutorService executor = Executors.newFixedThreadPool( m_fileList.size() );

        for (TargetFile targetFile : m_fileList)
        {
            executor.submit( getTask( targetFile ) );
        }

        executor.shutdown();

        while (true)
        {
            try
            {
                if (executor.awaitTermination( 5, TimeUnit.SECONDS ))
                    break;
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
                m_exitFlag = true;
            }
        }

        if (m_exitFlag)
        {
            System.err.println( "The error can't be handled! Termination" );
            System.exit( 1 );
        }
    }

    private Runnable getTask(TargetFile _targetFile)
    {
        return () ->
        {
            try
            {
                startDownload( _targetFile );
            }
            catch (GettingClipsException e)
            {
                System.err.println( e.getMessage() );
                m_downloadLog.writeLog( e, _targetFile.getUrl().toString() );
            }
            catch (Exception e)
            {
                if (VerboseEnum.isShow( VerboseEnum.DEBUG, VERBOSE ))
                    e.printStackTrace();

                //delete half-downloaded file
                if (_targetFile.m_file.exists())
                {
                    _targetFile.m_file.delete();
                }

                m_exitFlag = true;
            }
        };
    }

    private AsyncHttpClient initHttpClientConfig(TargetFile _targetFileObj)
    {
        DefaultAsyncHttpClientConfig.Builder builder = Dsl.config();

        builder.setRequestTimeout( MAX_DOWNLOADING_TIME_MS );
        builder.setUserAgent( USER_AGENT );

        builder.addResponseFilter( getResponseFilter( _targetFileObj ) );
        builder.addIOExceptionFilter( getIOExceptionFilter( _targetFileObj ) );

        return Dsl.asyncHttpClient( builder.build() );
    }

    private ResponseFilter getResponseFilter(TargetFile _targetFileObj)
    {
        return new ResponseFilter()
        {
            @Override
            public FilterContext filter(FilterContext ctx) throws FilterException
            {
                m_currentRespCode = ctx.getResponseStatus().toString();

                if (ctx.getResponseStatus().getStatusCode() != 200)
                {
                    if (VerboseEnum.isShow( VerboseEnum.DEBUG, VERBOSE ))
                        System.err.println( "Error response: " + m_currentRespCode );

                    FilterContext ctx2 = new FilterContext.FilterContextBuilder( ctx )
                            .request( new RequestBuilder( "GET" )
                                    .setUrl( _targetFileObj.getUrl().toString() ).build() ).replayRequest( true ).build();

                    return ctx2;
                }

                return ctx;
            }
        };
    }

    private IOExceptionFilter getIOExceptionFilter(TargetFile _targetFileObj)
    {
        return new IOExceptionFilter()
        {
            @Override
            public FilterContext filter(FilterContext ctx) throws FilterException
            {
                if (ctx.getIOException() != null)
                {
                    if (VerboseEnum.isShow( VerboseEnum.DEBUG, VERBOSE ))
                        System.err.println( ctx.getIOException().toString() );

                    FilterContext.FilterContextBuilder cb =
                            new FilterContext.FilterContextBuilder( ctx ).asyncHandler( ctx.getAsyncHandler() )
                                    .request( new RequestBuilder( "GET" )
                                            .setUrl( _targetFileObj.getUrl().toString() ).build() ).replayRequest( true );

                    FilterContext ctx2 = cb.build();

                    m_retryIOExAttemptCounter++;

                    return ctx2;
                }
                return ctx;
            }
        };
    }

    private ListenableFuture<Response> getResponse(AsyncHttpClient _client, TargetFile _targetFileObj)
    {
        File targetFile;


        if (_targetFileObj.getFile().isDirectory())
        {
            //if the file name is not specified
            targetFile = new File( _targetFileObj.getFile().getAbsolutePath()
                    + "/" + _targetFileObj.getUrl().getFile() );
        }
        else
        {
            targetFile = _targetFileObj.getFile();
        }

        return _client.prepareGet( _targetFileObj.getUrl().toString() )
                .execute( new AsyncCompletionHandler<Response>()
                {
                    FileOutputStream stream;

                    @Override
                    public State onBodyPartReceived(HttpResponseBodyPart bodyPart) throws Exception
                    {
                        stream.getChannel()
                                .write( bodyPart.getBodyByteBuffer() );

                        return State.CONTINUE;
                    }

                    @Override
                    public Response onCompleted(Response _response) throws Exception
                    {
                        if (VerboseEnum.isShow( VerboseEnum.NORMAL, VERBOSE ))
                            System.out.println( "File: " + _response.getUri()
                                    + "\nwas uploaded to: " + targetFile.getPath() );

                        if (stream != null)
                        {
                            stream.getChannel().close();
                        }

                        return _response;
                    }

                    @Override
                    public void onRetry()
                    {
                        if (m_retryAttemptCounter < RETRY_ATT_NUMBER)
                        {
                            m_retryAttemptCounter++;
                            if (VerboseEnum.isShow( VerboseEnum.NORMAL, VERBOSE ))
                                System.err.println( "Download attempt #: " + m_retryAttemptCounter );
                        }
                        else if (m_retryIOExAttemptCounter >= RETRY_IOEx_ATT_NUMBER)
                        {
                            throw new RuntimeException( "Exceed IO exception attempts limit!" );
                        }
                        else
                        {
                            throw new GettingClipsException( "Exceed connection attempts limit!"
                                    + " Last response code: " + m_currentRespCode );
                        }

                        try
                        {
                            Thread.sleep( RETRY_WAIT_TIME_MS );
                        }
                        catch (InterruptedException e)
                        {
                            if (VerboseEnum.isShow( VerboseEnum.DEBUG, VERBOSE ))
                                e.printStackTrace();
                        }

                        super.onRetry();
                    }

                    @Override
                    public State onHeadersReceived(HttpHeaders headers) throws Exception
                    {
                        if (!isAllowedFileFormat( headers ))
                        {
                            throw new GettingClipsException( headers.get( CONTENT_TYPE ) + " type isn't supported" );
                        }

                        if (targetFile.length() == Long.parseLong( headers.get( CONTENT_LENGTH ) ))
                        {
                            if (VerboseEnum.isShow( VerboseEnum.NORMAL, VERBOSE ))
                                System.err.println( "Warning! " + targetFile.getName() + " is already exist." );

                            if (m_retryAttemptCounter > 0)
                            {
                                m_retryAttemptCounter--;
                            }

                            super.onHeadersReceived( headers );

                            return State.ABORT;
                        }

                        stream = new FileOutputStream( targetFile );

                        return super.onHeadersReceived( headers );
                    }
                } );
    }

    private void startDownload(TargetFile _targetFileObj) throws Exception
    {
        AsyncHttpClient client = initHttpClientConfig( _targetFileObj );
        ListenableFuture<Response> response = getResponse( client, _targetFileObj );

        try
        {
            response.get();
        }
        catch (ExecutionException e)
        {
            throw (Exception) e.getCause();
        }
        finally
        {
            client.close();
        }
    }

    private boolean isAllowedFileFormat(HttpHeaders _headers)
    {
        return _headers.contains( CONTENT_TYPE, "binary/octet-stream", true ) ||
                _headers.contains( CONTENT_TYPE, "video/mp4", true );
    }
}