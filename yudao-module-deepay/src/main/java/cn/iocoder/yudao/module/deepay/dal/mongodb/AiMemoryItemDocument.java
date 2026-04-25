package cn.iocoder.yudao.module.deepay.dal.mongodb;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Map;

/**
 * 结构化长久记忆（MongoDB）。
 *
 * <p>按 module 分类：
 * <ul>
 *   <li>design  — 风格/尺码/禁忌偏好</li>
 *   <li>sales   — 购买动机/反对点/沟通风格</li>
 *   <li>finance — 付款偏好/预算范围</li>
 *   <li>order   — 订单/物流偏好</li>
 * </ul>
 * </p>
 *
 * <p>默认不设 TTL（永久保留），但支持用户/合规删除。</p>
 * <p>集合名：{@code ai_memory_item}</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "ai_memory_item")
@CompoundIndexes({
    @CompoundIndex(name = "idx_tenant_customer_module",
                   def  = "{'tenantId': 1, 'customerId': 1, 'module': 1}",
                   unique = true)
})
public class AiMemoryItemDocument {

    @Id
    private String id;

    /** 租户 ID */
    private Long tenantId;

    /** 用户 / 客户 ID */
    private Long customerId;

    /**
     * 记忆分类（design / sales / finance / order）。
     * 一个 customerId + module 只保留一份记忆（upsert）。
     */
    private String module;

    /**
     * 结构化记忆内容。
     * <ul>
     *   <li>design：{stylePreference, sizePreference, taboos}</li>
     *   <li>sales：{buyingMotivation, objections, communicationStyle}</li>
     *   <li>finance：{paymentPreference, budgetRange}</li>
     *   <li>order：{shippingPreference, deliveryNotes}</li>
     * </ul>
     */
    private Map<String, Object> facts;

    /** 创建时间 */
    private Instant createdAt;

    /** 最后更新时间 */
    private Instant updatedAt;

}
