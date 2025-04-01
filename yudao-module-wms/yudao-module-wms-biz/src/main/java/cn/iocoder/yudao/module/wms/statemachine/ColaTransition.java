package cn.iocoder.yudao.module.wms.statemachine;

import cn.iocoder.yudao.framework.common.util.spring.SpringUtils;
import com.alibaba.cola.statemachine.Action;
import com.alibaba.cola.statemachine.Condition;
import com.alibaba.cola.statemachine.builder.FailCallback;
import com.alibaba.cola.statemachine.builder.StateMachineBuilder;
import lombok.Getter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * @author: LeeFJ
 * @date: 2025/2/28 17:07
 * @description: 状态机事件动作
 * @param <S> 状态
 * @param <E> 事件
 * @param <D> 数据对象
 */
@Getter
public abstract class ColaTransition<S, E, D>  {


    /**
     * 承接状态机事件的条件判断
     * @param <D> 数据对象
     */
    public static class ColaActionWhen<D> implements Condition<ColaContext<D>> {
        private final ColaTransition<?,?,D> colaTransition;
        public ColaActionWhen(ColaTransition<?,?,D> colaTransition) {
            this.colaTransition = colaTransition;
        }

        @Override
        public boolean isSatisfied(ColaContext<D> context) {
            boolean when= colaTransition.when(context);
            context.isSatisfied(true);
            return when;
        }

    }

    /**
     * 承接状态机动作
     **/
    private static class ColaActionPerform<S, E, D> implements Action<S, E, ColaContext<D>> {
        private final ColaTransition<S,E,D> colaTransition;
        public ColaActionPerform(ColaTransition<S,E,D> colaTransition) {
            this.colaTransition = colaTransition;
        }


        @Override
        public void execute(S from, S to, E event, ColaContext<D> context) {
            this.colaTransition.perform(from, to, event, context);
            context.isPerformed(true);
        }
    }


    /**
     * 起始状态
     **/
    private final S[] from;

    /**
     * 结束状态
     **/
    private final S to;

    /**
     * 触发事件
     **/
    private final E event;

    /**
     * 状态值的获取方法
     **/
    private Function<D,S> getter;


    public ColaTransition(S[] from, S to, Function<D,S> getter, E event) {
        this.from = from;
        this.to = to;
        this.event = event;
        this.getter = getter;
    }

    /**
     * 给状态机提供 when
     **/
    public ColaActionWhen<D> getColaActionWhen() {
        return new ColaActionWhen<>(this);
    }
    /**
     * 给状态机提供 perform
     **/
    public ColaActionPerform<S,E,D> getColaActionPerform() {
        return new ColaActionPerform<>(this);
    }

    /**
     * 在子类中实现条件判断
     **/
    public boolean when(ColaContext<D> context) {
        return true;
    }

    /**
     * 在子类中实现业务动作
     **/
    public abstract void perform(S from, S to, E event, ColaContext<D> context);




}
