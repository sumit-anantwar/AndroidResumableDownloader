package com.sumitanantwar.android_resumable_downloader;

/**
 * Created by Sumit Anantwar on 6/10/17.
 */

public enum DownloadRequestError
{
    ConnectionError,
    TargetNotFoundError,
    AccessForbiddenError,
    OutOfMermoryError,
    InternalError,
    RetriesConsumedError
}
