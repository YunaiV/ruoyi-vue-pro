package cn.iocoder.yudao.module.oa.controller.admin.file.vo.node;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.oa.enums.file.OaFileCategoryEnum;
import cn.iocoder.yudao.module.oa.enums.file.OaFileScopeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.*;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 云盘文件分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class OaFileNodePageReqVO extends PageParam {

    @Schema(description = "列表范围：my、shared、favorite、recycle", requiredMode = Schema.RequiredMode.REQUIRED, example = "my")
    @NotBlank(message = "列表范围不能为空")
    @InEnum(value = OaFileScopeEnum.class, message = "列表范围必须是 {value}")
    private String scope;

    @Schema(description = "父目录编号，不传时查询范围内全部文件", example = "1024")
    private Long parentId;

    @Schema(description = "名称", example = "办公用品盘点结果.pdf")
    @Size(max = 255, message = "名称长度不能超过 255 个字符")
    private String name;

    @Schema(description = "文件分类", example = "1")
    @InEnum(value = OaFileCategoryEnum.class, message = "文件分类必须是 {value}")
    private Integer category;

    @Schema(description = "创建时间范围", example = "[\"2026-09-14 00:00:00\", \"2026-09-14 23:59:59\"]")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
