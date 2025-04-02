package cn.iocoder.yudao.framework.cola.statemachine;

import lombok.Getter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: LeeFJ
 * @date: 2025/2/28 15:02
 * @description: 状态机事件上下文
 */

public class TransitionContext<T> {

    /**
     * 创建上下文
     **/
    static <X> TransitionContext<X> from(X requestDO, StateMachineWrapper stateMachineWrapper) {
        TransitionContext<X> context = new TransitionContext<>();
        context.data=requestDO;
        context.success=true;
        context.stateMachineWrapper=stateMachineWrapper;
        return context;
    }

    /**
     * 是否已经匹配条件
     **/
    private Boolean satisfied = false;

    /**
     * 是否已经执行
     **/
    private Boolean performed = false;

    /**
     * 是否执行成功
     **/
    private Boolean success = true;

    /**
     * 事件数据
     **/
    private T data;

    /**
     * 其它额外数据
     **/
    private Map<String,Object> extraMap = new HashMap<>();

    public Object setExtra(String key,Object extra) {
        return extraMap.put(key,extra);
    }

    public Object getExtra(String key) {
        return extraMap.get(key);
    }



    private List<String> errors = new ArrayList<>();

    @Getter
    private StateMachineWrapper stateMachineWrapper;

    /**
     * 是否成功
     **/
    public Boolean success() {
        return success;
    }

    /**
     * 是否失败
     **/
    public Boolean failure() {
        return !success;
    }

    /**
     * 是否匹配条件
     **/
    public Boolean isSatisfied() {
        return satisfied;
    }


    /**
     * 是否匹配条件，包括状态条件与其它数据具备的条件
     **/
    public void isSatisfied(Boolean satisfied) {
        this.satisfied =satisfied;
    }

    /**
     * 是否执行
     **/
    public Boolean isPerformed() {
        return performed;
    }

    /**
     * 设置是否执行
     **/
    public void isPerformed(Boolean performed) {
        this.performed = performed;
    }


    /**
     * 获取事件数据
     **/
    public T data() {
        return data;
    }


    /**
     * 获取错误信息
     **/
    public void addError(String message) {
        errors.add(message);
        success=false;
    }

    /**
     * 获取错误信息
     **/
    private String getDefaultError() {
        if(this.errors.isEmpty()) {
            return null;
        } else {
            return this.errors.get(0);
        }
    }

    /**
     * 根据 from 和 event 获取目标状态
     **/
    public <S,E> S getTo(S from, E event) {
        return (S)stateMachineWrapper.getTo(from,event);
    }
}
