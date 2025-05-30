package com.somle.tiktok.sdk.invoke;

import com.google.gson.annotations.SerializedName;

import java.util.Arrays;


public class TokenInfo {
    @SerializedName("access_token")
    String accessToken;
    @SerializedName("access_token_expire_in")
    int accessTokenExpireIn;
    @SerializedName("refresh_token")
    String refreshToken;
    @SerializedName("refresh_token_expire_in")
    int refreshTokenExpireIn;
    @SerializedName("open_id")
    String openId;
    @SerializedName("seller_name")
    String sellerName;
    @SerializedName("seller_base_region")
    String sellerBaseRegion;
    @SerializedName("user_type")
    int userType;
    @SerializedName("granted_scopes")
    String[] grantedScopes;

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public int getAccessTokenExpireIn() {
        return accessTokenExpireIn;
    }

    public void setAccessTokenExpireIn(int accessTokenExpireIn) {
        this.accessTokenExpireIn = accessTokenExpireIn;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public int getRefreshTokenExpireIn() {
        return refreshTokenExpireIn;
    }

    public void setRefreshTokenExpireIn(int refreshTokenExpireIn) {
        this.refreshTokenExpireIn = refreshTokenExpireIn;
    }

    public String getOpenId() {
        return openId;
    }

    public void setOpenId(String openId) {
        this.openId = openId;
    }

    public String getSellerName() {
        return sellerName;
    }

    public void setSellerName(String sellerName) {
        this.sellerName = sellerName;
    }

    public String getSellerBaseRegion() {
        return sellerBaseRegion;
    }

    public void setSellerBaseRegion(String sellerBaseRegion) {
        this.sellerBaseRegion = sellerBaseRegion;
    }

    public int getUserType() {
        return userType;
    }

    public void setUserType(int userType) {
        this.userType = userType;
    }

    public String[] getGrantedScopes() {
        return grantedScopes;
    }

    public void setGrantedScopes(String[] grantedScopes) {
        this.grantedScopes = grantedScopes;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("TokenInfo {\n");
        sb.append("  accessToken='").append(accessToken).append("',\n");
        sb.append("  accessTokenExpireIn=").append(accessTokenExpireIn).append(",\n");
        sb.append("  refreshToken='").append(refreshToken).append("',\n");
        sb.append("  refreshTokenExpireIn=").append(refreshTokenExpireIn).append(",\n");
        sb.append("  openId='").append(openId).append("',\n");
        sb.append("  sellerName='").append(sellerName).append("',\n");
        sb.append("  sellerBaseRegion='").append(sellerBaseRegion).append("',\n");
        sb.append("  userType=").append(userType).append(",\n");
        sb.append("  grantedScopes=").append(Arrays.toString(grantedScopes)).append("\n");
        sb.append("}");
        return sb.toString();
    }
}