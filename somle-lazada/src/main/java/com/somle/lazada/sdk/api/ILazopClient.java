package com.somle.lazada.sdk.api;

import com.somle.lazada.sdk.util.ApiException;

public interface ILazopClient {
    LazopResponse execute(LazopRequest var1) throws ApiException;

    LazopResponse execute(LazopRequest var1, String var2) throws ApiException;
}