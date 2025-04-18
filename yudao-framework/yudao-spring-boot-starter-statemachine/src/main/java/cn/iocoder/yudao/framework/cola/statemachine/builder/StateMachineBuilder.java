package cn.iocoder.yudao.framework.cola.statemachine.builder;

import cn.iocoder.yudao.framework.cola.statemachine.StateMachine;
import cn.iocoder.yudao.framework.common.util.spring.SpringUtils;

/**
 * StateMachineBuilder
 *
 * @author Frank Zhang
 * @date 2020-02-07 5:32 PM
 */
public interface StateMachineBuilder<S, E, C> {
    /**
     * Builder for one transition
     *
     * @return External transition builder
     */
    ExternalTransitionBuilder<S, E, C> externalTransition();

    /**
     * Builder for multiple transitions
     *
     * @return External transition builder
     */
    ExternalTransitionsBuilder<S, E, C> externalTransitions();
    /**
     * Builder for parallel transitions
     *
     * @return External transition builder
     */
    ExternalParallelTransitionBuilder<S, E, C> externalParallelTransition();

    /**
     * Start to build internal transition
     *
     * @return Internal transition builder
     */
    InternalTransitionBuilder<S, E, C> internalTransition();

    /**
     * set up fail callback, default do nothing {@code NumbFailCallbackImpl}
     *
     * @param callback
     */
    void setFailCallback(FailCallback<S, E, C> callback);

    default void setFailCallback(Class<? extends FailCallback<S, E, C>> callback) {
        setFailCallback(SpringUtils.getBean(callback));
    }

    //void setConditionFailCallback(ConditionFailCallback<S, E, C> callback);

    StateMachine<S, E, C> build(String machineId);

}
