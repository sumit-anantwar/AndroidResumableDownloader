package com.sumitanantwar.android_resumable_downloader;

import java.util.List;

/**
 * Created by Sumit Anantwar on 7/14/16.
 */
public interface RetryResponse
{
    void needsRetry(List<Processable> processables);
//    void retryNotNeeded(RetryMode retryMode);
    void retryNotNeeded(Throwable e);
}
