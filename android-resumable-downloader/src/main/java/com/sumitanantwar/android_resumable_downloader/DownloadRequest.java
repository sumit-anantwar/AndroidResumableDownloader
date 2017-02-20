package com.sumitanantwar.android_resumable_downloader;

import android.os.SystemClock;
import android.util.Log;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by Sumit Anantwar on 7/13/16.
 *
 * Download Request ---
 *
 */
public class DownloadRequest
{
    private static String LOG_TAG = DownloadRequest.class.getSimpleName();

    private int retryCount = 7;
    private int timeBetweenRetries = 1000;

    public void setRetryCount(int count)
    {
        retryCount = count;
    }

    public void setTimeBetweenRetries(int milliseconds)
    {
        timeBetweenRetries = milliseconds;
    }

    /**
     *
     * @param url
     * @param destinationPath
     * @param callback
     */
    //TODO : Should handle multiple files in a single request
    //TODO : Should download to a temporary file and move it to the destination only after the download has completed
    //       But this must take into account the Resume functionality
    public void downloadURLtoFile(final String url, final String destinationPath, final DownloadCallback callback)
    {
        SystemClock.sleep(timeBetweenRetries);
        try
        {
            /* Convert URL String to URL Object */
            final URL targetURL = new URL(url);

            /* Create a new Retry Handler Async task */
            new RetryHandler(targetURL, destinationPath, new RetryResponse()
            {
                @Override
                public void needsRetry(long downloadedContentSize)
                {
                    /* Needs Retry, so create a new AsyncDownloader task */
                    new AsyncDownloader(targetURL, destinationPath, downloadedContentSize, new DownloadCallback()
                    {
                        @Override
                        public void onDownloadComplete()
                        {
                            Log.i(LOG_TAG, "Download Completed");

                            /* Download Completed, trigger onDownloadComplete on the callback */
                            callback.onDownloadComplete();
                        }

                        @Override
                        public void onDownloadProgress(long completedBytes, long totalBytes)
                        {
                            /* Pass the Progress values to the callback */
                            callback.onDownloadProgress(completedBytes, totalBytes);
                        }

                        @Override
                        public void onDownloadFailure(RetryMode retryMode)
                        {
                            /* Download has failed */
                            Log.i(LOG_TAG, "Download Failure With Retry Mode : " + retryMode.name());

                            /* Check the retryMode */

                            // If there was a ConnectionError or if the target URL returned 404, there is no point in Retrying
                            if ((retryMode == RetryMode.ConnectionError) || (retryMode == RetryMode.HostNotFound))
                            {
                                callback.onDownloadFailure(retryMode);
                            }
                            // Else, Retry until all the retries are consumed
                            else if (retryCount > 0)
                            {
                                retryCount--;
                                downloadURLtoFile(url, destinationPath, callback);
                            }
                            // All the retries are consumed, and we have a Download Failure
                            // So, trigger onDownloadFailure
                            else
                            {
                                callback.onDownloadFailure(RetryMode.RetriesConsumed);
                            }
                        }
                    }).execute();
                }

                @Override
                public void retryNotNeeded(RetryMode retryMode)
                {
                    /* Retry is not needed */
                    Log.i(LOG_TAG, "Retry Not Needed With Response Mode : " + retryMode.name());

                    /* If the Download has been completed, call onDownloadComplete */
                    if (retryMode == RetryMode.DownloadComplete)
                        callback.onDownloadComplete();
                    /* Else, call onDownloadFailure with the retryMode, we will handle this in the calling method */
                    else
                        callback.onDownloadFailure(retryMode);

                }
            }).execute();
        }
        catch (MalformedURLException e)
        {
            e.printStackTrace();
        }
    }

}
