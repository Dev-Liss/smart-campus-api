package com.smartcampus.model;

public class ApiErrorResponse {

    private String error;
    private String message;
    private long timestamp;

    public ApiErrorResponse(String error, String message) {
        this.error = error;
        this.message = message;
        this.timestamp = System.currentTimeMillis();
    }

    public String getError() {
        return error;
    }

    public String getMessage() {
        return message;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
