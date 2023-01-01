package cn.iocoder.yudao.module.bpm.controller.admin.definition.vo.group;

import lombok.*;
import java.util.*;
import io.swagger.annotations.*;
import javax.validation.constraints.*;

/**
* 用户组 Base VO，提供给添加、修改、详细的子 VO 使用
* 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
*/
@Data
public class BpmUserGroupBaseVO {

    @ApiModelProperty(value = "组名", required = true, example = "芋道")
    @NotNull(message = "组名不能为空")
    private String name;

    @ApiModelProperty(value = "描述", required = true, example = "芋道源码")
    @NotNull(message = "描述不能为空")
    private String description;

    @ApiModelProperty(value = "成员编号数组", required = true, example = "1,2,3")
    @NotNull(message = "成员编号数组不能为空")
    private Set<Long> memberUserIds;

    @ApiModelProperty(value = "状态", required = true, example = "1")
    @NotNull(message = "状态不能为空")
    private Integer status;

}
