package com.sumitanantwar.android_resumable_downloader;

/**
 * Created by Sumit Anantwar on 7/13/16.
 */
public enum RetryMode
{
    DownloadComplete,
    IncompleteDownload,
    ShouldRetry,
    HostNotFound,
    ConnectionError,
    RetriesConsumed,
    OutOfMemory
}
