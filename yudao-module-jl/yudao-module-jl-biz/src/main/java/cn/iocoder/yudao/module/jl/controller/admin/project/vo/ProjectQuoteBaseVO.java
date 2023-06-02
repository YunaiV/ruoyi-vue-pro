package cn.iocoder.yudao.module.jl.controller.admin.project.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import javax.validation.constraints.*;

/**
 * 项目报价 Base VO，提供给添加、修改、详细的子 VO 使用
 * 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
 */
@Data
public class ProjectQuoteBaseVO {

    @Schema(description = "销售线索 id", example = "23498")
    private Long salesleadId;

    @Schema(description = "项目 id", example = "21263")
    private Long projectId;

    @Schema(description = "报价单的名字", requiredMode = Schema.RequiredMode.REQUIRED, example = "张三")
    @NotNull(message = "报价单的名字不能为空")
    private String name;

    @Schema(description = "方案 URL", requiredMode = Schema.RequiredMode.REQUIRED, example = "https://www.iocoder.cn")
    @NotNull(message = "方案 URL不能为空")
    private String reportUrl;

    @Schema(description = "折扣(100: 无折扣, 98: 98折)", requiredMode = Schema.RequiredMode.REQUIRED, example = "6647")
    @NotNull(message = "折扣(100: 无折扣, 98: 98折)不能为空")
    private Integer discount;

    @Schema(description = "状态, 已提交、已作废、已采用", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "状态, 已提交、已作废、已采用不能为空")
    private String status;

}
