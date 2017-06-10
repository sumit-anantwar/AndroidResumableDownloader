package com.sumitanantwar.android_resumable_downloader;

import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.util.List;
import java.util.Map;

/**
 * Created by Sumit Anantwar on 2/8/17.
 */

public class Processable extends Downloadable {

    private String cacheFilPath;
    private long downloadedContentSize;
    private long totalContentSize;
    private int responseCode;
    private Map<String, List<String>> headerMap;


    Processable(Downloadable downloadable)
    {
        super(downloadable.getTargetUrl(), downloadable.getDestinationPath());
        setOnDownloadListener(downloadable.getOnDownloadListener());
        setTag(downloadable.getTag());
    }

    void setDownloadedContentSize(long downloadedContentSize) {
        this.downloadedContentSize = downloadedContentSize;
    }
    long getDownloadedContentSize() {
        return downloadedContentSize;
    }

    long getTotalContentSize() {
        return totalContentSize;
    }

    void setTotalContentSize(long totalContentSize) {
        this.totalContentSize = totalContentSize;
    }

    String getCacheFilePath() {
        return this.cacheFilPath;
    }

    void setCacheFilPath(String cacheFilPath) {
        this.cacheFilPath = cacheFilPath;
    }

    long getPendingContentSize() {

        return (responseCode == HttpURLConnection.HTTP_OK) ? (totalContentSize -downloadedContentSize) : 0;
    }

    Map<String, List<String>> getHeaderMap()
    {
        return headerMap;
    }

    void setHeaderMap(Map<String, List<String>> headerMap)
    {
        this.headerMap = headerMap;
    }

    int getResponseCode()
    {
        return responseCode;
    }

    void setResponseCode(int responseCode)
    {
        this.responseCode = responseCode;
    }
}
