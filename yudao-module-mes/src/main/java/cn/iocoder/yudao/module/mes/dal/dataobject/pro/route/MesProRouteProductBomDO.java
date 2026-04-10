package cn.iocoder.yudao.module.mes.dal.dataobject.pro.route;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.pro.process.MesProProcessDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.math.BigDecimal;

/**
 * MES 工艺路线产品 BOM DO
 *
 * @author 芋道源码
 */
@TableName("mes_pro_route_product_bom")
@KeySequence("mes_pro_route_product_bom_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MesProRouteProductBomDO extends BaseDO {

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
     * 工序编号
     *
     * 关联 {@link MesProProcessDO#getId()}
     */
    private Long processId;
    /**
     * 产品物料编号
     *
     * 关联 {@link cn.iocoder.yudao.module.mes.dal.dataobject.md.item.MesMdItemDO#getId()}
     */
    private Long productId;
    /**
     * BOM 物料编号
     *
     * 关联 {@link cn.iocoder.yudao.module.mes.dal.dataobject.md.item.MesMdItemDO#getId()}
     */
    private Long itemId;
    /**
     * 用料比例
     */
    private BigDecimal quantity;
    /**
     * 备注
     */
    private String remark;

}
