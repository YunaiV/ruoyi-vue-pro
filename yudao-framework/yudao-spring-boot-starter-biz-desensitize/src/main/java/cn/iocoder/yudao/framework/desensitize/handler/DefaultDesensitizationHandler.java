package cn.iocoder.yudao.framework.desensitize.handler;

public class DefaultDesensitizationHandler implements DesensitizationHandler {

    @Override
    public String handle(String origin) {
        return origin;
    }

}
