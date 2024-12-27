package com.somle.framework.common.util.io;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

@Data
public class SomleResponse<T> {
    private final int code;
    private final T BodyData;

    @Getter
    @AllArgsConstructor
    public enum ResponseType {
        //如果响应体非常大，读取为一个字符串可能会导致内存消耗过多，甚至 OutOfMemoryError
        STRING("String"),
        BYTES("Bytes"),
        BYTE_STRING("ByteString");

        private final String description;
    }
}
