package cn.iocoder.yudao.module.infra.controller.admin.db.vo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import javax.validation.constraints.*;

/**
* 数据源配置 Base VO，提供给添加、修改、详细的子 VO 使用
* 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
*/
@Data
public class DataSourceConfigBaseVO {

    @Schema(description = "数据源名称", required = true, example = "test")
    @NotNull(message = "数据源名称不能为空")
    private String name;

    @Schema(description = "数据源连接", required = true, example = "jdbc:mysql://127.0.0.1:3306/ruoyi-vue-pro")
    @NotNull(message = "数据源连接不能为空")
    private String url;

    @Schema(description = "用户名", required = true, example = "root")
    @NotNull(message = "用户名不能为空")
    private String username;

}
