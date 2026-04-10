package cn.iocoder.yudao.module.mes.dal.dataobject.pro.route;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.math.BigDecimal;

/**
 * MES 工艺路线产品 DO
 *
 * @author 芋道源码
 */
@TableName("mes_pro_route_product")
@KeySequence("mes_pro_route_product_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MesProRouteProductDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 工艺路线编号
     *
     * 关联 {@link MesProRouteDO#getId()}
     */
    private Long routeId;
    /**
     * 产品物料编号
     *
     * 关联 {@link cn.iocoder.yudao.module.mes.dal.dataobject.md.item.MesMdItemDO#getId()}
     */
    private Long itemId;
    /**
     * 生产数量
     */
    private Integer quantity;
    /**
     * 生产用时
     */
    private BigDecimal productionTime;
    /**
     * 时间单位
     *
     * 字典 {@link cn.iocoder.yudao.module.mes.enums.DictTypeConstants#MES_TIME_UNIT_TYPE}
     * 枚举 {@link cn.iocoder.yudao.module.mes.enums.pro.MesTimeUnitTypeEnum}
     */
    private String timeUnitType;
    /**
     * 备注
     */
    private String remark;

}
