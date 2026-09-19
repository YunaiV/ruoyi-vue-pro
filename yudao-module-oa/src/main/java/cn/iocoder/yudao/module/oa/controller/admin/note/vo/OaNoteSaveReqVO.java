package cn.iocoder.yudao.module.oa.controller.admin.note.vo;

import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.oa.enums.note.OaNoteTypeEnum;
import cn.iocoder.yudao.module.oa.enums.schedule.OaPriorityEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

@Schema(description = "管理后台 - OA 笔记新增/修改 Request VO")
@Data
public class OaNoteSaveReqVO {

    @Schema(description = "笔记编号", example = "1024")
    private Long id;

    @Schema(description = "目录编号", example = "1024")
    private Long categoryId;

    @Schema(description = "笔记类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "笔记类型不能为空")
    @InEnum(value = OaNoteTypeEnum.class, message = "笔记类型必须是 {value}")
    private Integer type;

    @Schema(description = "优先级", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "优先级不能为空")
    @InEnum(value = OaPriorityEnum.class, message = "优先级必须是 {value}")
    @Max(value = 2, message = "笔记优先级只能为一般或重要")
    private Integer priority;

    @Schema(description = "标题", requiredMode = Schema.RequiredMode.REQUIRED, example = "项目会议纪要")
    @NotBlank(message = "标题不能为空")
    @Size(max = 255, message = "标题长度不能超过 255 个字符")
    private String title;

    @Schema(description = "内容", requiredMode = Schema.RequiredMode.REQUIRED, example = "记录本周会议结论与待办事项。")
    @NotBlank(message = "笔记内容不能为空")
    private String content;

    @Schema(description = "附件地址列表", example = "[\"https://example.com/file.pdf\"]")
    private List<@NotBlank(message = "附件地址不能为空") @Size(max = 512, message = "附件地址长度不能超过 512 个字符") String> fileUrls;

}
