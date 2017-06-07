package com.sumitanantwar.android_resumable_downloader;

import java.net.URL;
import java.security.MessageDigest;

/**
 * Created by Sumit Anantwar on 2/8/17.
 */

public class Processable extends Downloadable {

    private String cacheFilPath;
    private long downloadedContentSize;
    private long totalContentSize;
    private boolean hostFound;


    public Processable(Downloadable downloadable)
    {
        this(downloadable.getTargetUrl(), downloadable.getDestinationPath());
        this.setOnDownloadListener(downloadable.getOnDownloadListener());
        setTag(downloadable.getTag());
    }

    public Processable(String url, String destinationPath) {

        super(url, destinationPath);
    }

    public Processable(URL url, String destinationPath) {

        super(url, destinationPath);
    }

    public long getDownloadedContentSize() {
        return downloadedContentSize;
    }

    public void setDownloadedContentSize(long downloadedContentSize) {
        this.downloadedContentSize = downloadedContentSize;
    }

    public long getTotalContentSize() {
        return totalContentSize;
    }

    public void setTotalContentSize(long totalContentSize) {
        this.totalContentSize = totalContentSize;
    }

    public boolean getHostFound() {
        return hostFound;
    }

    public void setHostFound(boolean hostFound) {
        this.hostFound = hostFound;
    }

    public String getCacheFilePath() {
        return this.cacheFilPath;
    }

    public void setCacheFilPath(String cacheFilPath) {
        this.cacheFilPath = cacheFilPath;
    }

    long getPendingContentSize() {

        return (hostFound) ? (totalContentSize -downloadedContentSize) : 0;
    }
}
