package cn.iocoder.yudao.module.crm.controller.admin.followup.vo;

import cn.iocoder.yudao.framework.excel.core.annotations.DictFormat;
import cn.iocoder.yudao.framework.excel.core.convert.DictConvert;
import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 跟进记录 Response VO")
@Data
@ExcelIgnoreUnannotated
public class CrmFollowUpRecordRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "28800")
    @ExcelProperty("编号")
    private Long id;

    @Schema(description = "数据类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @ExcelProperty("数据类型")
    private Integer bizType;

    @Schema(description = "数据编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "5564")
    @ExcelProperty("数据编号")
    private Long bizId;

    @Schema(description = "跟进类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @ExcelProperty(value = "跟进类型", converter = DictConvert.class)
    @DictFormat("crm_follow_up_type") // TODO 代码优化：建议设置到对应的 DictTypeConstants 枚举类中
    private Integer type;

    @Schema(description = "跟进内容", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("跟进内容")
    private String content;

    @Schema(description = "下次联系时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("下次联系时间")
    private LocalDateTime nextTime;

    @Schema(description = "关联的商机编号数组")
    @ExcelProperty("关联的商机编号数组")
    private String businessIds;

    @Schema(description = "关联的联系人编号数组")
    @ExcelProperty("关联的联系人编号数组")
    private String contactIds;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}