package cn.iocoder.yudao.framework.cola.statemachine;

import cn.iocoder.yudao.framework.common.util.spring.SpringUtils;
import com.alibaba.cola.statemachine.StateMachine;
import com.alibaba.cola.statemachine.Visitor;
import com.alibaba.cola.statemachine.builder.FailCallback;
import com.alibaba.cola.statemachine.builder.StateMachineBuilder;
import com.alibaba.cola.statemachine.builder.StateMachineBuilderFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * @author: LeeFJ
 * @date: 2025/2/28 14:16
 * @description: 状态机包装类
 */
public class StateMachineWrapper<S, E, D> implements StateMachine<S, E, TransitionContext<D>> {

    /**
     * 状态机
     **/
    private StateMachine<S, E, TransitionContext<D>> stateMachine;

    private List<TransitionHandler<S, E, D>> transitions = new ArrayList<>();


    /**
     * 状态属性值提取器
     **/
    private Function<D,S> getter;

    private StateMachineBuilder<S, E, TransitionContext<D>> builder;

    /**
     * 构造函数
     **/
    public StateMachineWrapper(String name,Function<D,S> getter) {

        this.getter = getter;
        // 创建状态机构建器
        this.builder = StateMachineBuilderFactory.create();
        // 创建状态机
        this.stateMachine = builder.build(name);

    }


    /**
     * 设置外部状态转换,多个起始状态
     **/
    public StateMachineWrapper<S, E, D> bindExternals(S[] from, E event, S to, Class<? extends TransitionHandler<S, E, D>> baseTransitionClass) {

        TransitionHandler<S, E, D> transition = SpringUtils.getBean(baseTransitionClass);
        // 为每个 Action 配置条件判断函数和执行函数

        builder.externalTransitions().fromAmong(from)
            .to(to)
            // 设置事件
            .on(event)
            // 设置条件判断函数
            .when(transition.getColaActionWhen())
            // 执行变更
            .perform(transition.getColaActionPerform());

        // 设置 Transition
        setupTransition(transition,to, event,from);

        return this;
    }

    /**
     * 设置外部状态转换
     **/
    public StateMachineWrapper<S, E, D> bindExternal(S from, E event, S to, Class<? extends TransitionHandler<S, E, D>> baseTransitionClass) {

        TransitionHandler<S, E, D> transition = SpringUtils.getBean(baseTransitionClass);

        builder.externalTransition()
            // 设置状态走向
            .from(from).to(to)
            // 设置事件
            .on(event)
            // 设置条件判断函数
            .when(transition.getColaActionWhen())
            // 执行变更
            .perform(transition.getColaActionPerform());

        // 设置 Transition
        setupTransition(transition,to, event,from);

        return this;
    }

    /**
     * 设置内部状态转换
     **/
    public StateMachineWrapper<S, E, D> bindInternal(S status, E event, Class<? extends TransitionHandler<S, E, D>> baseTransitionClass) {

        TransitionHandler<S, E, D> transition = SpringUtils.getBean(baseTransitionClass);

        builder.internalTransition()
            // 设置状态走向
            .within(status)
            // 设置事件
            .on(event)
            // 设置条件判断函数
            .when(transition.getColaActionWhen())
            // 执行变更
            .perform(transition.getColaActionPerform());

        // 设置 Transition
        setupTransition(transition,status, event,status);

        return this;
    }

    /**
     * 设置失败回调
     **/
    public StateMachineWrapper<S, E, D> setFailCallback(Class <? extends FailCallback<S, E, TransitionContext<D>>> failCallbackClass) {
        FailCallback<S, E, TransitionContext<D>> failCallback = SpringUtils.getBean(failCallbackClass);
        builder.setFailCallback(failCallback);
        return this;
    }

    /**
     * 设置 Transition
     **/
    private void setupTransition(TransitionHandler<S, E, D> transition, S to,E event, S... from) {
        transition.setFrom(from);
        transition.setTo(to);
        transition.setEvent(event);
        transitions.add(transition);
    }

    /**
     * 校验事件起始状态与事件
     * @return    是否触发
     **/
    @Override
    public boolean verify(S from, E event) {
        return stateMachine.verify(from, event);
    }

    /**
     * 触发事件
     **/
    @Override
    public S fireEvent(S from, E event, TransitionContext<D> ctx) {
        return stateMachine.fireEvent(from,event,ctx);
    }

    /**
     * 触发并行事件
     **/
    @Override
    public List<S> fireParallelEvent(S from, E event, TransitionContext<D> ctx) {
        return stateMachine.fireParallelEvent(from,event,ctx);
    }


    /**
     * 触发事件，起始状态从 利用 getter 方法从上下文的数据对象获取
     **/
    public S fireEvent(E event, TransitionContext<D> ctx) {
        return stateMachine.fireEvent(getter.apply(ctx.data()),event,ctx);
    }


    @Override
    public String getMachineId() {
        return stateMachine.getMachineId();
    }

    @Override
    public void showStateMachine() {
        stateMachine.showStateMachine();
    }

    @Override
    public String generatePlantUML() {
        return stateMachine.generatePlantUML();
    }

    @Override
    public String accept(Visitor visitor) {
        return stateMachine.accept(visitor);
    }

    /**
     * 获得目标状态
     * @param from   起始状态
     * @param event  事件
     */
    public S getTo(S from,E event) {

        for (TransitionHandler<S, E, D> transition : this.transitions) {
            if(transition.getEvent()!=event) {
                continue;
            }
            for (S f : transition.getFrom()) {
                if(from==f) {
                    return transition.getTo();
                }
            }
        }

        return null;
    }

    /**
     * 创建上下文
     */
    public TransitionContext<D> createContext(ApprovalReqVO approvalReqVO, D data) {
        return TransitionContext.from(data,approvalReqVO,this);
    }
}
