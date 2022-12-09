package cn.iocoder.yudao.module.infra.controller.admin.file.vo.config;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.validation.constraints.NotNull;
import java.util.Map;

@Schema(title = "管理后台 - 文件配置创建 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class FileConfigCreateReqVO extends FileConfigBaseVO {

    @Schema(title = "存储器", required = true, example = "1", description = "参见 FileStorageEnum 枚举类")
    @NotNull(message = "存储器不能为空")
    private Integer storage;

    @Schema(title = "存储配置", required = true, description = "配置是动态参数，所以使用 Map 接收")
    @NotNull(message = "存储配置不能为空")
    private Map<String, Object> config;

}
