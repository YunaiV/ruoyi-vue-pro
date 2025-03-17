package cn.iocoder.yudao.module.wms.dal.dataobject.inbound;

import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;

/**
 * 入库单 DO
 *
 * @author 李方捷
 */
@TableName("wms_inbound")
@KeySequence("wms_inbound_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WmsInboundDO extends BaseDO {

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
     * 入库单类型
     */
    private Integer type;
    /**
     * 仓库ID
     */
    private Long warehouseId;
    /**
     * 状态
     */
    private String status;
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
     * 参考号
     */
    private String referNo;
    /**
     * 跟踪号
     */
    private String traceNo;
    /**
     * 运输方式，1-海运；2-火车；3-空运；4、集卡
     */
    private Integer shippingMethod;
    /**
     * 预计到货时间
     */
    private LocalDateTime planArrivalTime;
    /**
     * 实际到货时间
     */
    private LocalDateTime actualArrivalTime;
    /**
     * 特别说明，创建方专用
     */
    private String creatorComment;
    /**
     * 初始库龄
     */
    private Integer initAge;

}