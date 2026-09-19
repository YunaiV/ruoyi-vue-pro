package cn.iocoder.yudao.module.oa.controller.admin.announcement.vo;

import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.oa.enums.announcement.OaAnnouncementTypeEnum;
import cn.iocoder.yudao.module.oa.enums.schedule.OaPriorityEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Schema(description = "管理后台 - OA 公告新增/修改 Request VO")
@Data
public class OaAnnouncementSaveReqVO {

    @Schema(description = "公告编号", example = "1024")
    private Long id;

    @Schema(description = "公告类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "公告类型不能为空")
    @InEnum(value = OaAnnouncementTypeEnum.class, message = "公告类型必须是 {value}")
    private Integer type;

    @Schema(description = "优先级", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "优先级不能为空")
    @InEnum(value = OaPriorityEnum.class, message = "优先级必须是 {value}")
    private Integer priority;

    @Schema(description = "公告标题", requiredMode = Schema.RequiredMode.REQUIRED, example = "国庆节放假通知")
    @NotBlank(message = "公告标题不能为空")
    @Size(max = 255, message = "公告标题长度不能超过 255 个字符")
    private String title;

    @Schema(description = "公告内容", example = "请各部门提前安排好假期值班工作。")
    private String content;

    @Schema(description = "相关链接", example = "https://www.iocoder.cn")
    @Size(max = 512, message = "相关链接长度不能超过 512 个字符")
    private String url;

    @Schema(description = "是否置顶", requiredMode = Schema.RequiredMode.REQUIRED, example = "false")
    @NotNull(message = "是否置顶不能为空")
    private Boolean top;

}
