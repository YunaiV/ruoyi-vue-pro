package cn.iocoder.yudao.module.fms.dal.dataobject.config;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.List;

/**
 * FMS 科目期初余额 DO
 *
 * @author 芋道源码
 */
@TableName(value = "fms_initial_balance", autoResultMap = true)
@KeySequence("fms_initial_balance_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FmsInitialBalanceDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 科目编号
     *
     * 关联 {@link cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsSubjectDO#getId()}
     */
    private Long subjectId;
    /**
     * 是否启用辅助核算
     */
    private Boolean auxiliaryAccounting;
    /**
     * 期初金额
     */
    private BigDecimal openingAmount;
    /**
     * 期初数量
     */
    private BigDecimal openingQuantity;
    /**
     * 本年累计借方金额
     */
    private BigDecimal yearDebitAmount;
    /**
     * 本年累计借方数量
     */
    private BigDecimal yearDebitQuantity;
    /**
     * 本年累计贷方金额
     */
    private BigDecimal yearCreditAmount;
    /**
     * 本年累计贷方数量
     */
    private BigDecimal yearCreditQuantity;
    /**
     * 年初金额
     */
    private BigDecimal yearOpeningAmount;
    /**
     * 年初数量
     */
    private BigDecimal yearOpeningQuantity;
    /**
     * 实际损益发生额
     */
    private BigDecimal profitLossAmount;
    /**
     * 实际损益发生数量
     */
    private BigDecimal profitLossQuantity;
    /**
     * 账套编号
     *
     * 关联 {@link cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsAccountSetDO#getId()}
     */
    private Long accountSetId;
    /**
     * 辅助核算余额数组
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<AssistBalance> assistBalances;

    /**
     * 辅助核算余额
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AssistBalance {

        /**
         * 辅助核算组合编号
         *
         * 关联 {@link cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsAuxiliaryCombinationDO#getId()}
         */
        private Long assistCombinationId;
        /**
         * 辅助核算项目数组
         */
        private List<AuxiliaryItem> auxiliaries;
        /**
         * 期初金额
         */
        private BigDecimal openingAmount;
        /**
         * 期初数量
         */
        private BigDecimal openingQuantity;
        /**
         * 本年累计借方金额
         */
        private BigDecimal yearDebitAmount;
        /**
         * 本年累计借方数量
         */
        private BigDecimal yearDebitQuantity;
        /**
         * 本年累计贷方金额
         */
        private BigDecimal yearCreditAmount;
        /**
         * 本年累计贷方数量
         */
        private BigDecimal yearCreditQuantity;
        /**
         * 年初金额
         */
        private BigDecimal yearOpeningAmount;
        /**
         * 年初数量
         */
        private BigDecimal yearOpeningQuantity;
        /**
         * 实际损益发生额
         */
        private BigDecimal profitLossAmount;
        /**
         * 实际损益发生数量
         */
        private BigDecimal profitLossQuantity;
    }

    /**
     * 辅助核算项目
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AuxiliaryItem {

        /**
         * 辅助核算类别
         *
         * 枚举 {@link cn.iocoder.yudao.module.fms.enums.config.FmsAuxiliaryTypeEnum}
         */
        private Integer type;
        /**
         * 辅助核算类别编号
         *
         * 关联 {@link cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsAuxiliaryTypeDO#getId()}
         */
        private Long typeId;
        /**
         * 辅助核算项目编号
         *
         * 关联 {@link cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsAuxiliaryItemDO#getId()}
         */
        private Long itemId;
        /**
         * 辅助核算项目名称快照
         *
         * 关联 {@link cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsAuxiliaryItemDO#getName()}
         */
        private String name;
    }

}
