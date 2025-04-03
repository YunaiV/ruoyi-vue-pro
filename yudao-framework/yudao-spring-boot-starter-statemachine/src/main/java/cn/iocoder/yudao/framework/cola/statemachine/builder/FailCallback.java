package cn.iocoder.yudao.framework.cola.statemachine.builder;

/**
 * FailCallback
 *
 * @author 龙也
 * @date 2022/9/15 12:02 PM
 */
@FunctionalInterface
public interface FailCallback<S, E, C> {

    /**
     * Callback function to execute if failed to trigger an Event
     *
     * @param sourceState
     * @param event
     * @param context
     */
    void onFail(S sourceState, E event, C context);
}
