package cn.iocoder.yudao.module.wms.dal.dataobject.outbound;

import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;

/**
 * 出库单 DO
 * @author 李方捷
 * @table-fields : code,company_id,remark,outbound_time,audit_status,creator_comment,type,source_bill_id,source_bill_code,latest_outbound_action_id,outbound_status,source_bill_type,id,dept_id,warehouse_id
 */
@TableName("wms_outbound")
// 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@KeySequence("wms_outbound_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WmsOutboundDO extends BaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;

    /**
     * 仓库ID
     */
    private Long warehouseId;

    /**
     * 类型
     */
    private Integer type;

    /**
     * 审核状态
     */
    private Integer auditStatus;

    /**
     * 来源单据ID
     */
    private Long sourceBillId;

    /**
     * 来源单据类型
     */
    private Integer sourceBillType;

    /**
     * 特别说明，创建方专用
     */
    private String creatorComment;

    /**
     * 状态
     */
    private Integer outboundStatus;

    /**
     * 库存财务公司ID
     */
    private Long companyId;

    /**
     * 库存归属部门ID
     */
    private Long deptId;

    /**
     * 出库时间
     */
    private LocalDateTime outboundTime;

    /**
     * 出库动作ID，与flow关联
     */
    private Long latestOutboundActionId;

    /**
     * 备注
     */
    private String remark;

    /**
     * 单据号
     */
    private String code;

    /**
     * 来源单据号
     */
    private String sourceBillCode;
}
