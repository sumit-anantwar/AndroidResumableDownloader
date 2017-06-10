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
import java.util.List;

/**
 * Created by Sumit Anantwar on 7/11/16.
 *
 */
class AsyncDownloader extends AsyncTask<Void, Integer, Exception>
{
    private static final String LOG_TAG = AsyncDownloader.class.getSimpleName();

    private final List<Processable> mProcessables;
    private AsyncDownloaderCallback mCallback;

    private long completedLength = 0;
    private long totalLength = 0;

    /**
     * Designated Initializer

     * @param callback : (DownloadCallback)
     */
    AsyncDownloader(List<Processable> processables, AsyncDownloaderCallback callback)
    {
        this.mProcessables = processables;
        this.mCallback = callback;
    }



    @Override
    protected void onPostExecute(Exception e)
    {
        super.onPostExecute(e);
        if (e == null) {
            mCallback.onDownloadComplete(mProcessables);
        }
        else {
            mCallback.onDownloadFailure(e);
        }
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        for (Processable processable : mProcessables) {

            totalLength += processable.getPendingContentSize();
        }
    }

    @Override
    protected void onProgressUpdate(Integer... values)
    {
        super.onProgressUpdate(values);
        // Publish the Progress Values
        mCallback.onDownloadProgress(values[0], values[1]);
    }

    @Override
    protected Exception doInBackground(Void... args)
    {
        //Log.i(LOG_TAG, "Download Started");

        try
        {
            for (Processable processable : mProcessables)
            {
                if (processable.getPendingContentSize() <= 0) {

                    completedLength += processable.getDownloadedContentSize();
                }
                else {

                    if (processable.getResponseCode() == HttpURLConnection.HTTP_OK) {

                        logInfo("Downloading : " + processable.getTargetUrl().getPath());

                        URL targetUrl = processable.getTargetUrl();
                        long downloadedContentSize = processable.getDownloadedContentSize();
                        String cacheFilePath = processable.getCacheFilePath();

                        // Open a new connection
                        HttpURLConnection connection = (HttpURLConnection) targetUrl.openConnection();
                        // We need to Download the file, so GET request
                        connection.setRequestMethod("GET");
                        // The Retry functionality depends on the "Range" header request property.
                        // Range:bytes=<startValue>-<endValue>
                        // If the <endValue> is not specified, the request returns all the Remaining Bytes
                        // Using this syntax :
                        // For first Request        : bytes=0-
                        // For all later Requests   : bytes=xxx-  (where xxx = downloadedContentSize)
                        connection.setRequestProperty("Range", "bytes=" + downloadedContentSize + "-");
                        connection.setConnectTimeout(7000);

                        // Now the getContentLength call will give us the size of the file remaining to be downloaded
                        int remainingLength = connection.getContentLength();
                        // Read the InputStream and use it to create a BufferedInputStream
                        InputStream inStream = connection.getInputStream();
                        BufferedInputStream bufferedInStream = new BufferedInputStream(inStream);

                        // Create new file if not exists
                        File cacheFile = new File(cacheFilePath);
                        if (!cacheFile.exists()) {
                            cacheFile.mkdirs();
                            cacheFile.createNewFile();
                        }

                        // Create a new FileOutputStream
                        FileOutputStream fileOutStream = new FileOutputStream(cacheFile, true);
                        final byte[] data = new byte[1024];
                        int count;
                        completedLength += downloadedContentSize;
                        while ((count = bufferedInStream.read(data, 0, 1024)) != -1) {
                            // Write the data directly to the OutputStream
                            fileOutStream.write(data, 0, count);
                            completedLength += count;

                            // Compute Cumulative Progress
                            //                    completedLength += completed;

                            // Publish the Progress
                            publishProgress((int) completedLength, (int) totalLength);
                        }

                        logInfo("Release connection for file : " + processable.getTargetUrl().getPath());
                        // Release
                        connection.disconnect();
                        inStream.close();
                        bufferedInStream.close();
                        fileOutStream.close();
                    }
                }
            }

            // this is just a security measure
            // Execution will rarely reach here
            // In most cases, the download will be terminated by raising an Exception, and execution will directly jump to Catch
            // But, just in case, lets check if the completedLength < total download size
            if (completedLength < totalLength) return new IncompleteDownloadException();
        }
        catch (Exception e) // When there is no Internet Connection or if the Host is not resolved
        {
            return e;
        }

        // Execution reaching here means the Download has been completed
        return null;
    }

    private void logInfo(String message) {
        logInfo(LOG_TAG, message);
    }

    private void logInfo(String tag, String message) {
        //Log.i(tag, message);
    }
}
