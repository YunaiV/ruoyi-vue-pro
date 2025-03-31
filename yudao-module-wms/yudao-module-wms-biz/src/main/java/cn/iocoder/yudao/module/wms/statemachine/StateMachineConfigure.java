package cn.iocoder.yudao.module.wms.statemachine;


import cn.iocoder.yudao.framework.common.util.spring.SpringUtils;
import com.alibaba.cola.statemachine.Action;
import com.alibaba.cola.statemachine.Condition;
import com.alibaba.cola.statemachine.builder.FailCallback;
import com.alibaba.cola.statemachine.builder.StateMachineBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: LeeFJ
 * @date: 2025/3/19 10:47
 * @description:
 */
public interface StateMachineConfigure<S, E, D> {

    /**
     * 初始化状态机
     **/
    default List<ColaTransition<S, E, D>> initActions(StateMachineBuilder<S, E, D> builder, Class actionType, FailCallback<S,E,D> failCallback) {

        Map<S,List<S>> conditionMap = new HashMap<>();
        List<ColaTransition<S, E, D>> actions = SpringUtils.getBeans(actionType);
        if(actions==null) {
            return actions;
        }
        for (ColaTransition<S, E, D> action : actions) {
            bindAction(builder,action,conditionMap);
        }
        builder.setFailCallback(failCallback);
        return actions;

    }


    /**
     * 绑定状态机动作
     **/
    default void bindAction(StateMachineBuilder<S, E, D> builder, ColaTransition<S,E,D> action, Map<S,List<S>> conditionMap) {

        S[] from = action.getFrom();

        // 搜集状态映射关系
        List<S> list= conditionMap.get(action.getTo());
        if(list==null) {
            list = new ArrayList<>();
            conditionMap.put(action.getTo(),list);
        }

        // 为每个 Action 配置条件判断函数和执行函数
        for (S item : from) {
            list.add(item);
            builder.externalTransition()
                // 设置状态走向
                .from(item).to(action.getTo())
                // 设置事件
                .on(action.getEvent())
                // 设置条件判断函数
                .when((Condition<D>) action.getColaActionWhen())
                // 执行变更
                .perform((Action<S, E, D>) action.getColaActionPerform());
        }


    }

}
