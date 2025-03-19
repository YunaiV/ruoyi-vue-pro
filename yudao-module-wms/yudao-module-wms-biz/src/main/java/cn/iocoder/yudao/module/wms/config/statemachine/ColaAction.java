package cn.iocoder.yudao.module.wms.config.statemachine;

import com.alibaba.cola.statemachine.Action;
import com.alibaba.cola.statemachine.Condition;
import lombok.Getter;

import java.util.function.Function;

/**
 * @author: LeeFJ
 * @date: 2025/2/28 17:07
 * @description:
 */
@Getter
public abstract class ColaAction<S, E,D>  {


    public static class ColaActionWhen<D> implements Condition<ColaContext<D>> {
        private final ColaAction<?,?,D> colaAction;
        public ColaActionWhen(ColaAction<?,?,D> colaAction) {
            this.colaAction=colaAction;
        }

        @Override
        public boolean isSatisfied(ColaContext<D> context) {
            boolean when=colaAction.when(context);
            context.handled(true);
            return when;
        }

    }

    private static class ColaActionPerform<S, E, D> implements Action<S, E, ColaContext<D>> {
        private final ColaAction<S,E,D> colaAction;
        public ColaActionPerform(ColaAction<S,E,D> colaAction) {
            this.colaAction=colaAction;
        }


        @Override
        public void execute(S from, S to, E event, ColaContext<D> context) {
            this.colaAction.perform(from, to, event, context);
        }
    }



    private final S from;
    private final S to;
    private final E event;
    private Function<D,S> getter;

    public ColaAction(S from, S to, Function<D,S> getter, E event) {
        this.from = from;
        this.to = to;
        this.event = event;
        this.getter = getter;
    }

    public ColaActionWhen<D> getColaActionWhen() {
        return new ColaActionWhen<>(this);
    }
    public ColaActionPerform<S,E,D> getColaActionPerform() {
        return new ColaActionPerform<>(this);
    }

    public abstract boolean when(ColaContext<D> context);

    public abstract void perform(S from, S to, E event, ColaContext<D> context);




}
