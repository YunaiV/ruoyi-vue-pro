package cn.iocoder.yudao.module.wms.dal.dataobject.exchange.defective;

import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;

/**
 * 良次换货详情 DO
 *
 * @author 李方捷
 */
@TableName("wms_exchange_defective")
@KeySequence("wms_exchange_defective_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WmsExchangeDefectiveDO extends BaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;
    /**
     * 换货单ID
     */
    private Long exchangeId;
    /**
     * 标准产品ID
     */
    private Long productId;
    /**
     * 源仓位ID
     */
    private Long fromBinId;
    /**
     * 目的仓位ID
     */
    private Long toBinId;
    /**
     * 换货量
     */
    private Integer qty;
    /**
     * 备注
     */
    private String remark;

}