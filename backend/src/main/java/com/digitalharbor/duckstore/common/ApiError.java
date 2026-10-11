package com.digitalharbor.duckstore.common;

import java.util.List;

/** Uniform error body returned for every failed request. */
public record ApiError(int status, String message, List<String> details) {
    public static ApiError of(int status, String message) {
        return new ApiError(status, message, List.of());
    }
}
