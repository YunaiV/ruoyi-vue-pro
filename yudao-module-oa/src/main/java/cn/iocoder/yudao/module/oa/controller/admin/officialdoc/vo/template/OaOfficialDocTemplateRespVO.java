package cn.iocoder.yudao.module.oa.controller.admin.officialdoc.vo.template;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - 套红模板 Response VO")
@Data
public class OaOfficialDocTemplateRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "模板名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "公司行政发文模板")
    private String name;

    @Schema(description = "红头名称", example = "芋道科技有限公司文件")
    private String authorityName;

    @Schema(description = "红头字号", example = "32")
    private Integer fontSize;

    @Schema(description = "发文字号前缀", example = "芋办")
    private String noPrefix;

    @Schema(description = "印章图片地址", example = "https://example.com/image.png")
    private String sealPicUrl;

    @Schema(description = "分隔线类型", example = "0")
    private Integer separatorType;

    @Schema(description = "状态", example = "0")
    private Integer status;

    @Schema(description = "显示顺序", example = "1")
    private Integer sort;

    @Schema(description = "备注", example = "请提前联系行政部确认")
    private String remark;

    @Schema(description = "创建人编号", example = "1024")
    private String creator;

    @Schema(description = "创建时间", type = "integer", format = "int64", example = "1789347600000")
    private LocalDateTime createTime;

}
