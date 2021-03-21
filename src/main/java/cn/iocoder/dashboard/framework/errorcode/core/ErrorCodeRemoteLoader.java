package cn.iocoder.dashboard.framework.errorcode.core;

public interface ErrorCodeRemoteLoader {

    /**
     * 全量加载 ErrorCode 错误码
     */
    void loadErrorCodes();

    /**
     * 增量加载 ErrorCode 错误码
     */
    void refreshErrorCodes();
}
