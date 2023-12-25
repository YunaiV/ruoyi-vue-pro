package cn.iocoder.yudao.framework.operatelogv2.core.enums;

/**
 * 操作日志常量接口
 *
 * @author HUIHUI
 */
public interface OperateLogV2Constants {

    // ========== 开关字段-如果没有值默认就是记录 ==========

    /**
     * 是否记录日志
     */
    String ENABLE = "enable";

    /**
     * 是否记录方法参数
     */
    String IS_LOG_ARGS = "isLogArgs";

    /**
     * 是否记录方法结果的数据
     */
    String IS_LOG_RESULT_DATA = "isLogResultData";

    // ========== 扩展 ==========

    /**
     * 扩展信息
     */
    String EXTRA_KEY = "extra";

}
