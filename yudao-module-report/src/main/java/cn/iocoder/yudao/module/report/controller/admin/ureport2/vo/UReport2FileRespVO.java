package cn.iocoder.yudao.module.report.controller.admin.ureport2.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 管理后台 - UReport2 报表模板 Response VO
 *
 * 不返回模板内容，设计、预览由 UReport2 自带的页面加载
 *
 * @author 芋道源码
 */
@Schema(description = "管理后台 - UReport2 报表模板 Response VO")
@Data
public class UReport2FileRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "模板名称，不包含存储前缀", requiredMode = Schema.RequiredMode.REQUIRED, example = "demo.ureport.xml")
    private String name;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

    @Schema(description = "更新时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime updateTime;

}
