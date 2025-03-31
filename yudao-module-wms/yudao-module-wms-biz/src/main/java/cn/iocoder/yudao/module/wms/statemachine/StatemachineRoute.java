package cn.iocoder.yudao.module.wms.statemachine;

/**
 * @author: LeeFJ
 * @date: 2025/3/31 9:25
 * @description:状态机路由
 */
public class StatemachineRoute<S,E> {
    private S from;
    private E event;
    private S to;
}
