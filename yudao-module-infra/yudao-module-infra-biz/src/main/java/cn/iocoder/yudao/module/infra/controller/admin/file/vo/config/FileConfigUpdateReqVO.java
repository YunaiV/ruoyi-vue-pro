package cn.iocoder.yudao.module.infra.controller.admin.file.vo.config;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.validation.constraints.NotNull;
import java.util.Map;

@ApiModel("管理后台 - 文件配置更新 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class FileConfigUpdateReqVO extends FileConfigBaseVO {

    @ApiModelProperty(value = "编号", required = true, example = "1")
    @NotNull(message = "编号不能为空")
    private Long id;

    @ApiModelProperty(value = "存储配置", required = true, notes = "配置是动态参数，所以使用 Map 接收")
    @NotNull(message = "存储配置不能为空")
    private Map<String, Object> config;

}
