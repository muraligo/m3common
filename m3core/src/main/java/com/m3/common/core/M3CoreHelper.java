package com.m3.common.core;

import java.security.SecureRandom;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.BiFunction;

public final class M3CoreHelper {
    private static Random _RANDGEN = null;

    public synchronized static void generateRandom(byte[] bary) {
        RANDOM_GENERATOR();
        _RANDGEN.nextBytes(bary);
    }

    public synchronized static int generateRandomInt() {
        RANDOM_GENERATOR();
        return _RANDGEN.nextInt();
    }

    public synchronized static Random RANDOM_GENERATOR() {
        if (_RANDGEN == null) {
            synchronized (M3CoreHelper.class) {
                if (_RANDGEN == null) {
                    _RANDGEN = new SecureRandom();
                }
            }
        }
        return _RANDGEN;
    }

	public static <T> void convertDelimitedStringToMap(String delimstr, String delimiter, String kvseparator, Map<String, T> dest, BiFunction<String, String, T> converter) {
        String[] starr = delimstr.strip().split(delimiter);
        if (starr == null || starr.length <= 0) return;
        for (int ix = 0; ix < starr.length; ix++) {
            if (starr[ix] != null && !starr[ix].isBlank()) {
                String[] kvpair = starr[ix].strip().split(kvseparator);
                if (kvpair != null && kvpair.length > 2 && kvpair[0] != null && !kvpair[0].isBlank() && kvpair[1] != null && !kvpair[1].isBlank()) {
                    String key = kvpair[0].strip();
                    dest.put(key, converter.apply(key, kvpair[1].strip()));
                }
            }
        }
    }

    @SuppressWarnings("unchecked") // for just casting to String when converter is null
	public static <T> void convertDelimitedStringToList(String delimstr, String delimiter, String service, List<T> dest, BiFunction<String, String, T> converter) {
        String[] starr = delimstr.strip().split(delimiter);
        if (starr == null || starr.length <= 0) return;
        for (int ix = 0; ix < starr.length; ix++) {
            if (starr[ix] != null && !starr[ix].isBlank()) {
                dest.add(converter == null ? (T)starr[ix].strip() : converter.apply(service, starr[ix].strip()));
            }
        }
    }
}
