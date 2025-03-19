package cn.iocoder.yudao.module.wms.dal.dataobject.approval.history;

import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;

/**
 * 审批历史 DO
 * @author 李方捷
 * @table-fields : bill_id,status_after,status_type,bill_type,comment,id,status_before
 */
@TableName("wms_approval_history")
// 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@KeySequence("wms_approval_history_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WmsApprovalHistoryDO extends BaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;

    /**
     * 代码
     */
    private Integer billType;

    /**
     * 名称
     */
    private Long billId;

    /**
     * 状态类型
     */
    private String statusType;

    /**
     * 审批前的状态
     */
    private Integer statusBefore;

    /**
     * 审批后状态
     */
    private Integer statusAfter;

    /**
     * 审批意见
     */
    private String comment;
}
