package cn.iocoder.yudao.module.pms.controller.admin.pm.workitem.vo.workitem;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import cn.iocoder.yudao.framework.excel.core.annotations.DictFormat;
import cn.iocoder.yudao.framework.excel.core.annotations.ExcelColumnSelect;
import cn.iocoder.yudao.framework.excel.core.convert.DictConvert;
import cn.iocoder.yudao.framework.excel.core.convert.StringListConvert;
import cn.iocoder.yudao.module.pms.enums.DictTypeConstants;
import cn.iocoder.yudao.module.pms.enums.pm.workitem.PmsWorkItemTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * PMS 工作项 Excel 导入 VO
 *
 * @author 芋道源码
 */
@Schema(description = "管理后台 - PMS 工作项 Excel 导入 VO")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ExcelIgnoreUnannotated
public class PmsWorkItemImportExcelVO {

    @ExcelProperty("事项标题")
    @Schema(description = "工作项标题", example = "完成登录页")
    private String name;

    @ExcelProperty("描述")
    @Schema(description = "工作项描述", example = "实现账号密码登录")
    private String description;

    @ExcelProperty("处理人用户编号")
    @Schema(description = "处理人用户编号，为空时不指定处理人", example = "1024")
    private Long assigneeUserId;

    @ExcelProperty("状态")
    @Schema(description = "项目状态名称；为空时使用项目默认状态", example = "待处理")
    private String statusName;

    @ExcelProperty(value = "优先级", converter = DictConvert.class)
    @DictFormat(DictTypeConstants.PMS_WORK_ITEM_PRIORITY)
    @ExcelColumnSelect(dictType = DictTypeConstants.PMS_WORK_ITEM_PRIORITY)
    @Schema(description = "优先级，Excel 中填写字典标签", example = "中")
    private Integer priority;

    @ExcelProperty("开始时间")
    @Schema(description = "开始时间", example = "2026-08-30 09:00:00")
    private LocalDateTime startTime;

    @ExcelProperty("截止时间")
    @Schema(description = "截止时间", example = "2026-09-01 18:00:00")
    private LocalDateTime endTime;

    @ExcelProperty(value = "标签", converter = StringListConvert.class)
    @Schema(description = "标签名称列表，多个标签可用逗号、顿号或斜杠分隔", example = "前端/登录")
    private List<String> labels;

    @ExcelProperty("进度")
    @Schema(description = "完成进度，范围 0-100", example = "50")
    private Integer progress;

    @ExcelProperty("预估工时")
    @Schema(description = "预估工时，单位：小时", example = "8")
    private Integer estimatedHours;

    @ExcelProperty("实际投入时长")
    @Schema(description = "实际投入时长，单位：小时", example = "4")
    @Min(value = 1, message = "实际投入时长必须大于 0")
    private Integer actualHours;

    @ExcelProperty("剩余工时")
    @Schema(description = "剩余工时，单位：小时", example = "4")
    @Min(value = 0, message = "剩余工时不能小于 0")
    private Integer remainingHours;

    @ExcelProperty(value = "缺陷类型", converter = DictConvert.class)
    @DictFormat(DictTypeConstants.PMS_WORK_ITEM_DEFECT_TYPE)
    @ExcelColumnSelect(dictType = DictTypeConstants.PMS_WORK_ITEM_DEFECT_TYPE)
    @Schema(description = "缺陷类型，Excel 中填写字典标签，仅缺陷工作项使用", example = "功能缺陷")
    private Integer defectType;

    /**
     * 导入时从 {@link #statusName} 解析出的项目状态编号，不作为 Excel 列
     */
    @Schema(hidden = true)
    private Long statusId;

    /**
     * 导入时从 {@link #labels} 解析出的标签编号列表，不作为 Excel 列
     */
    @Schema(hidden = true)
    private List<Long> labelIds;

    /**
     * 导入时使用的工作项类型，不作为 Excel 列
     */
    @Schema(hidden = true)
    private Integer workItemType;

    @AssertTrue(message = "填写剩余工时时必须同时填写实际投入时长")
    public boolean isWorkLogHoursValid() {
        return actualHours != null || remainingHours == null;
    }

    @AssertTrue(message = "缺陷类型仅可用于缺陷工作项")
    public boolean isDefectTypeValid() {
        return defectType == null || PmsWorkItemTypeEnum.DEFECT.getType().equals(workItemType);
    }

}
