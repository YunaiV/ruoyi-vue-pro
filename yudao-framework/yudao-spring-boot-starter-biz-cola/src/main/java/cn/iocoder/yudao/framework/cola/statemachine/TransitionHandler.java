package cn.iocoder.yudao.framework.cola.statemachine;

import com.alibaba.cola.statemachine.Action;
import com.alibaba.cola.statemachine.Condition;
import lombok.Data;

import java.util.function.Function;

/**
 * @author: LeeFJ
 * @date: 2025/2/28 17:07
 * @description: 状态机事件动作
 * @param <S> 状态
 * @param <E> 事件
 * @param <D> 数据对象
 */
@Data
public abstract class TransitionHandler<S, E, D> {


    /**
     * 承接状态机事件的条件判断
     * @param <D> 数据对象
     */
    public static class ColaActionWhen<D> implements Condition<TransitionContext<D>> {
        private final TransitionHandler<?,?,D> colaTransition;
        public ColaActionWhen(TransitionHandler<?,?,D> colaTransition) {
            this.colaTransition = colaTransition;
        }

        @Override
        public boolean isSatisfied(TransitionContext<D> context) {
            boolean when= colaTransition.when(context);
            context.isSatisfied(true);
            return when;
        }

    }

    /**
     * 承接状态机动作
     **/
    private static class ColaActionPerform<S, E, D> implements Action<S, E, TransitionContext<D>> {
        private final TransitionHandler<S,E,D> colaTransition;
        public ColaActionPerform(TransitionHandler<S,E,D> colaTransition) {
            this.colaTransition = colaTransition;
        }


        @Override
        public void execute(S from, S to, E event, TransitionContext<D> context) {
            this.colaTransition.perform(from, to, event, context);
            context.isPerformed(true);
        }
    }


    /**
     * 起始状态
     **/
    private S[] from;

    public void setFrom(S... from) {
        this.from = from;
    }

    /**
     * 结束状态
     **/
    private S to;

    /**
     * 触发事件
     **/
    private E event;

    /**
     * 状态值的获取方法
     **/
    private Function<D,S> getter;


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
     * 在子类中实现条件判断, 如需校验在子类中覆盖此方法，默认返回 true
     **/
    public boolean when(TransitionContext<D> context) {
        return true;
    }

    /**
     * 在子类中实现业务动作
     **/
    public abstract void perform(S from, S to, E event, TransitionContext<D> context);




}
