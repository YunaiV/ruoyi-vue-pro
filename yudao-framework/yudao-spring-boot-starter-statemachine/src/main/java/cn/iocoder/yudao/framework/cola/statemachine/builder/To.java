package cn.iocoder.yudao.framework.cola.statemachine.builder;

/**
 * To
 *
 * @author Frank Zhang
 * @date 2020-02-07 6:14 PM
 */
public interface To<S, E, C> {
    /**
     * Build transition event
     * @param event transition event
     * @return On clause builder
     */
    On<S, E, C> on(E event);
}
