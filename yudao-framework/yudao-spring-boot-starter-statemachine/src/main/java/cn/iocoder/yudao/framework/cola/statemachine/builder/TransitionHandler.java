package cn.iocoder.yudao.framework.cola.statemachine.builder;

import cn.iocoder.yudao.framework.cola.statemachine.Handler;

public interface TransitionHandler<S, E, C> extends Handler<S, E, C> {

    default void execute(S from, S to, E event, C context) {
        perform(from, to, event, context);
    }

    default boolean isSatisfied(C context) {
        return when(context);
    }

    void perform(S from, S to, E event, C context);

    default boolean when(C context){
        return true;
    }





}
