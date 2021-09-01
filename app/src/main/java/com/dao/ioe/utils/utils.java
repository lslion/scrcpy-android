package com.dao.ioe.utils;

import android.util.Log;

import com.dao.ioe.scrcpy.BuildConfig;

public class utils {
    private static String prefix = "shuai_";
    public static void logd(String tag, String msg) {
//        if (Log.isLoggable(tag, Log.DEBUG)) {
//            Log.d(tag, msg);
//        }

        if (BuildConfig.DEBUG) {
            Log.d(prefix +tag, msg);
        }
    }

    /**
     * Converts a series of bytes to a raw long variable which can be both positive and negative.
     * This method currently only supports 64-bit long variable.
     *
     * @param src The source bytes.
     * @param offset The position of the first byte of the data to be converted. The data is base
     *     256 with the most significant digit first.
     * @param length The length of the data to be converted. It must be <= 8.
     * @throws IllegalArgumentException If {@code length} is bigger than 8.
     * @throws IndexOutOfBoundsException If the range defined by {@code offset} and {@code length}
     *     exceeds the bounds of {@code src}.
     */
    public static long bytesToRawLong(byte[] src, int offset, int length) {
        if (length > 8) {
            throw new IllegalArgumentException(
                    "length must be <= 8 (only 64-bit long supported): " + length);
        }
        if (offset < 0 || length < 0 || offset + length > src.length) {
            throw new IndexOutOfBoundsException(
                    "Out of the bounds: src=["
                            + src.length
                            + "], offset="
                            + offset
                            + ", length="
                            + length);
        }
        long result = 0;
        for (int i = 0; i < length; i++) {
            result = (result << 8) | (src[offset + i] & 0xFF);
        }
        return result;
    }

    /**
     * Converts a series of bytes to an integer. This method currently only supports positive 32-bit
     * integers.
     *
     * @param src The source bytes.
     * @param offset The position of the first byte of the data to be converted. The data is base
     *     256 with the most significant digit first.
     * @param length The length of the data to be converted. It must be <= 4.
     * @throws IllegalArgumentException If {@code length} is bigger than 4 or {@code src} cannot be
     *     parsed as a positive integer.
     * @throws IndexOutOfBoundsException If the range defined by {@code offset} and {@code length}
     *     exceeds the bounds of {@code src}.
     */
    public static int bytesToInt(byte[] src, int offset, int length) {
        if (length > 4) {
            throw new IllegalArgumentException(
                    "length must be <= 4 (only 32-bit integer supported): " + length);
        }
        if (offset < 0 || length < 0 || offset + length > src.length) {
            throw new IndexOutOfBoundsException(
                    "Out of the bounds: src=["
                            + src.length
                            + "], offset="
                            + offset
                            + ", length="
                            + length);
        }
        int result = 0;
        for (int i = 0; i < length; i++) {
            result = (result << 8) | (src[offset + i] & 0xFF);
        }
        if (result < 0) {
            logd("lslion", "positive result is: " + result);
            result = Math.abs(result);
//            throw new IllegalArgumentException(
//                    "src cannot be parsed as a positive integer: " + result);
        }
        return result;
    }
}
