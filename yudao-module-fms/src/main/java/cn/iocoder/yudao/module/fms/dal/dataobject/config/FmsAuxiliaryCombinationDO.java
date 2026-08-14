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

import java.util.List;

/**
 * FMS 辅助核算组合 DO
 *
 * @author 芋道源码
 */
@TableName(value = "fms_assist_combination", autoResultMap = true)
@KeySequence("fms_assist_combination_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FmsAuxiliaryCombinationDO extends BaseDO {

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
     * 账套编号
     *
     * 关联 {@link cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsAccountSetDO#getId()}
     */
    private Long accountSetId;
    /**
     * 辅助核算项目数组
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<AuxiliaryItem> items;

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
         * 关联 {@link FmsAuxiliaryTypeDO#getId()}
         */
        private Long typeId;
        /**
         * 辅助核算项目编号
         *
         * 关联 {@link FmsAuxiliaryItemDO#getId()}
         */
        private Long itemId;
        /**
         * 辅助核算项目名称快照
         *
         * 关联 {@link FmsAuxiliaryItemDO#getName()}
         */
        private String name;
    }

}
