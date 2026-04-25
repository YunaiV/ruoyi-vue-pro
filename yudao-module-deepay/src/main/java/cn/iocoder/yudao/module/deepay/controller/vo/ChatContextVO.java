package cn.iocoder.yudao.module.deepay.controller.vo;

import lombok.Data;

/**
 * 前端上下文注入 VO — 每次 AI 对话时附带当前页面上下文，
 * 让 AI 知道用户正在看什么（订单/商品/客户/收款链接等）。
 *
 * <h3>字段说明</h3>
 * <ul>
 *   <li>{@code route}      — 当前路由路径，如 /order/detail</li>
 *   <li>{@code module}     — 板块，如 order / product / finance</li>
 *   <li>{@code entityType} — 实体类型，如 order / product / customer / paymentLink</li>
 *   <li>{@code entityId}   — 实体 ID，如订单号、商品ID</li>
 *   <li>{@code snapshot}   — 页面已有数据快照（JSON 字符串，可选）</li>
 * </ul>
 */
@Data
public class ChatContextVO {

    /** 当前路由路径（如 /deepay/order/detail）*/
    private String route;

    /** 板块标识（selection/design/product/inventory/finance/trend/order）*/
    private String module;

    /** 实体类型（order/product/customer/paymentLink/design 等）*/
    private String entityType;

    /** 实体 ID（如订单 ID、商品 chainCode 等） */
    private String entityId;

    /**
     * 页面数据快照（JSON 字符串）。
     * 前端在发送消息时可选择附带当前页面的关键数据，
     * 如 {"orderId":"PAY-XXX","status":"PENDING","amount":299}。
     * 后端优先使用此快照；快照不足时根据 entityType+entityId 查询 DB 补充。
     */
    private String snapshot;

    /** 用户界面语言（zh/en），影响 AI 回复语言 */
    private String locale;

}
