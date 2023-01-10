package cn.iocoder.yudao.framework.desensitize.handler;

public interface DesensitizationHandler {

    /**
     * 脱敏
     *
     * @param origin 原始字符串
     * @return 脱敏后的字符串
     */
    String handle(String origin);

}
