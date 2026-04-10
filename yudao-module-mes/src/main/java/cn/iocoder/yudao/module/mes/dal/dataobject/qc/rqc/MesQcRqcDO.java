package cn.iocoder.yudao.module.mes.dal.dataobject.qc.rqc;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.item.MesMdItemDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.qc.template.MesQcTemplateDO;
import cn.iocoder.yudao.module.mes.enums.qc.MesQcStatusEnum;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * MES 退货检验单（RQC, Return Quality Control） DO
 *
 * @author 芋道源码
 */
@TableName("mes_qc_rqc")
@KeySequence("mes_qc_rqc_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MesQcRqcDO extends BaseDO {

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
     */
    private Long sourceDocId;
    /**
     * 来源单据类型
     *
     * 关联 {@link cn.iocoder.yudao.module.mes.enums.MesBizTypeConstants}
     */
    private Integer sourceDocType;
    /**
     * 来源单据行 ID
     */
    private Long sourceLineId;
    /**
     * 来源单据编码（冗余）
     */
    private String sourceDocCode;

    // ========== 检验类型 ==========

    /**
     * 检验类型
     *
     * 字典 mes_rqc_type
     */
    private Integer type;

    // ========== 物料 ==========

    /**
     * 产品物料 ID
     *
     * 关联 {@link MesMdItemDO#getId()}
     */
    private Long itemId;
    /**
     * 批次号
     */
    private String batchCode;

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
     * 不合格数量
     */
    private BigDecimal unqualifiedQuantity;

    // ========== 检验 ==========

    /**
     * 检测结果
     *
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
     * 枚举 {@link MesQcStatusEnum}
     */
    private Integer status;
    /**
     * 备注
     */
    private String remark;

    // ========== 缺陷统计 ==========

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

}
