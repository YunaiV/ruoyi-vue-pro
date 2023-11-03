package cn.iocoder.yudao.module.system.controller.admin.dept.vo.post;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Schema(description = "管理后台 - 岗位创建 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class PostCreateReqVO extends PostBaseVO {
}
