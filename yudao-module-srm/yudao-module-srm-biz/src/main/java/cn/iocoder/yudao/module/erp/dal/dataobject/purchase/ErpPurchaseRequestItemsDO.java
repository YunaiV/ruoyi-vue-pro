package cn.iocoder.yudao.module.erp.dal.dataobject.purchase;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * ERP采购申请单子 DO
 *
 * @author 索迈管理员
 */
@TableName("erp_purchase_request_items")
@KeySequence("erp_purchase_request_items_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErpPurchaseRequestItemsDO extends BaseDO {

    /**
     * id
     */
    @TableId
    private Long id;
    /**
     * 商品id
     */
    private Long productId;

    /**
    * 主表id
    */
    private Long requestId;

    /**
     * 申请数量
     */
    private Integer count;

}