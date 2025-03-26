package cn.iocoder.yudao.module.wms.config.statemachine;


import cn.iocoder.yudao.framework.common.util.spring.SpringUtils;
import com.alibaba.cola.statemachine.Action;
import com.alibaba.cola.statemachine.Condition;
import com.alibaba.cola.statemachine.builder.StateMachineBuilder;
import lombok.Getter;

import java.util.List;
import java.util.function.Function;

/**
 * @author: LeeFJ
 * @date: 2025/3/19 10:47
 * @description:
 */
public interface StateMachineConfigure<S, E, D> {

    /**
     * 初始化
     **/
    default void initActions(StateMachineBuilder<S, E, D> builder, Class actionType) {

        List<? extends ColaAction<S, E, D>> actions = SpringUtils.getBeans(actionType);
        if(actions==null) {
            return;
        }
        for (ColaAction<S, E, D> action : actions) {
            bindAction(builder,action);
        }
    }


    default void bindAction(StateMachineBuilder<S, E, D> builder, ColaAction<S,E,D> action) {

        S[] from = action.getFrom();

        for (S fm : from) {
            builder.externalTransition()
                // 设置状态走向
                .from(fm).to(action.getTo())
                // 设置事件
                .on(action.getEvent())
                // 设置条件判断函数
                .when((Condition<D>) action.getColaActionWhen())
                // 执行变更
                .perform((Action<S, E, D>) action.getColaActionPerform());
        }

    }

}
