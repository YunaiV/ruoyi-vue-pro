package cn.iocoder.yudao.framework.cola.statemachine;

/**
 * Condition
 *
 * @author Frank Zhang
 * @date 2020-02-07 2:50 PM
 */
public interface Condition<C> {

    /**
     * @param context context object
     * @return whether the context satisfied current condition
     */
    boolean isSatisfied(C context);

    default String name(){
        return this.getClass().getSimpleName();
    }
}