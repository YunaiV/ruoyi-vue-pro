package cn.iocoder.yudao.module.wms.statemachine;

import com.alibaba.cola.statemachine.Action;
import com.alibaba.cola.statemachine.Condition;
import lombok.Getter;

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
public abstract class ColaAction<S, E, D>  {


    /**
     * 承接状态机事件的条件判断
     * @param <D> 数据对象
     */
    public static class ColaActionWhen<D> implements Condition<ColaContext<D>> {
        private final ColaAction<?,?,D> colaAction;
        public ColaActionWhen(ColaAction<?,?,D> colaAction) {
            this.colaAction=colaAction;
        }

        @Override
        public boolean isSatisfied(ColaContext<D> context) {
            boolean when=colaAction.when(context);
            context.isSatisfied(true);
            return when;
        }

    }

    /**
     * 承接状态机动作
     **/
    private static class ColaActionPerform<S, E, D> implements Action<S, E, ColaContext<D>> {
        private final ColaAction<S,E,D> colaAction;
        public ColaActionPerform(ColaAction<S,E,D> colaAction) {
            this.colaAction=colaAction;
        }


        @Override
        public void execute(S from, S to, E event, ColaContext<D> context) {
            this.colaAction.perform(from, to, event, context);
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


    public ColaAction(S[] from, S to, Function<D,S> getter, E event) {
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
    public abstract boolean when(ColaContext<D> context);

    /**
     * 在子类中实现业务动作
     **/
    public abstract void perform(S from, S to, E event, ColaContext<D> context);


}
