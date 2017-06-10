package com.sumitanantwar.android_resumable_downloader;

import java.util.List;

/**
 * Created by Sumit Anantwar on 6/10/17.
 */

public interface DownloadRequestCallback
{
    void onDownloadComplete();
    void onDownloadProgress(long completedBytes, long totalBytes);
    void onDownloadIncomplete(List<Downloadable> incompleteDownloadables);
    void onDownloadFailure(DownloadRequestError error);
}
