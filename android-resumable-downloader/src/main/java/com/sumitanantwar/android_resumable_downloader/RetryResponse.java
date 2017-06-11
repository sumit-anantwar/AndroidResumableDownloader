package com.sumitanantwar.android_resumable_downloader;

import java.util.List;
import java.util.Map;

/**
 * Created by Sumit Anantwar on 7/14/16.
 */
public interface RetryResponse
{
    void needsRetry(Map<Downloadable, Processable> processableMap);
    void retryNotNeeded(Throwable e);
}
