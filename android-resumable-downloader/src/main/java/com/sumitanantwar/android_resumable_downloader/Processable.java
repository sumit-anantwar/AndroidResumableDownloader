package com.sumitanantwar.android_resumable_downloader;

import java.net.URL;

/**
 * Created by Sumit Anantwar on 2/8/17.
 */

public class Processable extends Downloadable {

    private long mDownloadedContentSize;
    private long mTotalContentSize;

    public Processable(String url, String destinationPath) {

        super(url, destinationPath);
    }

    public Processable(URL url, String destinationPath) {

        super(url, destinationPath);
    }



    public long getDownloadedContentSize() {
        return mDownloadedContentSize;
    }

    public void setmDownloadedContentSize(long downloadedContentSize) {
        this.mDownloadedContentSize = downloadedContentSize;
    }

    public long getTotalContentSize() {
        return mTotalContentSize;
    }

    public void setTotalContentSize(long totalContentSize) {
        this.mTotalContentSize = totalContentSize;
    }
}
