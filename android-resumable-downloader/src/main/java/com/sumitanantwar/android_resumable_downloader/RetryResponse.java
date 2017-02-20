package com.sumitanantwar.android_resumable_downloader;

/**
 * Created by Sumit Anantwar on 7/14/16.
 */
public interface RetryResponse
{
    void needsRetry(long downloadedContentSize);
    void retryNotNeeded(RetryMode retryMode);
}
