package com.sumitanantwar.android_resumable_downloader;

import android.content.Context;
import android.os.SystemClock;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
    private boolean isDownloading = false;

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
    public void downloadURLtoFile(final String url, final String destinationPath, final DownloadRequestCallback callback) {

        List<Downloadable> downloadables = new ArrayList<>();
        Downloadable downloadable = new Downloadable(url, destinationPath);
        downloadables.add(downloadable);

        download(downloadables, callback);
    }

    /**
     *
     * @param downloadables List of  {@link Downloadable}
     * @param callback {@link DownloadRequestCallback}
     */
    public void download(final List<Downloadable> downloadables, final DownloadRequestCallback callback)
    {
        isDownloading = true;
        SystemClock.sleep(timeBetweenRetries);

        // Create a new Retry Handler Async task
        new RetryHandler(mContext, downloadables, new RetryResponse()
        {
            @Override
            public void needsRetry(Map<Downloadable, Processable> processableMap)
            {
                // Needs Retry, so create a new AsyncDownloader task
                new AsyncDownloader(processableMap, new AsyncDownloaderCallback()
                {
                    @Override
                    public void onDownloadComplete(List<Processable> processables)
                    {
                        List<Processable> failedProcessables = new ArrayList<>();
                        List<Processable> completeProcessables = new ArrayList<>();

                        Log.i(LOG_TAG, "Download Completed");

                        // Copy the files from Cache to the final destination
                        try {

                            for (Processable processable : processables) {
                                if (processable.getResponseCode() != HttpURLConnection.HTTP_OK) {
                                    processable.onDownloadFailure(processable.getResponseCode(), processable.getHeaders());
                                    failedProcessables.add(processable);
                                }
                                else {

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

                                    completeProcessables.add(processable);
                                    processable.onDownloadComplete();
                                }
                            }

                            List<Downloadable> completeDownloadables = downloadableForProcessables(downloadables, completeProcessables);
                            List<Downloadable> failedDownloadables = downloadableForProcessables(downloadables, failedProcessables);
                            callback.onDownloadComplete(completeDownloadables, failedDownloadables);

                            isDownloading = false;
                            return; // Deliberate return, to avoid executing following code
                        }
                        catch (IOException e) {

                            Log.e(LOG_TAG, "Failed to move cached downloads");
                            e.printStackTrace();
                        }

                        // FIXME: Placeholder return, should return sensible data
                        callback.onDownloadFailure(DownloadRequestError.InternalError);
                    }

                    @Override
                    public void onDownloadProgress(long completedBytes, long totalBytes)
                    {
                        // Pass the Progress values to the callback
                        callback.onDownloadProgress(completedBytes, totalBytes);
                    }

                    @Override
                    public void onDownloadFailure(Exception e)
                    {
                        // Download has failed
                        if (e instanceof UnknownHostException)
                        {
                            // UnknownHostException : When there is no Internet Connection or if the Host is not resolved
                            callback.onDownloadFailure(DownloadRequestError.ConnectionError);
                        }
                        else if (e instanceof FileNotFoundException) {
                            // FileNotFoundException : When the host is resolved but the file is not found
                            callback.onDownloadFailure(DownloadRequestError.TargetNotFoundError);
                        }
                        else {
                            if (retryCount > 0)
                            {
                                // Else, Retry until all the retries are consumed
                                retryCount--;
                                download(downloadables, callback);
                            }
                            else
                            {
                                // All the retries are consumed, and we have a Download Failure
                                // So, trigger onDownloadFailure
                                isDownloading = false;
                                callback.onDownloadFailure(DownloadRequestError.RetriesConsumedError);
                            }
                        }
                    }
                }).execute();
            }

            @Override
            public void retryNotNeeded(Throwable e)
            {
                // Retry is not needed
                // call onDownloadFailure with the human understandable Error

                DownloadRequestError error = DownloadRequestError.InternalError;

                if (e instanceof UnknownHostException) {
                    error = DownloadRequestError.ConnectionError;
                }
                else if (e instanceof FileNotFoundException) {
                    error = DownloadRequestError.TargetNotFoundError;
                }
                else if (e instanceof OutOfMemoryError) {
                    error = DownloadRequestError.OutOfMermoryError;
                }

                Log.i(LOG_TAG, "Retry Not Needed With Response Mode : " + error.name());

                callback.onDownloadFailure(error);

            }
        }).execute();

    }

    // Get state
    public boolean isDownloading() { return isDownloading; }


    private List<Downloadable> downloadableForProcessables(List<Downloadable> downloadables, List<Processable> processables)
    {
        List<Downloadable> incompleteDownloadables = new ArrayList<>(processables.size());

        for (Downloadable downloadable : downloadables) {
            if (incompleteDownloadables.size() < processables.size()){
                for (Processable processable : processables) {
                    if ((downloadable.getTargetUrl().equals(processable.getTargetUrl())) && (downloadable.getDestinationPath().equals(processable.getDestinationPath()))) {
                        incompleteDownloadables.add(downloadable);
                        break;
                    }
                }
            }
        }

        return incompleteDownloadables;
    }
}
