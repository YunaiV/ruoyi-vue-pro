package cn.iocoder.yudao.framework.desensitize.handler;

/**
 * 脱敏处理器接口
 */
public interface DesensitizationHandler {

    /**
     * 脱敏
     *
     * @param origin 原始字符串
     * @return 脱敏后的字符串
     */
    String desensitize(String origin, Object... arg);

}
