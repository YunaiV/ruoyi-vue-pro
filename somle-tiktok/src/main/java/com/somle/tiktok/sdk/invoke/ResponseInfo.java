package com.somle.tiktok.sdk.invoke;

import com.google.gson.annotations.SerializedName;

public class ResponseInfo {
    @SerializedName("code")
    int code;
    @SerializedName("message")
    String message;
    @SerializedName("data")
    TokenInfo data;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public TokenInfo getData() {
        return data;
    }

    public void setData(TokenInfo data) {
        this.data = data;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ResponseInfo {\n");
        sb.append("  code=").append(code).append(",\n");
        sb.append("  message='").append(message).append("',\n");
        sb.append("  data=").append(data != null ? data.toString().replace("\n", "\n  ") : "null").append("\n");
        sb.append("}");
        return sb.toString();
    }
}