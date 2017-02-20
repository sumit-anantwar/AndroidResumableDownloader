package com.sumitanantwar.android_resumable_downloader;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownHostException;

/**
 * Created by Sumit Anantwar on 7/11/16.
 *
 */
public class AsyncDownloader extends AsyncTask<Void, Integer, RetryMode>
{
    private static final String LOG_TAG = AsyncDownloader.class.getSimpleName();

    private URL targetURL;
    private String destinationPath;
    private long downloadedContentSize;
    private DownloadCallback downloadCallback;

    /**
     * Designated Initializer
     * @param url : (URL) URL to be downloaded.
     * @param localFilePath : (String) Local path where the download is to be saved.
     * @param contentSize : (long) Size of the already downloaded content.
     * @param callback : (DownloadCallback)
     */
    public AsyncDownloader(URL url, String localFilePath, long contentSize, DownloadCallback callback)
    {
        targetURL = url;
        destinationPath = localFilePath;
        downloadedContentSize = contentSize;
        downloadCallback = callback;
    }

    @Override
    protected void onPreExecute()
    {
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(RetryMode retryMode)
    {
        super.onPostExecute(retryMode);

        /* If the Download has been completed, call onDownloadComplete */
        if (retryMode == RetryMode.DownloadComplete)
            downloadCallback.onDownloadComplete();
        /* Else, call onDownloadFailure with the retryMode, we will handle this in the calling method */
        else
            downloadCallback.onDownloadFailure(retryMode);
    }

    @Override
    protected void onProgressUpdate(Integer... values)
    {
        super.onProgressUpdate(values);
        /* Publish the Progress Values */
        downloadCallback.onDownloadProgress(values[0], values[1]);
    }

    @Override
    protected RetryMode doInBackground(Void... args)
    {
        Log.i(LOG_TAG, "Download Started");

        HttpURLConnection connection = null;
        InputStream inStream = null;
        BufferedInputStream bufferedInStream = null;
        FileOutputStream fileOutStream = null;

        try
        {
            // Open a new connection
            connection = (HttpURLConnection) targetURL.openConnection();
            // We need to Download the file, so GET request
            connection.setRequestMethod("GET");
            // The Retry functionality depends on the "Range" header request property.
            // Range:bytes=<startValue>-<endValue>
            // If the <endValue> is not specified, the request returns all the Remaining Bytes
            // Using this syntax :
            // For first Request        : bytes=0-
            // For all later Requests   : bytes=100-  (where downloadedContentSize = 100)
            connection.setRequestProperty("Range", "bytes=" + downloadedContentSize + "-");
            connection.setConnectTimeout(7000);

            // Now the getContentLength call will give us the size of the file remaining to be downloaded
            int remainingLength = connection.getContentLength();
            // Read the InputStream and use it to create a BufferedInputStream
            inStream = connection.getInputStream();
            bufferedInStream = new BufferedInputStream(inStream);

            // Create new file if not exists
            File destinationFile = new File( destinationPath );
            if( destinationFile.exists()==false ){
                destinationFile.mkdirs();
                destinationFile.createNewFile();
            }

            // Create a new FileOutputStream
            fileOutStream = new FileOutputStream(destinationFile, true);
            final byte[] data = new byte[1024];
            int count;
            int completedLength = 0;
            while ((count = bufferedInStream.read(data, 0, 1024)) != -1)
            {
                // Write the data directly to the OutputStream
                fileOutStream.write(data, 0, count);
                completedLength += count;

                // Compute Cumulative Progress
                Integer completed   = (int) (downloadedContentSize +completedLength);
                Integer total       = (int) (downloadedContentSize +remainingLength);
                // Publish the Progress
                publishProgress(completed, total);
            }

            // Release
            connection.disconnect();
            inStream.close();
            bufferedInStream.close();
            fileOutStream.close();

            // Execution will rarely reach here
            // this is just a security measure
            // In most cases, the download will be terminated by raising an Exception, and execution will directly jump to Catch
            // But, just incase, lets check if the completedLength < total download size
            if (completedLength < (downloadedContentSize+remainingLength)) return RetryMode.IncompleteDownload;

        }
        catch (UnknownHostException e) // When there is no Internet Connection or if the Host is not resolved
        {
            return RetryMode.ConnectionError;
        }
        catch (FileNotFoundException e) // When the host is resolved but the file is not found
        {
            return RetryMode.HostNotFound; // We will catch it as HostNotFound
        }
        catch (IOException e) // For all other IOExceptions
        {
            e.printStackTrace();

            return RetryMode.ShouldRetry; // We need to Retry
        }

        // Execution reaching here means the Download has been completed
        return RetryMode.DownloadComplete;
    }
}
