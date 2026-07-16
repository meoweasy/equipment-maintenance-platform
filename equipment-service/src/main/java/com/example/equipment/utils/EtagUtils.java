package com.example.equipment.utils;

import com.example.equipment.exception.EtagMismatchException;
import com.example.equipment.exception.EtagRequiredException;
import com.example.equipment.utils.ksuuid.KsuidVersion;

public final class EtagUtils {

    private EtagUtils() {
    }

    public static void validateIfMatch(String ifMatch, KsuidVersion currentEtag) {
        if (ifMatch == null || ifMatch.isBlank()) {
            throw new EtagRequiredException();
        }

        String candidate = ifMatch.trim();
        if (candidate.startsWith("W/")) {
            throw new EtagMismatchException();
        }
        if (candidate.length() >= 2 && candidate.startsWith("\"") && candidate.endsWith("\"")) {
            candidate = candidate.substring(1, candidate.length() - 1);
        }
        if (currentEtag == null || !currentEtag.toString().equals(candidate)) {
            throw new EtagMismatchException();
        }
    }

    public static String value(KsuidVersion etag) {
        return etag == null ? null : etag.toString();
    }
}
