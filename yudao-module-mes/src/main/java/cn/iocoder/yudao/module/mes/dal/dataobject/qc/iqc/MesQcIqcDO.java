package cn.iocoder.yudao.module.mes.dal.dataobject.qc.iqc;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.item.MesMdItemDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.vendor.MesMdVendorDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.qc.template.MesQcTemplateDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.arrivalnotice.MesWmArrivalNoticeDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.arrivalnotice.MesWmArrivalNoticeLineDO;
import cn.iocoder.yudao.module.mes.enums.qc.MesQcCheckResultEnum;
import cn.iocoder.yudao.module.mes.enums.qc.MesQcSourceDocTypeEnum;
import cn.iocoder.yudao.module.mes.enums.qc.MesQcStatusEnum;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import cn.iocoder.yudao.module.mes.enums.DictTypeConstants;

/**
 * MES 来料检验单（IQC, Incoming Quality Control） DO
 *
 * @author 芋道源码
 */
@TableName("mes_qc_iqc")
@KeySequence("mes_qc_iqc_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MesQcIqcDO extends BaseDO {

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
     * 来源单据类型
     *
     * 字典 {@link DictTypeConstants#MES_QC_SOURCE_DOC_TYPE}
     * 枚举 {@link MesQcSourceDocTypeEnum}
     */
    private Integer sourceDocType;
    /**
     * 来源单据 ID
     *
     * 关联：根据 {@link #sourceDocType} 不同：
     * 1. {@link MesQcSourceDocTypeEnum#ARRIVAL_NOTICE} 时，关联 {@link MesWmArrivalNoticeDO#getId()}
     */
    private Long sourceDocId;
    /**
     * 来源单据行 ID
     *
     * 关联：根据 {@link #sourceDocType} 不同：
     * 1. {@link MesQcSourceDocTypeEnum#ARRIVAL_NOTICE} 时，关联 {@link MesWmArrivalNoticeLineDO#getId()}
     */
    private Long sourceLineId;
    /**
     * 来源单据编号（冗余）
     */
    private String sourceDocCode;

    // ========== 供应商 ==========

    /**
     * 供应商 ID
     *
     * 关联 {@link MesMdVendorDO#getId()}
     */
    private Long vendorId;
    /**
     * 供应商批次号
     */
    private String vendorBatch;

    // ========== 物料 ==========

    /**
     * 产品物料 ID
     *
     * 关联 {@link MesMdItemDO#getId()}
     */
    private Long itemId;

    // ========== 数量 ==========

    /**
     * 本次接收数量
     */
    private BigDecimal receivedQuantity;
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
     * 字典 {@link DictTypeConstants#MES_QC_CHECK_RESULT}
     * 枚举 {@link MesQcCheckResultEnum}
     */
    private Integer checkResult;
    /**
     * 来料日期
     */
    private LocalDateTime receiveDate;
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
     * 字典 {@link DictTypeConstants#MES_ORDER_STATUS}
     * 枚举 {@link MesQcStatusEnum}
     */
    private Integer status;
    /**
     * 备注
     */
    private String remark;

}
