package cn.iocoder.yudao.module.mes.dal.dataobject.qc.oqc;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.client.MesMdClientDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.item.MesMdItemDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.qc.template.MesQcTemplateDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * MES 出货检验单（OQC, Outgoing Quality Control） DO
 *
 * @author 芋道源码
 */
@TableName("mes_qc_oqc")
@KeySequence("mes_qc_oqc_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MesQcOqcDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 检验单编号
     */
    private String code;
    /**
     * 检验单名称
     */
    private String name;
    /**
     * 检验模板 ID
     *
     * 关联 {@link MesQcTemplateDO#getId()}
     */
    private Long templateId;

    // ========== 来源单据 ==========

    /**
     * 来源单据 ID
     *
     * 关联：根据 {@link #sourceDocType} 不同：
     * 1. {@link cn.iocoder.yudao.module.mes.enums.qc.MesQcSourceDocTypeEnum#PRODUCT_SALES} 时，关联 {@link cn.iocoder.yudao.module.mes.dal.dataobject.wm.productsales.MesWmProductSalesDO#getId()}
     */
    private Long sourceDocId;
    /**
     * 来源单据类型
     *
     * 枚举 {@link cn.iocoder.yudao.module.mes.enums.qc.MesQcSourceDocTypeEnum}
     */
    private Integer sourceDocType;
    /**
     * 来源单据行 ID
     *
     * 关联：根据 {@link #sourceDocType} 不同：
     * 1. {@link cn.iocoder.yudao.module.mes.enums.qc.MesQcSourceDocTypeEnum#PRODUCT_SALES} 时，关联 {@link cn.iocoder.yudao.module.mes.dal.dataobject.wm.productsales.MesWmProductSalesLineDO#getId()}
     */
    private Long sourceLineId;
    /**
     * 来源单据编号（冗余）
     */
    private String sourceDocCode;

    // ========== 客户 ==========

    /**
     * 客户 ID
     *
     * 关联 {@link MesMdClientDO#getId()}
     */
    private Long clientId;
    /**
     * 批次号
     */
    private String batchCode;

    // ========== 物料 ==========

    /**
     * 产品物料 ID
     *
     * 关联 {@link MesMdItemDO#getId()}
     */
    private Long itemId;

    // ========== 数量 ==========

    /**
     * 最低检测数
     */
    private Integer minCheckQuantity;
    /**
     * 最大不合格数
     */
    private Integer maxUnqualifiedQuantity;
    /**
     * 本次出货数量
     */
    private BigDecimal outQuantity;
    /**
     * 本次检测数量
     */
    private BigDecimal checkQuantity;
    /**
     * 合格品数量
     */
    private BigDecimal qualifiedQuantity;
    /**
     * 不合格品数量
     */
    private BigDecimal unqualifiedQuantity;

    // ========== 缺陷统计 ==========

    /**
     * 致命缺陷率（%）
     */
    private BigDecimal criticalRate;
    /**
     * 严重缺陷率（%）
     */
    private BigDecimal majorRate;
    /**
     * 轻微缺陷率（%）
     */
    private BigDecimal minorRate;
    /**
     * 致命缺陷数量
     */
    private Integer criticalQuantity;
    /**
     * 严重缺陷数量
     */
    private Integer majorQuantity;
    /**
     * 轻微缺陷数量
     */
    private Integer minorQuantity;

    // ========== 检验 ==========

    /**
     * 检测结果
     *
     * 枚举 {@link cn.iocoder.yudao.module.mes.enums.qc.MesQcCheckResultEnum}
     */
    private Integer checkResult;
    /**
     * 出货日期
     */
    private LocalDateTime outDate;
    /**
     * 检测日期
     */
    private LocalDateTime inspectDate;
    /**
     * 检测人员用户 ID
     *
     * 关联 AdminUserDO 的 id
     */
    private Long inspectorUserId;
    /**
     * 状态
     *
     * 枚举 {@link cn.iocoder.yudao.module.mes.enums.qc.MesQcStatusEnum}
     */
    private Integer status;
    /**
     * 备注
     */
    private String remark;

}
