package cn.iocoder.yudao.framework.cola.statemachine.impl;

import cn.iocoder.yudao.framework.cola.statemachine.State;
import cn.iocoder.yudao.framework.cola.statemachine.Transition;
import cn.iocoder.yudao.framework.cola.statemachine.Visitor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * StateImpl
 *
 * @author Frank Zhang
 * @date 2020-02-07 11:19 PM
 */
public class StateImpl<S,E,C> implements State<S,E,C> {
    protected final S stateId;
    private EventTransitions eventTransitions = new EventTransitions();

    StateImpl(S stateId){
        this.stateId = stateId;
    }

    @Override
    public Transition<S, E, C> addTransition(E event, State<S,E,C> target, TransitionType transitionType) {
        Transition<S, E, C> newTransition = new TransitionImpl<>();
        newTransition.setSource(this);
        newTransition.setTarget(target);
        newTransition.setEvent(event);
        newTransition.setType(transitionType);

        Debugger.debug("Begin to add new transition: "+ newTransition);

        eventTransitions.put(event, newTransition);
        return newTransition;
    }

    @Override
    public List<Transition<S, E, C>> addTransitions(E event, List<State<S, E, C>> targets, TransitionType transitionType) {
        List<Transition<S, E, C>> result = new ArrayList<>();
        for (State<S, E, C> target : targets) {
            Transition<S, E, C> secTransition = addTransition(event, target, transitionType);
            result.add(secTransition);
        }

        return result;
    }

    @Override
    public List<Transition<S, E, C>> getEventTransitions(E event) {
        return eventTransitions.get(event);
    }

    @Override
    public Collection<Transition<S, E, C>> getAllTransitions() {
        return eventTransitions.allTransitions();
    }

    @Override
    public S getId() {
        return stateId;
    }

    @Override
    public String accept(Visitor visitor) {
        String entry = visitor.visitOnEntry(this);
        String exit = visitor.visitOnExit(this);
        return entry + exit;
    }

    @Override
    public boolean equals(Object anObject){
        if(anObject instanceof State){
            State other = (State)anObject;
            if(this.stateId.equals(other.getId()))
                return true;
        }
        return false;
    }

    @Override
    public String toString(){
        return stateId.toString();
    }
}
