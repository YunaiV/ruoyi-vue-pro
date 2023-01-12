package cn.iocoder.yudao.framework.desensitize.handler;

public class RegexDesensitizationHandler implements DesensitizationHandler {

    @Override
    public String desensitize(String origin, Object... arg) {
        String regex = (String) arg[0];
        String replacer = (String) arg[1];

        return origin.replaceAll(regex, replacer);
    }

}
