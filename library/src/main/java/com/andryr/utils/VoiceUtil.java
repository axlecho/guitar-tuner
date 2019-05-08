package com.andryr.utils;

/**
 * 声音计算工具类
 * Created by shine on 18-10-15.
 */

public class VoiceUtil {
    public static double getVolume(byte[] bufferRead, int length) {
        int volume = 0;
        for (int i = 0; i < bufferRead.length; i++) {
            volume += bufferRead[i] * bufferRead[i];
        }
        return volume / (float) length;  //10 * Math.log10(mean);
    }
}
