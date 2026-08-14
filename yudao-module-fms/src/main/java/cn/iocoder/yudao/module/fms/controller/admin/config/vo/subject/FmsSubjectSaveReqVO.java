package cn.iocoder.yudao.module.fms.controller.admin.config.vo.subject;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.fms.enums.common.FmsDebitCreditDirectionEnum;
import cn.iocoder.yudao.module.fms.enums.config.FmsSubjectCategoryEnum;
import cn.iocoder.yudao.module.fms.enums.config.FmsSubjectTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * FMS 科目保存 Request VO
 *
 * @author 芋道源码
 */
@Schema(description = "管理后台 - FMS 科目保存 Request VO")
@Data
public class FmsSubjectSaveReqVO {

    @Schema(description = "科目编号", example = "1024")
    private Long id;

    @Schema(description = "账套编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "账套编号不能为空")
    private Long accountSetId;

    @Schema(description = "科目编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "1001")
    @NotBlank(message = "科目编码不能为空")
    @Size(max = 64, message = "科目编码长度不能超过 64 个字符")
    private String code;

    @Schema(description = "科目名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "库存现金")
    @NotBlank(message = "科目名称不能为空")
    @Size(max = 255, message = "科目名称长度不能超过 255 个字符")
    private String name;

    @Schema(description = "上级科目编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "上级科目编号不能为空")
    private Long parentId;

    @Schema(description = "科目类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "科目类型不能为空")
    @InEnum(FmsSubjectTypeEnum.class)
    private Integer type;

    @Schema(description = "科目类别", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "科目类别不能为空")
    private Integer category;

    @Schema(description = "余额方向", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "余额方向不能为空")
    @InEnum(FmsDebitCreditDirectionEnum.class)
    private Integer balanceDirection;

    @Schema(description = "辅助核算类别编号数组")
    @NotNull(message = "辅助核算类别编号数组不能为空")
    private List<Long> auxiliaryTypeIds;

    @Schema(description = "外币核算币别编号数组")
    @NotNull(message = "外币核算币别编号数组不能为空")
    private List<Long> currencyIds;

    @Schema(description = "是否启用数量核算", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "是否启用数量核算不能为空")
    private Boolean quantityAccounting;

    @Schema(description = "数量单位", example = "件")
    @Size(max = 255, message = "数量单位长度不能超过 255 个字符")
    private String quantityUnit;

    @Schema(description = "是否现金及现金等价物", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "是否现金及现金等价物不能为空")
    private Boolean cash;

    @Schema(description = "是否迁移上级科目历史数据")
    private Boolean migrateParentData;

    @Schema(description = "辅助核算历史数据迁移项目数组")
    private List<AuxiliaryMapping> auxiliaryMappings;

    @AssertTrue(message = "科目类别与科目类型不匹配")
    public boolean isCategoryValid() {
        return type == null || category == null || FmsSubjectCategoryEnum.valueOf(type, category) != null;
    }

    @AssertTrue(message = "启用数量核算时数量单位不能为空")
    public boolean isQuantityUnitValid() {
        return !Boolean.TRUE.equals(quantityAccounting) || StrUtil.isNotBlank(quantityUnit);
    }

    /**
     * 辅助核算历史数据迁移项目
     */
    @Data
    public static class AuxiliaryMapping {

        @Schema(description = "辅助核算类别编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
        @NotNull(message = "辅助核算类别编号不能为空")
        private Long typeId;

        @Schema(description = "辅助核算项目编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
        @NotNull(message = "辅助核算项目编号不能为空")
        private Long itemId;

    }

}
