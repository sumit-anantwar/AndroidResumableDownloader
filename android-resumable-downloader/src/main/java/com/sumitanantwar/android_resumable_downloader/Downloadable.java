package com.sumitanantwar.android_resumable_downloader;

import java.net.MalformedURLException;
import java.net.URL;
import java.security.InvalidParameterException;
import java.util.List;
import java.util.Map;

/**
 * Created by Sumit Anantwar on 2/6/17.
 */

public class Downloadable {

    private final URL mTargetUrl;
    private final String mDestinationPath;
    private Integer mTag = null;
    private OnDownloadListener mDownloadListener;
    private int responseCode;
    private Map<String, List<String>> headerMap;

    public Downloadable(String urlString, String destinationPath) {

        this(urlFromString(urlString), destinationPath);
    }

    public Downloadable(URL url, String destinationPath) {

        if ((url == null) || (destinationPath == null)) throw new InvalidParameterException("URL and Destination path should not be null");

        mTargetUrl = url;
        mDestinationPath = destinationPath;
    }

    public URL getTargetUrl() { return mTargetUrl; }

    public String getDestinationPath() { return mDestinationPath; }

    public void setTag(Integer tag) { mTag = tag; }
    public Integer getTag() { return mTag; }

    private static URL urlFromString(String urlString) {

        try {

            return new URL(urlString);
        }
        catch (MalformedURLException e) {

            e.printStackTrace();
        }

        return null;
    }

    public void setOnDownloadListener(OnDownloadListener listener)
    {
        mDownloadListener = listener;
    }
    public OnDownloadListener getOnDownloadListener()
    {
        return mDownloadListener;
    }

    public void onDownloadComplete()
    {
        if (mDownloadListener != null) {
            mDownloadListener.onDownloadComplete(this);
        }
    }

    public void onDownloadFailure(int responseCode, Map<String, List<String>> headerMap)
    {
        if (mDownloadListener != null) {
            mDownloadListener.onDownloadFailure(this, responseCode, headerMap);
        }
    }

    public Map<String, List<String>> getHeaders()
    {
        return headerMap;
    }

    void setHeaders(Map<String, List<String>> headerMap)
    {
        this.headerMap = headerMap;
    }

    public int getResponseCode()
    {
        return responseCode;
    }

    void setResponseCode(int responseCode)
    {
        this.responseCode = responseCode;
    }

    // OnDownloadListener
    public interface OnDownloadListener
    {
        void onDownloadComplete(Downloadable downloadable);
        void onDownloadFailure(Downloadable downloadable, int responseCode, Map<String, List<String>> headerMap);
    }
}
