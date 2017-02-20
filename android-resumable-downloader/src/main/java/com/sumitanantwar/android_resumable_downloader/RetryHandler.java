package com.sumitanantwar.android_resumable_downloader;

import android.os.AsyncTask;
import android.os.Environment;
import android.os.StatFs;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;

/**
 * Created by Sumit Anantwar on 7/13/16.
 *
 */
public class RetryHandler extends AsyncTask<Void, Void, Boolean>
{
    private static final String LOG_TAG = RetryHandler.class.getSimpleName();

    private URL targetURL;
    private String destinationPath;
    private RetryResponse retryResponse;

    private long downloadedContentSize;
    private Throwable error;

    public RetryHandler(URL url, String localFilePath, RetryResponse response)
    {
        targetURL = url;
        destinationPath = localFilePath;
        retryResponse = response;
    }

    @Override
    protected void onPreExecute()
    {
        super.onPreExecute();


    }

    @Override
    protected void onPostExecute(Boolean shouldRetry)
    {
        super.onPostExecute(shouldRetry);

        /* Check the result Boolean */
        // if True, we should retry downloading
        if (shouldRetry)
        {
            retryResponse.needsRetry(downloadedContentSize);
        }
        // if False and if an Exception was caught
        else if (error != null)
        {
            if (error instanceof UnknownHostException)
                retryResponse.retryNotNeeded(RetryMode.HostNotFound);
            else if (error instanceof SocketTimeoutException)
                retryResponse.retryNotNeeded(RetryMode.HostNotFound);
            else if (error instanceof OutOfMemoryError)
                retryResponse.retryNotNeeded(RetryMode.OutOfMemory);
            else
                retryResponse.retryNotNeeded(RetryMode.ConnectionError);
        }
        else
        {
            // Execution reaches here if (targetLength == downloadedContentSize)
            // This means that no more data is available for download, Download has been completed
            retryResponse.retryNotNeeded(RetryMode.DownloadComplete);
        }
    }

    @Override
    protected Boolean doInBackground(Void... params)
    {
        try
        {
            /* Open a new connection */
            HttpURLConnection connection = (HttpURLConnection) targetURL.openConnection();

            // We only need to check the total size of the file to be downloaded.
            // So, a HEAD request is enough.
            // This saves bandwidth and is faster.
            connection.setRequestMethod("HEAD");
            connection.setConnectTimeout(7000);

            // Open the local file.
            // If not found, a new file will be created.
            File destinationFile = new File(destinationPath);
            if( destinationFile.exists()==false ){

                destinationFile.getParentFile().mkdirs();
                destinationFile.createNewFile();
            }

            // Check the size of the downloaded file.
            // If file has been created just now, the size will be 0.
            downloadedContentSize = destinationFile.length();

            StatFs stat = new StatFs(Environment.getDataDirectory().getPath());
            long bytesAvailable = (long)stat.getBlockSize() *(long)stat.getBlockCount();

            if (bytesAvailable < downloadedContentSize)
            {
                error = new OutOfMemoryError();
                return false;
            }

            // Get the Response Code from the connection
            int responseCode = connection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_NOT_FOUND) // 404
            {
                // If the Host is resolved, but the target file is not found, we will get a 404 Not Found response.
                // This wont raise an exception, but our onPostExecute logic needs a UnknownHostException for this kind of error
                // So assign error with a placeholder exception and return False.
                error = new UnknownHostException();
                return false;
            }
            else if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) // 200
            {
                // Get the Content Length
                long targetLength = connection.getContentLength();

                // If targetLength == downloadedContentSize,
                // this means that the File has been completely downloaded, hence retry is not needed,
                // return False
                if (targetLength == downloadedContentSize)
                    return false;
            }

            // Disconnect
            connection.disconnect();
        }
        catch (IOException e)
        {
            // Catching the raised IOException
            // This happens when the URL is unreachable or the Host is not resolved.
            Log.i(LOG_TAG, "Connection Error");
            e.printStackTrace();
            error = e;
            return false;
        }

        /* If execution reaches here, we need to retry, return True */
        return true;
    }
}
