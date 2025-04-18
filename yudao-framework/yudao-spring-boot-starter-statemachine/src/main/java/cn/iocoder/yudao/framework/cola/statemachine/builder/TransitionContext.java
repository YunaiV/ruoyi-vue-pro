package cn.iocoder.yudao.framework.cola.statemachine.builder;

import java.util.HashMap;
import java.util.Map;

/**
 * @author: LeeFJ
 * @date: 2025/4/7 10:12
 * @description:
 */
public class TransitionContext<T> {

    /**
     * 创建上下文
     **/
    public static <D> TransitionContext<D> from(D requestDO) {
        TransitionContext<D> context = new TransitionContext<>();
        context.data=requestDO;
        return context;
    }

    /**
     * 事件数据
     **/
    private T data;
    /**
     * 其它额外数据
     **/
    private Map<String,Object> extraMap = new HashMap<>();


    /**
     * 获取事件数据
     **/
    public T data() {
        return data;
    }

    public Object setExtra(String key,Object extra) {
        return extraMap.put(key,extra);
    }

    public Object getExtra(String key) {
        return extraMap.get(key);
    }



}
