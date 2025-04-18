package cn.iocoder.yudao.module.wms.dal.dataobject.exchange;

import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;

/**
 * 换货单 DO
 * @author 李方捷
 * @table-fields : code,remark,id,audit_status,type,warehouse_id
 */
@TableName("wms_exchange")
// 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@KeySequence("wms_exchange_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WmsExchangeDO extends BaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;

    /**
     * 单据号
     */
    private String code;

    /**
     * 类型
     */
    private Integer type;

    /**
     * 调出仓库ID
     */
    private Long warehouseId;

    /**
     * 状态
     */
    private Integer auditStatus;

    /**
     * 特别说明
     */
    private String remark;
}
