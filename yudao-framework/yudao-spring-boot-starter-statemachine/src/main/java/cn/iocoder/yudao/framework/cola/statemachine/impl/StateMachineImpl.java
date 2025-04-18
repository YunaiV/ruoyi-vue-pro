package cn.iocoder.yudao.framework.cola.statemachine.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import cn.iocoder.yudao.framework.cola.statemachine.State;
import cn.iocoder.yudao.framework.cola.statemachine.StateMachine;
import cn.iocoder.yudao.framework.cola.statemachine.Transition;
import cn.iocoder.yudao.framework.cola.statemachine.Visitor;
import cn.iocoder.yudao.framework.cola.statemachine.builder.FailCallback;

/**
 * For performance consideration,
 * The state machine is made "stateless" on purpose.
 * Once it's built, it can be shared by multi-thread
 * <p>
 * One side effect is since the state machine is stateless, we can not get current state from State Machine.
 *
 * @author Frank Zhang
 * @date 2020-02-07 5:40 PM
 */
public class StateMachineImpl<S, E, C> implements StateMachine<S, E, C> {

    private String machineId;

    private final Map<S, State<S, E, C>> stateMap;

    private boolean ready;

    private FailCallback<S, E, C> failCallback;



    /**
     * 状态属性值提取器(可选）
     **/
    private Function<C,S> getter;

    @Override
    public void setGetter(Function<C,S> getter) {
        this.getter = getter;
    }

    public StateMachineImpl(Map<S, State<S, E, C>> stateMap) {
        this.stateMap = stateMap;
    }

    @Override
    public boolean verify(S sourceStateId, E event) {
        isReady();

        State sourceState = getState(sourceStateId);

        List<Transition<S, E, C>> transitions = sourceState.getEventTransitions(event);

        return transitions != null && transitions.size() != 0;
    }

    public S fireEvent(E event, C ctx) {
        if (getter != null) {
            return fireEvent(getter.apply(ctx), event, ctx);
        }

        throw new RuntimeException("getter is null");
    }

    @Override
    public S fireEvent(S sourceStateId, E event, C ctx) {
        isReady();
        Transition<S, E, C> transition = routeTransition(sourceStateId, event, ctx);


        if (transition == null) {
            Debugger.debug("There is no Transition for " + event);
            failCallback.onFail(sourceStateId,null, event, ctx);
            return sourceStateId;
        } else {
            if(transition.getConditionResult()!=null && !transition.getConditionResult()) {
                failCallback.onFail(sourceStateId,transition.getTarget().getId(), event, ctx);
                return sourceStateId;
            }
        }


        return transition.transit(ctx, false).getId();
    }
    @Override
    public List<S> fireParallelEvent(S sourceState, E event, C context) {
        isReady();
        List<Transition<S, E, C>> transitions = routeTransitions(sourceState, event, context);
        List<S> result = new ArrayList<>();
        if (transitions == null||transitions.isEmpty()) {
            Debugger.debug("There is no Transition for " + event);
            failCallback.onFail(sourceState,null, event, context);
            result.add(sourceState);
            return result;
        }
        for (Transition<S, E, C> transition : transitions) {
            S id = transition.transit(context, false).getId();
            result.add(id);
        }
        return result;
    }

    private Transition<S, E, C> routeTransition(S sourceStateId, E event, C ctx) {
        State sourceState = getState(sourceStateId);

        List<Transition<S, E, C>> transitions = sourceState.getEventTransitions(event);

        if (transitions == null || transitions.size() == 0) {
            return null;
        }

        Transition<S, E, C> transit = null;
        for (Transition<S, E, C> transition : transitions) {
            if (transition.getCondition() == null) {
                transit = transition;
            } else {
                Boolean conditionResult=transition.getCondition().isSatisfied(ctx);
                transition.setConditionResult(conditionResult);
                transit = transition;
                break;
            }
        }


        return transit;
    }
    private List<Transition<S,E,C>> routeTransitions(S sourceStateId, E event, C context) {
        State sourceState = getState(sourceStateId);
        List<Transition<S, E, C>> result = new ArrayList<>();
        List<Transition<S, E, C>> transitions = sourceState.getEventTransitions(event);
        if (transitions == null || transitions.size() == 0) {
            return null;
        }

        for (Transition<S, E, C> transition : transitions) {
            Transition<S, E, C> transit = null;
            if (transition.getCondition() == null) {
                transit = transition;
            } else if (transition.getCondition().isSatisfied(context)) {
                transit = transition;
            } else {
                failCallback.onFail(sourceStateId, transition.getTarget().getId(), event, context);
            }
            result.add(transit);
        }
        return result;
    }

    private State getState(S currentStateId) {
        State state = StateHelper.getState(stateMap, currentStateId);
        if (state == null) {
            showStateMachine();
            throw new StateMachineException(currentStateId + " is not found, please check state machine");
        }
        return state;
    }

    private void isReady() {
        if (!ready) {
            throw new StateMachineException("State machine is not built yet, can not work");
        }
    }

    @Override
    public String accept(Visitor visitor) {
        StringBuilder sb = new StringBuilder();
        sb.append(visitor.visitOnEntry(this));
        for (State state : stateMap.values()) {
            sb.append(state.accept(visitor));
        }
        sb.append(visitor.visitOnExit(this));
        return sb.toString();
    }

    @Override
    public void showStateMachine() {
        SysOutVisitor sysOutVisitor = new SysOutVisitor();
        accept(sysOutVisitor);
    }

    @Override
    public String generatePlantUML() {
        PlantUMLVisitor plantUMLVisitor = new PlantUMLVisitor();
        return accept(plantUMLVisitor);
    }

    @Override
    public String getMachineId() {
        return machineId;
    }

    public void setMachineId(String machineId) {
        this.machineId = machineId;
    }

    public void setReady(boolean ready) {
        this.ready = ready;
    }

    public void setFailCallback(FailCallback<S, E, C> failCallback) {
        this.failCallback = failCallback;
    }

}
