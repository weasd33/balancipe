package com.beokay.balancipe.global.common;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
        boolean success,
        String message,
        T data
) {
    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, null, data);
    }

    public static ApiResponse<Void> ok() {
        return new ApiResponse<>(true, null, null);
    }

    public static ApiResponse<Void> fail(String message) {
        return new ApiResponse<>(false, message, null);
    }

    public static ApiResponse<Void> error(String message) {
        return new ApiResponse<>(false, message, null);
    }
}
