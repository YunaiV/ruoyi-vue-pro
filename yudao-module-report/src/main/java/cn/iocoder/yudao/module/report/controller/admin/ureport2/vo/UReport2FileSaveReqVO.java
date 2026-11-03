package cn.iocoder.yudao.module.report.controller.admin.ureport2.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * 管理后台 - UReport2 报表模板保存 Request VO
 *
 * @author 芋道源码
 */
@Schema(description = "管理后台 - UReport2 报表模板保存 Request VO")
@Data
public class UReport2FileSaveReqVO {

    @Schema(description = "模板名称，不包含存储前缀", requiredMode = Schema.RequiredMode.REQUIRED, example = "demo.ureport.xml")
    @NotBlank(message = "模板名称不能为空")
    @Size(max = 128, message = "模板名称长度不能超过 128 个字符") // 与 report_ureport_file.name 字段长度一致
    private String name;

    @Schema(description = "模板内容，UReport2 的 XML 格式", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "模板内容不能为空")
    private String content;

}
