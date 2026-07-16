package com.example.platform.common.etag;

import com.example.platform.common.exception.EtagMismatchException;
import com.example.platform.common.exception.EtagRequiredException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EtagUtilsTest {

    private static final String ETAG = "36mYwPp7Kx4X1rQc2fJ8NvAbCdE";

    @Test
    void acceptsQuotedCurrentEtag() {
        assertDoesNotThrow(() -> EtagUtils.validateIfMatch("\"" + ETAG + "\"", new KsuidVersion(ETAG)));
    }

    @Test
    void rejectsMissingEtag() {
        assertThrows(EtagRequiredException.class,
                () -> EtagUtils.validateIfMatch(null, new KsuidVersion(ETAG)));
    }

    @Test
    void rejectsDifferentEtag() {
        assertThrows(EtagMismatchException.class,
                () -> EtagUtils.validateIfMatch("\"another-version\"", new KsuidVersion(ETAG)));
    }

    @Test
    void rejectsWeakEtagForIfMatch() {
        assertThrows(EtagMismatchException.class,
                () -> EtagUtils.validateIfMatch("W/\"" + ETAG + "\"", new KsuidVersion(ETAG)));
    }
}
