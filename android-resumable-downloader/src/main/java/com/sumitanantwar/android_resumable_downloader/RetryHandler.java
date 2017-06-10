package com.sumitanantwar.android_resumable_downloader;

import android.accounts.NetworkErrorException;
import android.content.Context;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Sumit Anantwar on 7/13/16.
 *
 */
public class RetryHandler extends AsyncTask<Void, Void, Boolean>
{
    private static final String LOG_TAG = RetryHandler.class.getSimpleName();

    private final Context mContext;
    private final List<Downloadable> mDownloadables;
    private final RetryResponse mRetryResponse;

    private final List<Processable> processables;
    private Throwable error;

    public RetryHandler(Context context, List<Downloadable> downloadables, RetryResponse retryResponse)
    {
        this.mContext = context;
        this.mDownloadables = downloadables;
        this.mRetryResponse = retryResponse;

        this.processables = new ArrayList<>(downloadables.size());
    }

    @Override
    protected void onPostExecute(Boolean shouldRetry)
    {
        super.onPostExecute(shouldRetry);

        // Check the result Boolean
        // if True, we should retry downloading
        if (shouldRetry) {

            mRetryResponse.needsRetry(processables);
        }
        else if (error != null) {
            // if False and if an Exception was caught
            mRetryResponse.retryNotNeeded(error);
        }
        else {
            // Execution reaches here if (targetLength == downloadedContentSize)
            // This means that no more data is available for download, Download has been completed
            mRetryResponse.retryNotNeeded(null);
        }
    }

    @Override
    protected Boolean doInBackground(Void... params)
    {
        try
        {
            long pendingContentSize = 0;

            for (Downloadable downloadable : mDownloadables)
            {
                Processable processable = new Processable(downloadable);

                URL targetUrl = processable.getTargetUrl();

                String cacheFilename = Util.generateHashFromString(targetUrl.getPath());
                String cachePath = mContext.getCacheDir() + File.separator + cacheFilename;
                processable.setCacheFilPath(cachePath);

                // Open a new connection
                HttpURLConnection connection = (HttpURLConnection) targetUrl.openConnection();
                Map<String, List<String>> headers = connection.getHeaderFields();
                // We only need to check the total size of the file to be downloaded.
                // So, a HEAD request is enough.
                // This saves bandwidth and is faster.
                connection.setRequestMethod("HEAD");
                connection.setConnectTimeout(7000);

                // Get the Response Code from the connection
                int responseCode = connection.getResponseCode();
                processable.setResponseCode(responseCode);
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    // The connection is successful

                    // Open the cache file.
                    File cacheFile = new File(cachePath);
                    if(!cacheFile.exists()) {
                        // Create the parent folder(s) if necessary
                        cacheFile.getParentFile().mkdirs();
                        // Create the file
                        cacheFile.createNewFile();
                    }

                    processable.setDownloadedContentSize(cacheFile.length());

                    // Get the Content Length
                    processable.setTotalContentSize(connection.getContentLength());
                }

                // Disconnect
                connection.disconnect();

                // Add the processable to the ArrayList
                processables.add(processable);

                // Calculate the Total Pending Content Size
                pendingContentSize += processable.getPendingContentSize();
            }

            // return false with null error, If there is no pendingContent
            if (pendingContentSize <= 0) {

                if (processables.size() > 0) {
                    return true;
                }

                error = null;
                return false;
            }

            // Check if we have enough space available in the Internal Memory
            StatFs stat = new StatFs(Environment.getDataDirectory().getPath());
            long bytesAvailable = (long)stat.getBlockSize() *(long)stat.getBlockCount();

            if (bytesAvailable < pendingContentSize)
            {
                // Not enough memory to download the content
                error = new OutOfMemoryError();
                return false;
            }
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

        // If execution reaches here, we need to retry, return True
        return true;
    }
}
