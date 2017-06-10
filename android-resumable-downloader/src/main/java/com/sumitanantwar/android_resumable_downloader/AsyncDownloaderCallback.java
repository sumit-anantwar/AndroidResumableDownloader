package com.sumitanantwar.android_resumable_downloader;

import java.util.List;

/**
 * Created by Sumit Anantwar on 2/21/17.
 */

public interface AsyncDownloaderCallback {

    void onDownloadComplete(List<Processable> processables);
    void onDownloadProgress(long completedBytes, long totalBytes);
    void onDownloadFailure(Exception e);
}
