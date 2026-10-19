package cn.iocoder.yudao.module.oa.controller.admin.officialdoc.vo.receive;

import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.framework.dict.validation.InDict;
import cn.iocoder.yudao.module.oa.enums.DictTypeConstants;
import cn.iocoder.yudao.module.oa.enums.officialdoc.OaOfficialDocReceiveTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - 公文收文新增/修改 Request VO")
@Data
public class OaOfficialDocReceiveSaveReqVO {

    @Schema(description = "编号", example = "1024")
    private Long id;

    @Schema(description = "公文标题", requiredMode = Schema.RequiredMode.REQUIRED, example = "关于开展办公用品盘点的通知")
    @NotBlank(message = "公文标题不能为空")
    @Size(max = 255, message = "公文标题长度不能超过 {max} 个字符")
    private String title;

    @Schema(description = "来文字号", example = "芋办〔2026〕1号")
    @Size(max = 64, message = "来文字号长度不能超过 {max} 个字符")
    private String documentNo;

    @Schema(description = "密级", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @NotNull(message = "密级不能为空")
    @InDict(type = DictTypeConstants.OFFICIAL_DOC_SECRET_LEVEL, message = "密级必须是 {value}")
    private Integer secrecyLevel;

    @Schema(description = "紧急程度", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @NotNull(message = "紧急程度不能为空")
    @InDict(type = DictTypeConstants.OFFICIAL_DOC_URGENCY_LEVEL, message = "紧急程度必须是 {value}")
    private Integer urgencyLevel;

    @Schema(description = "收文类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @NotNull(message = "收文类型不能为空")
    @InEnum(value = OaOfficialDocReceiveTypeEnum.class, message = "收文类型必须是 {value}")
    private Integer receiveType;

    @Schema(description = "收文时间", requiredMode = Schema.RequiredMode.REQUIRED, type = "integer", format = "int64", example = "1789347600000")
    @NotNull(message = "收文时间不能为空")
    private LocalDateTime receiveTime;

    @Schema(description = "收文部门编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "收文部门不能为空")
    private Long receiveDeptId;

    @Schema(description = "主办人用户编号", example = "1024")
    private Long handlerUserId;

    @Schema(description = "领导批示", example = "请行政部牵头办理")
    @Size(max = 2000, message = "领导批示长度不能超过 {max} 个字符")
    private String instruction;

    @Schema(description = "办理结果", example = "已完成盘点并归档")
    @Size(max = 2000, message = "办理结果长度不能超过 {max} 个字符")
    private String result;

    @Schema(description = "办理期限", type = "integer", format = "int64", example = "1789347600000")
    private LocalDateTime deadlineTime;

    @Schema(description = "内容摘要", example = "本周已完成办公用品盘点")
    private String summary;

    @Schema(description = "备注", example = "请提前联系行政部确认")
    @Size(max = 500, message = "备注长度不能超过 {max} 个字符")
    private String remark;

    @Schema(description = "附件地址列表", example = "[\"https://example.com/file.pdf\"]")
    private List<@NotBlank(message = "附件地址不能为空") String> fileUrls;

    @Schema(description = "正式公文地址", example = "https://example.com/file.pdf")
    @Size(max = 2048, message = "正式公文地址长度不能超过 {max} 个字符")
    private String formalFileUrl;

}
