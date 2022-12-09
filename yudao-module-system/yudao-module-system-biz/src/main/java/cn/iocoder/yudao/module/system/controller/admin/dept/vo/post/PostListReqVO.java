package cn.iocoder.yudao.module.system.controller.admin.dept.vo.post;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Schema(title = "管理后台 - 岗位列表 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class PostListReqVO extends PostBaseVO {

    @Schema(title = "岗位名称", example = "芋道", description = "模糊匹配")
    private String name;

    @Schema(title = "展示状态", example = "1", description = "参见 CommonStatusEnum 枚举类")
    private Integer status;

}
