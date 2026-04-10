package cn.iocoder.yudao.module.mes.dal.dataobject.md.item;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * MES 物料批次属性配置 DO
 *
 * @author 芋道源码
 */
@TableName("mes_md_item_batch_config")
@KeySequence("mes_md_item_batch_config_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MesMdItemBatchConfigDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 物料编号
     *
     * 关联 {@link MesMdItemDO#getId()}
     */
    private Long itemId;
    /**
     * 批次属性-生产日期
     */
    private Boolean produceDateFlag;
    /**
     * 批次属性-有效期
     */
    private Boolean expireDateFlag;
    /**
     * 批次属性-入库日期
     */
    private Boolean receiptDateFlag;
    /**
     * 批次属性-供应商
     */
    private Boolean vendorFlag;
    /**
     * 批次属性-客户
     */
    private Boolean clientFlag;
    /**
     * 批次属性-销售订单编号
     */
    private Boolean salesOrderCodeFlag;
    /**
     * 批次属性-采购订单编号
     */
    private Boolean purchaseOrderCodeFlag;
    /**
     * 批次属性-生产工单
     */
    private Boolean workOrderFlag;
    /**
     * 批次属性-生产任务
     */
    private Boolean taskFlag;
    /**
     * 批次属性-工作站
     */
    private Boolean workstationFlag;
    /**
     * 批次属性-工具
     */
    private Boolean toolFlag;
    /**
     * 批次属性-模具
     */
    private Boolean moldFlag;
    /**
     * 批次属性-生产批号
     */
    private Boolean lotNumberFlag;
    /**
     * 批次属性-质量状态
     */
    private Boolean qualityStatusFlag;

}
