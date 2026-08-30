package cn.iocoder.yudao.module.fms.dal.dataobject.config;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.fms.enums.common.FmsDebitCreditDirectionEnum;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.Jackson3TypeHandler;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

/**
 * FMS 会计科目 DO
 *
 * @author 芋道源码
 */
@TableName(value = "fms_subject", autoResultMap = true)
@KeySequence("fms_subject_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FmsSubjectDO extends BaseDO {

    /**
     * 根科目编号
     */
    public static final Long PARENT_ID_ROOT = 0L;

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 科目编码
     */
    private String code;
    /**
     * 科目名称
     */
    private String name;
    /**
     * 上级科目编号
     *
     * 关联 {@link FmsSubjectDO#getId()}
     */
    private Long parentId;
    /**
     * 科目类型
     *
     * 枚举 {@link cn.iocoder.yudao.module.fms.enums.config.FmsSubjectTypeEnum}
     */
    private Integer type;
    /**
     * 科目类别
     */
    private Integer category;
    /**
     * 余额方向
     *
     * 枚举 {@link FmsDebitCreditDirectionEnum}
     */
    private Integer balanceDirection;
    /**
     * 数量核算 计量单位
     */
    private String quantityUnit;
    /**
     * 是否现金科目
     */
    private Boolean cash;
    /**
     * 状态
     *
     * 枚举 {@link cn.iocoder.yudao.framework.common.enums.CommonStatusEnum}
     */
    private Integer status;
    /**
     * 科目层级
     */
    private Integer level;
    /**
     * 是否启用数量核算
     */
    private Boolean quantityAccounting;
    /**
     * 账套编号
     *
     * 关联 {@link cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsAccountSetDO#getId()}
     */
    private Long accountSetId;
    /**
     * 辅助核算类别编号数组
     *
     * 关联 {@link cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsAuxiliaryTypeDO#getId()}
     */
    @TableField(typeHandler = Jackson3TypeHandler.class)
    private List<Long> auxiliaryTypeIds;
    /**
     * 核算币种编号数组
     *
     * 关联 {@link cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsCurrencyDO#getId()}
     */
    @TableField(typeHandler = Jackson3TypeHandler.class)
    private List<Long> currencyIds;

}
