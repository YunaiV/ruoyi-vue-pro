package cn.iocoder.yudao.module.infra.controller.admin.test.vo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;
import org.springframework.format.annotation.DateTimeFormat;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(title = "管理后台 - 字典类型 Excel 导出 Request VO", description = "参数和 TestDemoPageReqVO 是一致的")
@Data
public class TestDemoExportReqVO {

    @Schema(title = "名字")
    private String name;

    @Schema(title = "状态")
    private Integer status;

    @Schema(title = "类型")
    private Integer type;

    @Schema(title = "分类")
    private Integer category;

    @Schema(title = "备注")
    private String remark;

    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @Schema(title = "创建时间")
    private LocalDateTime[] createTime;

}
