package cn.iocoder.yudao.framework.common.enums;


/**
 * 环境
 */
public enum EnvEnum {

    LOCAL("本地环境"),
    DEV("开发环境"),
    TEST("测试环境"),
    PROD( "生产环境"),
    UNKNOWN("未识别的环境");

    private String text;

    EnvEnum(String text) {
        this.text = text;
    }


    public static EnvEnum parse(String code) {
        if (code == null) {
            return UNKNOWN;
        }

        for (EnvEnum e : EnvEnum.values()) {
            if (e.code().equalsIgnoreCase(code)) {
                return e;
            }
        }

        return UNKNOWN;
    }

    /**
     * 是否生产环境
     * */
    public boolean isProdEnv() {
        return this==PROD;
    }

    /**
     * 是否本地环境
     * */
    public boolean isLocalEnv() {
        return this==LOCAL;
    }

    /**
     * 是否测试环境
     * */
    public boolean isTestEnv() {
        return this==TEST;
    }

    /**
     * 是否开发环境
     * */
    public boolean isDevEnv() {
        return this==TEST;
    }

    public String code() {
        return this.name().toLowerCase();
    }


    /**
     * 是否匹配任意一个
     **/
    public boolean isAnyMatch(EnvEnum... envEnums) {
        boolean matched = false;
        for (EnvEnum envEnum : envEnums) {
            if(envEnum==this) {
                matched=true;
                break;
            }
        }
        return matched;
    }


    private static EnvEnum current=null;

    /**
     * 当前环境
     * */
    public static EnvEnum current() {
        return current;
    }

    public static void current(EnvEnum c) {
        if(current!=null) {
            throw new RuntimeException("不允许重复设置");
        }
        current=c;
    }

    public String text() {
        return text;
    }

}
