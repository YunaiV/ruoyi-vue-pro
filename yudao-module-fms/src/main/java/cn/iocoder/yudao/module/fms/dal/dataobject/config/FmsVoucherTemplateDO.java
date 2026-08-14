package cn.iocoder.yudao.module.fms.dal.dataobject.config;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.fms.dal.dataobject.voucher.FmsVoucherEntryDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.List;

/**
 * FMS 凭证模板 DO
 *
 * @author 芋道源码
 */
@TableName(value = "fms_voucher_template", autoResultMap = true)
@KeySequence("fms_voucher_template_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class FmsVoucherTemplateDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 凭证模板名称
     */
    private String name;
    /**
     * 凭证模板分类编号
     *
     * 关联 {@link FmsVoucherTemplateCategoryDO#getId()}
     */
    private Long categoryId;
    /**
     * 凭证模板分录数组
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<Entry> entries;
    /**
     * 账套编号
     *
     * 关联 {@link cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsAccountSetDO#getId()}
     */
    private Long accountSetId;

    /**
     * 凭证模板分录
     */
    @Data
    public static class Entry {

        /**
         * 摘要内容
         */
        private String digest;
        /**
         * 科目编号
         *
         * 关联 {@link cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsSubjectDO#getId()}
         */
        private Long subjectId;
        /**
         * 数量
         */
        private BigDecimal quantity;
        /**
         * 单价
         */
        private BigDecimal unitPrice;
        /**
         * 借方金额
         */
        private BigDecimal debitAmount;
        /**
         * 贷方金额
         */
        private BigDecimal creditAmount;
        /**
         * 辅助核算项目数组
         */
        private List<FmsVoucherEntryDO.AuxiliaryItem> auxiliaries;

    }

}
