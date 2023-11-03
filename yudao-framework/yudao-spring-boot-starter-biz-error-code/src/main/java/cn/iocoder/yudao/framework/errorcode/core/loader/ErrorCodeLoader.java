package cn.iocoder.yudao.framework.errorcode.core.loader;

import cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil;

/**
 * 错误码加载器
 *
 * 注意，错误码最终加载到 {@link ServiceExceptionUtil} 的 MESSAGES 变量中！
 *
 * @author dlyan
 */
public interface ErrorCodeLoader {

    /**
     * 添加错误码
     *
     * @param code 错误码的编号
     * @param msg 错误码的提示
     */
    default void putErrorCode(Integer code, String msg) {
        ServiceExceptionUtil.put(code, msg);
    }

}
