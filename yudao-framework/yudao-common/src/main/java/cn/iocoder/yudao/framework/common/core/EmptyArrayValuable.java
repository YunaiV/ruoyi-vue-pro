package cn.iocoder.yudao.framework.common.core;

/**
 * 空实现的 ArrayValuable，用于 InEnum 注解的默认值
 *
 * @author Ken
 */
public enum EmptyArrayValuable implements ArrayValuable<Object> {
    ;

    @Override
    public Object[] array() {
        return new Object[0];
    }
}
