package cn.iocoder.yudao.module.infra.controller.admin.file.vo.config;

import cn.iocoder.yudao.framework.file.core.client.FileClientConfig;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@ApiModel("管理后台 - 文件配置 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class FileConfigRespVO extends FileConfigBaseVO {

    @ApiModelProperty(value = "编号", required = true, example = "1")
    private Long id;

    @ApiModelProperty(value = "存储器", required = true, example = "1", notes = "参见 FileStorageEnum 枚举类")
    @NotNull(message = "存储器不能为空")
    private Integer storage;

    @ApiModelProperty(value = "是否为主配置", required = true, example = "true")
    @NotNull(message = "是否为主配置不能为空")
    private Boolean master;

    @ApiModelProperty(value = "存储配置", required = true)
    private FileClientConfig config;

    @ApiModelProperty(value = "创建时间", required = true)
    private LocalDateTime createTime;

}
