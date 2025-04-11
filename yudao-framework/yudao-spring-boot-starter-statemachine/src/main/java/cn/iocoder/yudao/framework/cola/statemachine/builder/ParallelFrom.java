package cn.iocoder.yudao.framework.cola.statemachine.builder;


public interface ParallelFrom<S, E, C> {
    /**
     * Build transition target state and return to clause builder
     * @param stateIds id of state
     * @return To clause builder
     */
    To<S, E, C> toAmong(S ... stateIds);

}
