package com.sumitanantwar.android_resumable_downloader;

import android.content.Context;
import android.os.SystemClock;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Sumit Anantwar on 7/13/16.
 *
 * Download Request ---
 *
 */
public class DownloadRequest
{
    private static String LOG_TAG = DownloadRequest.class.getSimpleName();

    private final Context mContext;
    private int retryCount = 7;
    private int timeBetweenRetries = 1000;

    public DownloadRequest(Context context) {
        this.mContext = context;
    }

    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }

    public void setTimeBetweenRetries(int milliseconds) {
        this.timeBetweenRetries = milliseconds;
    }

    /**
     *
     * @param url
     * @param destinationPath
     * @param callback
     */
    public void downloadURLtoFile(final String url, final String destinationPath, final DownloadCallback callback) {

        List<Downloadable> downloadables = new ArrayList<>();
        Downloadable downloadable = new Downloadable(url, destinationPath);
        downloadables.add(downloadable);

        download(downloadables, callback);
    }

    public void download(final List<Downloadable> downloadables, final DownloadCallback callback)
    {
        SystemClock.sleep(timeBetweenRetries);

        // Create a new Retry Handler Async task
        new RetryHandler(mContext, downloadables, new RetryResponse()
        {
            @Override
            public void needsRetry(List<Processable> processables)
            {
                // Needs Retry, so create a new AsyncDownloader task
                new AsyncDownloader(processables, new AsyncDownloaderCallback()
                {
                    @Override
                    public void onDownloadComplete(List<Processable> processables)
                    {
                        //Log.i(LOG_TAG, "Download Completed");

                        try {

                            for (Processable processable : processables) {

                                File cacheFile = new File(processable.getCacheFilePath());
                                File destinationFile = new File(processable.getDestinationPath());

                                if (!destinationFile.exists()) {
                                    destinationFile.getParentFile().mkdirs();
                                    destinationFile.createNewFile();
                                }

                                FileInputStream inStream = new FileInputStream(cacheFile);
                                FileOutputStream outStream = new FileOutputStream(destinationFile);

                                byte[] buffer = new byte[1024];
                                int read;
                                while ((read = inStream.read(buffer)) != -1) {
                                    outStream.write(buffer, 0, read);
                                }
                                inStream.close();
                                outStream.close();

                                //Delete the cache file
                                cacheFile.delete();


                                callback.onComplete(processable);
                            }

                            // Download Completed, trigger onDownloadComplete on the callback
                            callback.onDownloadComplete();
                        }
                        catch (IOException e) {

                            e.printStackTrace();
                        }

                        // FIXME: Placeholder return, should return sensible info
                        callback.onDownloadFailure(RetryMode.ConnectionError);
                    }

                    @Override
                    public void onDownloadProgress(long completedBytes, long totalBytes)
                    {
                        // Pass the Progress values to the callback
                        callback.onDownloadProgress(completedBytes, totalBytes);
                    }

                    @Override
                    public void onDownloadFailure(RetryMode retryMode)
                    {
                        // Download has failed
                        //Log.i(LOG_TAG, "Download Failure With Retry Mode : " + retryMode.name());

                        // Check the retryMode

                        // If there was a ConnectionError or if the target URL returned 404, there is no point in Retrying
                        if ((retryMode == RetryMode.ConnectionError) || (retryMode == RetryMode.HostNotFound))
                        {
                            callback.onDownloadFailure(retryMode);
                        }
                        // Else, Retry until all the retries are consumed
                        else if (retryCount > 0)
                        {
                            retryCount--;
                            download(downloadables, callback);
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
                // Retry is not needed
                //Log.i(LOG_TAG, "Retry Not Needed With Response Mode : " + retryMode.name());

                // If the Download has been completed, call onDownloadComplete
                if (retryMode == RetryMode.DownloadComplete)
                    callback.onDownloadComplete();
                // Else, call onDownloadFailure with the retryMode, we will handle this in the calling method
                else
                    callback.onDownloadFailure(retryMode);

            }
        }).execute();

    }

}
