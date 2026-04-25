package cn.iocoder.yudao.module.deepay.vo;

import lombok.Data;

/**
 * AI 对话上下文注入 VO（Context Injection v1）。
 *
 * <p>前端每次发送消息时，附带此上下文 JSON，后端将其注入到 prompt 中，
 * 让 AI 理解用户当前所在页面和正在操作的业务对象。</p>
 *
 * <pre>
 * 使用示例（前端 URLSearchParams）：
 *   context={"route":"/order/detail","module":"order","entityType":"order","entityId":"123","snapshot":{"status":"PAID","amount":99.00}}
 * </pre>
 */
@Data
public class AiChatContextVO {

    /** 当前页面路由路径（如 /order/detail）*/
    private String route;

    /**
     * 模块名（selection/design/product/inventory/finance/trend/order/global）。
     * 若与 URL param module 不一致，以此为准（前端优先）。
     */
    private String module;

    /**
     * 实体类型（order / product / customer / paymentLink / inventory / design 等）。
     * 后端据此知道要查哪张表。
     */
    private String entityType;

    /**
     * 实体 ID（对应 entityType 的主键值，字符串化）。
     * 后端可用此 ID 查询数据库补充快照。
     */
    private String entityId;

    /**
     * 页面快照（前端已有的字段 JSON，避免后端再查）。
     * 格式：任意 JSON 对象字符串，由后端解析为 Map 注入 prompt。
     */
    private String snapshot;
}
