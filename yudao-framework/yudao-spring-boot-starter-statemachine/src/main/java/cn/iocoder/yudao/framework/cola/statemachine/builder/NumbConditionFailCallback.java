package cn.iocoder.yudao.framework.cola.statemachine.builder;

/**
 * Default fail callback, do nothing.
 *
 * @author 龙也
 * @date 2022/9/15 12:02 PM
 */
public class NumbConditionFailCallback<S, E, C> {

    public void onFail(S sourceState, S targetState, E event, C context) {
        //do nothing
    }
}
