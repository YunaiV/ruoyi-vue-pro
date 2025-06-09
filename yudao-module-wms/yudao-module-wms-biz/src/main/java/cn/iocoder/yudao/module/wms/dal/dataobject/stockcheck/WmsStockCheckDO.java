package cn.iocoder.yudao.module.wms.dal.dataobject.stockcheck;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * 盘点 DO
 * @author 李方捷
 * @table-fields : code,remark,id,audit_status,warehouse_id
 */
@TableName("wms_stock_check")
// 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@KeySequence("wms_stock_check_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WmsStockCheckDO extends BaseDO {

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
     * 审批状态 : 0-起草中 , 1-待审批 , 2-已驳回 , 3-已通过
     */
    private Integer auditStatus;

    /**
     * 单据号
     */
    private String code;

    /**
     * 创建者备注
     */
    private String remark;
}
