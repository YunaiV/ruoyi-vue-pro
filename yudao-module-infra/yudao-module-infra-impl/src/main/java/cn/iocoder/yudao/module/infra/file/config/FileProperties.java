package cn.iocoder.yudao.module.infra.file.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;

@ConfigurationProperties(prefix = "yudao.file")
@Validated
@Data
public class FileProperties {

    /**
     * 对应 InfFileController 的 getFile 方法
     */
    @NotNull(message = "基础文件路径不能为空")
    private String basePath;

    // TODO 七牛、等等

}
