package cn.iocoder.yudao.module.wms.dal.dataobject.inbound.item;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 入库单详情 QueryDO
 * @author 李方捷
 */
@TableName("wms_inbound_item")
// 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@KeySequence("wms_inbound_item_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class WmsInboundItemLogicDO extends BaseDO {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 仓库ID
     */
    private Long warehouseId;

    /**
     * 产品ID
     */
    private Long productId;

    /**
     * 库龄
     */
    private Integer age;

    /**
     * 入库时间
     */
    private LocalDateTime inboundTime;

    /**
     * 主单公司ID
     */
    private Long companyId;

    /**
     * 主单部门ID
     */
    private Long deptId;

    /**
     * 库位ID
     */
    private Long binId;

    /**
     * 可售量，未被单据占用的良品数量
     */
    private Integer sellableQty;





}
