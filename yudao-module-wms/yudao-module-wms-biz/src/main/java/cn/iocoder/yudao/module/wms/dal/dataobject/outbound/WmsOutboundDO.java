package cn.iocoder.yudao.module.wms.dal.dataobject.outbound;

import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;

/**
 * 出库单 DO
 *
 * @author 李方捷
 */
@TableName("wms_outbound")
@KeySequence("wms_outbound_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
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
     * 单据号
     */
    private String no;
    /**
     * 仓库ID
     */
    private Long warehouseId;
    /**
     * 类型
     */
    private Integer type;
    /**
     * 状态
     */
    private String status;
    /**
     * 审核状态
     */
    private Integer auditStatus;
    /**
     * 来源单据ID
     */
    private Long sourceBillId;
    /**
     * 来源单据号
     */
    private String sourceBillNo;
    /**
     * 来源单据类型
     */
    private Integer sourceBillType;
    /**
     * 特别说明，创建方专用
     */
    private String creatorComment;

}