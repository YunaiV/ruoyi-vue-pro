package cn.iocoder.yudao.module.jl.controller.admin.project.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import javax.validation.constraints.*;

/**
 * 项目合同 Base VO，提供给添加、修改、详细的子 VO 使用
 * 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
 */
@Data
public class ProjectConstractBaseVO {

    @Schema(description = "项目 id", requiredMode = Schema.RequiredMode.REQUIRED, example = "12507")
    @NotNull(message = "项目 id不能为空")
    private Long projectId;

    @Schema(description = "合同名字", example = "赵六")
    private String name;

    @Schema(description = "合同文件 URL", requiredMode = Schema.RequiredMode.REQUIRED, example = "https://www.iocoder.cn")
    @NotNull(message = "合同文件 URL不能为空")
    private String fileUrl;

    @Schema(description = "合同状态：起效、失效、其它", example = "2")
    private String status;

    @Schema(description = "合同类型", example = "1")
    private String type;

    @Schema(description = "合同金额", example = "30614")
    private Long price;

    @Schema(description = "签订销售人员", example = "32406")
    private Long salesId;

    @Schema(description = "合同编号")
    private String sn;

    @Schema(description = "合同文件名", example = "芋艿")
    private String fileName;

}
