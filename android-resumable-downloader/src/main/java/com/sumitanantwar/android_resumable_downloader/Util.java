package com.sumitanantwar.android_resumable_downloader;

import java.security.MessageDigest;

/**
 * Created by Sumit Anantwar on 2/21/17.
 */

public class Util {

    public static String generateHashFromString(String url)
    {
        try
        {
            byte[] urlBytes = url.getBytes("UTF-8");

            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digestedBytes = md.digest(urlBytes);

            return encodeHex(digestedBytes);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return null;
    }

    protected static String encodeHex(final byte[] data) {

        char[] toDigits = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

        final int l = data.length;
        final char[] out = new char[l << 1];
        // two characters form the hex value.
        for (int i = 0, j = 0; i < l; i++) {
            out[j++] = toDigits[(0xF0 & data[i]) >>> 4];
            out[j++] = toDigits[0x0F & data[i]];
        }
        return new String(out);
    }
}
