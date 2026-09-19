package cn.iocoder.yudao.module.oa.controller.admin.officialdoc.vo.send;

import cn.iocoder.yudao.framework.dict.validation.InDict;
import cn.iocoder.yudao.module.oa.enums.DictTypeConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - 公文发文新增/修改 Request VO")
@Data
public class OaOfficialDocSendSaveReqVO {

    @Schema(description = "编号", example = "1024")
    private Long id;

    @Schema(description = "套红模板编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "套红模板不能为空")
    private Long templateId;

    @Schema(description = "公文标题", requiredMode = Schema.RequiredMode.REQUIRED, example = "关于开展办公用品盘点的通知")
    @NotBlank(message = "标题不能为空")
    @Size(max = 255, message = "公文标题长度不能超过 {max} 个字符")
    private String title;

    @Schema(description = "字号", example = "芋办")
    @Size(max = 64, message = "字号长度不能超过 {max} 个字符")
    private String noPrefix;

    @Schema(description = "年份", example = "2026")
    @Min(value = 1, message = "年份不能小于 {value}")
    @Max(value = 9999, message = "年份不能大于 {value}")
    private Integer year;

    @Schema(description = "第几号文", example = "1")
    @Min(value = 1, message = "第几号文不能小于 {value}")
    private Integer sequence;

    @Schema(description = "密级", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @NotNull(message = "密级不能为空")
    @InDict(type = DictTypeConstants.OFFICIAL_DOC_SECRET_LEVEL, message = "密级必须是 {value}")
    private Integer secrecyLevel;

    @Schema(description = "紧急程度", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @NotNull(message = "紧急程度不能为空")
    @InDict(type = DictTypeConstants.OFFICIAL_DOC_URGENCY_LEVEL, message = "紧急程度必须是 {value}")
    private Integer urgencyLevel;

    @Schema(description = "公开类别", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @NotNull(message = "公开类别不能为空")
    @InDict(type = DictTypeConstants.OFFICIAL_DOC_PUBLIC_CATEGORY, message = "公开类别必须是 {value}")
    private Integer disclosureType;

    @Schema(description = "发文日期", requiredMode = Schema.RequiredMode.REQUIRED, type = "integer", format = "int64", example = "1789347600000")
    @NotNull(message = "发文日期不能为空")
    private LocalDateTime issueTime;

    @Schema(description = "发文部门编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "发文部门不能为空")
    private Long sendDeptId;

    @Schema(description = "主送部门编号列表", requiredMode = Schema.RequiredMode.REQUIRED, example = "[1, 2]")
    @NotEmpty(message = "主送部门不能为空")
    private List<@NotNull(message = "主送部门编号不能为空") Long> mainDeptIds;

    @Schema(description = "抄送部门编号列表", example = "[1, 2]")
    private List<@NotNull(message = "抄送部门编号不能为空") Long> copyDeptIds;

    @Schema(description = "公文正文", example = "请各部门于本周五前完成办公用品盘点并提交结果。")
    private String content;

    @Schema(description = "附件地址列表", example = "[\"https://example.com/file.pdf\"]")
    private List<@NotBlank(message = "附件地址不能为空") String> fileUrls;

    @Schema(description = "正式公文地址", example = "https://example.com/file.pdf")
    @Size(max = 2048, message = "正式公文地址长度不能超过 {max} 个字符")
    private String formalFileUrl;

    @Schema(description = "附注", example = "请按要求办理并反馈结果")
    @Size(max = 500, message = "附注长度不能超过 {max} 个字符")
    private String remark;

}
