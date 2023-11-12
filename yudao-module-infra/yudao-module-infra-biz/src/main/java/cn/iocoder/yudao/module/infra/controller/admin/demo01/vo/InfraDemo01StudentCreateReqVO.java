package cn.iocoder.yudao.module.infra.controller.admin.demo01.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - 学生创建 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class InfraDemo01StudentCreateReqVO extends InfraDemo01StudentBaseVO {

}