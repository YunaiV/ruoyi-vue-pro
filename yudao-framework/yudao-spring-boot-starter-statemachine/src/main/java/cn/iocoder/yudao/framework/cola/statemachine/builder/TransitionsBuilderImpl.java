package cn.iocoder.yudao.framework.cola.statemachine.builder;

import cn.iocoder.yudao.framework.cola.statemachine.*;
import cn.iocoder.yudao.framework.cola.statemachine.impl.StateHelper;
import cn.iocoder.yudao.framework.cola.statemachine.impl.TransitionType;
import cn.iocoder.yudao.framework.common.util.spring.SpringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * TransitionsBuilderImpl
 *
 * @author Frank Zhang
 * @date 2020-02-08 7:43 PM
 */
public class TransitionsBuilderImpl<S,E,C> extends AbstractTransitionBuilder<S,E,C> implements ExternalTransitionsBuilder<S,E,C> {
    /**
     * This is for fromAmong where multiple sources can be configured to point to one target
     */
    private List<State<S, E, C>> sources = new ArrayList<>();

    private List<Transition<S, E, C>> transitions = new ArrayList<>();

    public TransitionsBuilderImpl(Map<S, State<S, E, C>> stateMap, TransitionType transitionType) {
        super(stateMap, transitionType);
    }

    @Override
    public From<S, E, C> fromAmong(S... stateIds) {
        for(S stateId : stateIds) {
            sources.add(StateHelper.getState(super.stateMap, stateId));
        }
        return this;
    }

    @Override
    public On<S, E, C> on(E event) {
        for(State source : sources) {
            Transition transition = source.addTransition(event, super.target, super.transitionType);
            transitions.add(transition);
        }
        return this;
    }

    @Override
    public When<S, E, C> when(Condition<C> condition) {
        for(Transition transition : transitions){
            transition.setCondition(condition);
        }
        return this;
    }

    @Override
    public void perform(Action<S, E, C> action) {
        for(Transition transition : transitions){
            transition.setAction(action);
        }
    }

    @Override
    public When<S, E, C> handle(Handler<S, E, C> handler) {
        for(Transition transition : transitions){
            transition.setCondition(handler);
            transition.setAction(handler);
        }
        return this;
    }

    @Override
    public When<S, E, C> handle(Class<? extends Handler<S, E, C>> handlerType) {
        Handler<S, E, C> handler = SpringUtils.getBean(handlerType);
        this.handle(handler);
        return this;
    }
}
