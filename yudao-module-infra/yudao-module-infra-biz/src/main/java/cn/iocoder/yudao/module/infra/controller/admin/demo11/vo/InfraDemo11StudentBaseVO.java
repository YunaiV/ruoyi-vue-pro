package cn.iocoder.yudao.module.infra.controller.admin.demo11.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import javax.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;
import cn.iocoder.yudao.module.infra.dal.dataobject.demo11.InfraDemo11StudentContactDO;
import cn.iocoder.yudao.module.infra.dal.dataobject.demo11.InfraDemo11StudentTeacherDO;

/**
 * 学生 Base VO，提供给添加、修改、详细的子 VO 使用
 * 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
 */
@Data
public class InfraDemo11StudentBaseVO {

    @Schema(description = "名字", requiredMode = Schema.RequiredMode.REQUIRED, example = "芋头")
    @NotEmpty(message = "名字不能为空")
    private String name;

    @Schema(description = "简介", requiredMode = Schema.RequiredMode.REQUIRED, example = "我是介绍")
    @NotEmpty(message = "简介不能为空")
    private String description;

    @Schema(description = "出生日期", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "出生日期不能为空")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime birthday;

    @Schema(description = "性别", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "性别不能为空")
    private Integer sex;

    @Schema(description = "是否有效", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    @NotNull(message = "是否有效不能为空")
    private Boolean enabled;

    @Schema(description = "头像", requiredMode = Schema.RequiredMode.REQUIRED, example = "https://www.iocoder.cn/1.png")
    @NotEmpty(message = "头像不能为空")
    private String avatar;

    @Schema(description = "附件", example = "https://www.iocoder.cn/1.mp4")
    private String video;

    @Schema(description = "备注", requiredMode = Schema.RequiredMode.REQUIRED, example = "我是备注")
    @NotEmpty(message = "备注不能为空")
    private String memo;

    private List<InfraDemo11StudentContactDO> demo11StudentContacts;

    private InfraDemo11StudentTeacherDO demo11StudentTeacher;

}