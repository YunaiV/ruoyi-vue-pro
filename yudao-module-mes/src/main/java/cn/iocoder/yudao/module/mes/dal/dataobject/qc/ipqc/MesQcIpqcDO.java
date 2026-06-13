package cn.iocoder.yudao.module.mes.dal.dataobject.qc.ipqc;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.item.MesMdItemDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.workstation.MesMdWorkstationDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.pro.feedback.MesProFeedbackDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.pro.workorder.MesProWorkOrderDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.qc.template.MesQcTemplateDO;
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
 * MES 过程检验单（IPQC, In-Process Quality Control） DO
 *
 * @author 芋道源码
 */
@TableName("mes_qc_ipqc")
@KeySequence("mes_qc_ipqc_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MesQcIpqcDO extends BaseDO {

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
     * IPQC 检验类型
     *
     * 字典 {@link cn.iocoder.yudao.module.mes.enums.DictTypeConstants#MES_IPQC_TYPE}
     */
    private Integer type;
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
     * 1. {@link MesQcSourceDocTypeEnum#PRO_FEEDBACK} 时，关联 {@link MesProFeedbackDO#getId()}
     */
    private Long sourceDocId;
    /**
     * 来源单据行 ID
     *
     * 关联：根据 {@link #sourceDocType} 不同：
     * 1. {@link MesQcSourceDocTypeEnum#PRO_FEEDBACK} 时，关联 {@link cn.iocoder.yudao.module.mes.dal.dataobject.wm.productproduce.MesWmProductProduceLineDO#getId()}
     */
    private Long sourceLineId;
    /**
     * 来源单据编号（冗余）
     */
    private String sourceDocCode;

    // ========== 生产关联 ==========

    /**
     * 生产工单 ID
     *
     * 关联 {@link MesProWorkOrderDO#getId()}
     */
    private Long workOrderId;
    /**
     * 生产任务 ID
     *
     * 关联 {@link cn.iocoder.yudao.module.mes.dal.dataobject.pro.task.MesProTaskDO#getId()}
     */
    private Long taskId;
    /**
     * 工位 ID
     *
     * 关联 {@link MesMdWorkstationDO#getId()}
     */
    private Long workstationId;
    /**
     * 工序 ID
     *
     * 关联 {@link cn.iocoder.yudao.module.mes.dal.dataobject.pro.process.MesProProcessDO#getId()}
     */
    private Long processId;

    // ========== 物料 ==========

    /**
     * 产品物料 ID
     *
     * 关联 {@link MesMdItemDO#getId()}
     */
    private Long itemId;

    // ========== 数量 ==========

    /**
     * 检测数量
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
    /**
     * 工废数量
     */
    private BigDecimal laborScrapQuantity;
    /**
     * 料废数量
     */
    private BigDecimal materialScrapQuantity;
    /**
     * 其他废品数量
     */
    private BigDecimal otherScrapQuantity;

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
     * 枚举 {@link cn.iocoder.yudao.module.mes.enums.qc.MesQcCheckResultEnum}
     */
    private Integer checkResult;
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
