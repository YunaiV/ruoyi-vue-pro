package cn.iocoder.yudao.module.wms.dal.dataobject.pickup;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * 拣货单 DO
 * @author 李方捷
 * @table-fields : code,upstream_id,id,upstream_code,warehouse_id,upstream_type
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
    private Long upstreamId;

    /**
     * 来源单据编码
     */
    private String upstreamCode;

    /**
     * 来源单据类型
     */
    private Integer upstreamType;
}
