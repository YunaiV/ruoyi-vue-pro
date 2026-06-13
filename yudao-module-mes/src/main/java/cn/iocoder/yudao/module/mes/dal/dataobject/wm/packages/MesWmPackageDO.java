package cn.iocoder.yudao.module.mes.dal.dataobject.wm.packages;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.client.MesMdClientDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.unitmeasure.MesMdUnitMeasureDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import cn.iocoder.yudao.module.mes.enums.DictTypeConstants;

/**
 * MES 装箱单 DO
 *
 * @author 芋道源码
 */
@TableName("mes_wm_package")
@KeySequence("mes_wm_package_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MesWmPackageDO extends BaseDO {

    public static final Long PARENT_ID_ROOT = 0L;

    @TableId
    private Long id;
    /**
     * 装箱单编号
     */
    private String code;
    /**
     * 父箱 ID
     */
    private Long parentId;
    /**
     * 装箱日期
     */
    private LocalDateTime packageDate;
    /**
     * 销售订单编号
     */
    private String salesOrderCode;
    /**
     * 发票编号
     */
    private String invoiceCode;
    /**
     * 客户 ID
     *
     * 关联 {@link MesMdClientDO#getId()}
     */
    private Long clientId;
    /**
     * 箱长度
     */
    private BigDecimal length;
    /**
     * 箱宽度
     */
    private BigDecimal width;
    /**
     * 箱高度
     */
    private BigDecimal height;
    /**
     * 尺寸单位 ID
     *
     * 关联 {@link MesMdUnitMeasureDO#getId()}
     */
    private Long sizeUnitId;
    /**
     * 净重
     */
    private BigDecimal netWeight;
    /**
     * 毛重
     */
    private BigDecimal grossWeight;
    /**
     * 重量单位 ID
     *
     * 关联 {@link MesMdUnitMeasureDO#getId()}
     */
    private Long weightUnitId;
    /**
     * 检查员用户 ID
     *
     * 关联 AdminUserDO 的 id
     */
    private Long inspectorUserId;
    /**
     * 状态
     *
     * 字典 {@link DictTypeConstants#MES_WM_PACKAGE_STATUS}
     * 枚举 {@link cn.iocoder.yudao.module.mes.enums.wm.MesWmPackageStatusEnum}
     */
    private Integer status;
    /**
     * 备注
     */
    private String remark;

}
