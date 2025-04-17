package cn.iocoder.yudao.module.wms.dal.dataobject.pickup;

import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;

/**
 * 拣货单 DO
 * @author 李方捷
 * @table-fields : code,upstream_bill_id,id,upstream_bill_code,warehouse_id,upstream_bill_type
 */
@TableName("wms_pickup")
// 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@KeySequence("wms_pickup_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WmsPickupDO extends BaseDO {

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
     * 单据号
     */
    private String code;

    /**
     * 来源单据ID
     */
    private Long upstreamBillId;

    /**
     * 来源单据号
     */
    private String upstreamBillCode;

    /**
     * 来源单据类型
     */
    private Integer upstreamBillType;
}
