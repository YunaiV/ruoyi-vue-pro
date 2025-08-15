package cn.iocoder.yudao.module.system.controller.admin.student.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import javax.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import cn.iocoder.yudao.module.system.dal.dataobject.student.StudentCourseDO;
import cn.iocoder.yudao.module.system.dal.dataobject.student.StudentGradeDO;

@Schema(description = "管理后台 - 学生主子表测试新增/修改 Request VO")
@Data
public class StudentSaveReqVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "29522")
    private Long id;

    @Schema(description = "名字", requiredMode = Schema.RequiredMode.REQUIRED, example = "王五")
    @NotEmpty(message = "名字不能为空")
    private String name;

    @Schema(description = "出生日期", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "出生日期不能为空")
    private LocalDateTime birthday;

    @Schema(description = "简介", requiredMode = Schema.RequiredMode.REQUIRED, example = "随便")
    @NotEmpty(message = "简介不能为空")
    private String description;

}