package cn.iocoder.yudao.module.oa.controller.admin.officialdoc.vo.template;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.framework.dict.validation.InDict;
import cn.iocoder.yudao.module.oa.enums.DictTypeConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.*;

@Schema(description = "管理后台 - 套红模板新增/修改 Request VO")
@Data
public class OaOfficialDocTemplateSaveReqVO {

    @Schema(description = "编号", example = "1024")
    private Long id;

    @Schema(description = "模板名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "公司行政发文模板")
    @NotBlank(message = "模板名称不能为空")
    @Size(max = 128, message = "模板名称长度不能超过 {max} 个字符")
    private String name;

    @Schema(description = "红头名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "芋道科技有限公司文件")
    @NotBlank(message = "红头名称不能为空")
    @Size(max = 255, message = "红头名称长度不能超过 {max} 个字符")
    private String authorityName;

    @Schema(description = "红头字号", requiredMode = Schema.RequiredMode.REQUIRED, example = "32")
    @NotNull(message = "字号不能为空")
    @Min(value = 18, message = "红头字号不能小于 {value}")
    @Max(value = 72, message = "红头字号不能大于 {value}")
    private Integer fontSize;

    @Schema(description = "发文字号前缀", example = "芋办")
    @Size(max = 64, message = "发文字号前缀长度不能超过 {max} 个字符")
    private String noPrefix;

    @Schema(description = "印章图片地址", example = "https://example.com/image.png")
    @Size(max = 2048, message = "印章图片地址长度不能超过 {max} 个字符")
    private String sealPicUrl;

    @Schema(description = "分隔线类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @NotNull(message = "分隔线类型不能为空")
    @InDict(type = DictTypeConstants.OFFICIAL_DOC_SEPARATOR_TYPE, message = "分隔线类型必须是 {value}")
    private Integer separatorType;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @NotNull(message = "状态不能为空")
    @InEnum(value = CommonStatusEnum.class, message = "状态必须是 {value}")
    private Integer status;

    @Schema(description = "显示顺序", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "显示顺序不能为空")
    private Integer sort;

    @Schema(description = "备注", example = "请提前联系行政部确认")
    @Size(max = 500, message = "备注长度不能超过 {max} 个字符")
    private String remark;

}
