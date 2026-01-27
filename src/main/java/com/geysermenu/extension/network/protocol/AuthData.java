package com.geysermenu.extension.network.protocol;

/**
 * Authentication request/response data
 */
public class AuthData {

    private String secretKey;
    private String serverIdentifier;
    private boolean success;
    private String message;

    public AuthData() {}

    public static AuthData request(String secretKey, String serverIdentifier) {
        AuthData data = new AuthData();
        data.secretKey = secretKey;
        data.serverIdentifier = serverIdentifier;
        return data;
    }

    public static AuthData success(String message) {
        AuthData data = new AuthData();
        data.success = true;
        data.message = message;
        return data;
    }

    public static AuthData failure(String message) {
        AuthData data = new AuthData();
        data.success = false;
        data.message = message;
        return data;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public String getServerIdentifier() {
        return serverIdentifier;
    }

    public void setServerIdentifier(String serverIdentifier) {
        this.serverIdentifier = serverIdentifier;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
