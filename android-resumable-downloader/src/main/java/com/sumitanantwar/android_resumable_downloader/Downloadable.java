package com.sumitanantwar.android_resumable_downloader;

import android.support.annotation.NonNull;

import java.net.MalformedURLException;
import java.net.URL;
import java.security.InvalidParameterException;

/**
 * Created by Sumit Anantwar on 2/6/17.
 */

public class Downloadable {

    private final URL mTargetUrl;
    private final String mDestinationPath;
    private Integer mTag = null;

    public Downloadable(String urlString, String destinationPath) {

        this(urlFromString(urlString), destinationPath);
    }

    public Downloadable(URL url, String destinationPath) {

        if ((url == null) || (destinationPath == null)) throw new InvalidParameterException("URL and Destination path should not be null");

        mTargetUrl = url;
        mDestinationPath = destinationPath;
    }

    public URL getTargetUrl() {
        return mTargetUrl;
    }

    public String getDestinationPath() {
        return mDestinationPath;
    }

    public void setTag(Integer tag) {
        mTag = tag;
    }

    public Integer getTag() {
        return mTag;
    }

    private static URL urlFromString(String urlString) {

        try {

            return new URL(urlString);
        }
        catch (MalformedURLException e) {

            e.printStackTrace();
        }

        return null;
    }
}
