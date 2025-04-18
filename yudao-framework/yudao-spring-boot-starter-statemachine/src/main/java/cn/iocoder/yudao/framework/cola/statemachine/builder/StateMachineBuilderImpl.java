package cn.iocoder.yudao.framework.cola.statemachine.builder;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import cn.iocoder.yudao.framework.cola.statemachine.State;
import cn.iocoder.yudao.framework.cola.statemachine.StateMachine;
import cn.iocoder.yudao.framework.cola.statemachine.StateMachineFactory;
import cn.iocoder.yudao.framework.cola.statemachine.impl.StateMachineImpl;
import cn.iocoder.yudao.framework.cola.statemachine.impl.TransitionType;

/**
 * StateMachineBuilderImpl
 *
 * @author Frank Zhang
 * @date 2020-02-07 9:40 PM
 */
public class StateMachineBuilderImpl<S, E, C> implements StateMachineBuilder<S, E, C> {

    /**
     * StateMap is the same with stateMachine, as the core of state machine is holding reference to states.
     */
    private final Map<S, State<S, E, C>> stateMap = new ConcurrentHashMap<>();
    private final StateMachineImpl<S, E, C> stateMachine = new StateMachineImpl<>(stateMap);
    private FailCallback<S, E, C> failCallback = new NumbFailCallback<>();

    @Override
    public ExternalTransitionBuilder<S, E, C> externalTransition() {
        return new TransitionBuilderImpl<>(stateMap, TransitionType.EXTERNAL);
    }

    @Override
    public ExternalTransitionsBuilder<S, E, C> externalTransitions() {
        return new TransitionsBuilderImpl<>(stateMap, TransitionType.EXTERNAL);
    }

    @Override
    public ExternalParallelTransitionBuilder<S, E, C> externalParallelTransition() {
        return new ParallelTransitionBuilderImpl<>(stateMap, TransitionType.EXTERNAL);
    }

    @Override
    public InternalTransitionBuilder<S, E, C> internalTransition() {
        return new TransitionBuilderImpl<>(stateMap, TransitionType.INTERNAL);
    }

    @Override
    public void setFailCallback(FailCallback<S, E, C> callback) {
        this.failCallback = callback;
    }

//    @Override
//    public void setConditionFailCallback(ConditionFailCallback<S, E, C> callback) {
//        this.conditionFailCallback = callback;
//    }

    @Override
    public StateMachine<S, E, C> build(String machineId) {
        stateMachine.setMachineId(machineId);
        stateMachine.setReady(true);
        stateMachine.setFailCallback(failCallback);
        // stateMachine.setCondtionFailCallback(conditionFailCallback);
        StateMachineFactory.register(stateMachine);
        return stateMachine;
    }

}
