package cn.iocoder.yudao.module.deepay.dal.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 客户表 deepay_client（Phase 5 B2B 客户分层）
 */
@TableName("deepay_client")
@Data
public class DeepayClientDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 客户名称 */
    private String name;

    /**
     * 客户等级：
     * A — 大客户（优先供货 + 专属折扣）
     * B — 标准客户
     * C — 限量客户（优先级最低）
     */
    private String level;

    /** 历史累计下单金额（元），用于动态升降级 */
    private BigDecimal totalOrderAmount;

    /** 联系方式 */
    private String contact;

    /** 备注 */
    private String remark;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

}
