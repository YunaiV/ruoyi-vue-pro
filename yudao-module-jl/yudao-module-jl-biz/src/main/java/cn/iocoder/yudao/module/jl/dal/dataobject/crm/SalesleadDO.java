package cn.iocoder.yudao.module.jl.dal.dataobject.crm;

import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;

/**
 * 销售线索 DO
 *
 * @author 惟象科技
 */
@TableName("jl_crm_saleslead")
@KeySequence("jl_crm_saleslead_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalesleadDO extends BaseDO {

    /**
     * 岗位ID
     */
    @TableId
    private Long id;
    /**
     * 销售线索来源
     *
     * 枚举 {@link TODO sales_lead_source 对应的类}
     */
    private String source;
    /**
     * 关键需求
     */
    private String requirement;
    /**
     * 预算(元)
     */
    private Long budget;
    /**
     * 报价
     */
    private Long quotation;
    /**
     * 状态
     *
     * 枚举 {@link TODO sales_lead_status 对应的类}
     */
    private Integer status;
    /**
     * 客户id
     */
    private Long customerId;
    /**
     * 项目id
     */
    private Long projectId;

}
