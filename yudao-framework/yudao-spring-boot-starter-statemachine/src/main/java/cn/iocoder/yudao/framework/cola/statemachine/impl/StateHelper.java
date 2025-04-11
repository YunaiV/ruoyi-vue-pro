package cn.iocoder.yudao.framework.cola.statemachine.impl;

import cn.iocoder.yudao.framework.cola.statemachine.State;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * StateHelper
 *
 * @author Frank Zhang
 * @date 2020-02-08 4:23 PM
 */
public class StateHelper {
    public static <S, E, C> State<S, E, C> getState(Map<S, State<S, E, C>> stateMap, S stateId){
        State<S, E, C> state = stateMap.get(stateId);
        if (state == null) {
            state = new StateImpl<>(stateId);
            stateMap.put(stateId, state);
        }
        return state;
    }
    public static <C, E, S> List<State<S,E,C>> getStates(Map<S, State<S,E,C>> stateMap, S ... stateIds) {
        List<State<S, E, C>> result = new ArrayList<>();
        for (S stateId : stateIds) {
            State<S, E, C> state = getState(stateMap, stateId);
            result.add(state);
        }
        return result;
    }
}
