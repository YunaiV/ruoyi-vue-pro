package cn.iocoder.yudao.module.infra.controller.admin.test.vo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Schema(description = "管理后台 - 字典类型创建 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class TestDemoCreateReqVO extends TestDemoBaseVO {

}
