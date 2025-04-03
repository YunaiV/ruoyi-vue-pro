package cn.iocoder.yudao.framework.cola.statemachine;

/**
 * Generic strategy interface used by a state machine to respond
 * events by executing an {@code Action} with a {@link StateContext}.
 *
 * @author Cheng Tao
 * @date
 */
public interface Handler<S, E, C> extends Action<S, E, C> , Condition<C> {

}