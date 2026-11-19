package cn.iocoder.yudao.module.infra.framework.file.core.client;

import cn.iocoder.yudao.module.infra.framework.file.core.client.db.DBFileClientConfig;
import cn.iocoder.yudao.module.infra.framework.file.core.client.ftp.FtpFileClientConfig;
import cn.iocoder.yudao.module.infra.framework.file.core.client.local.LocalFileClientConfig;
import cn.iocoder.yudao.module.infra.framework.file.core.client.sftp.SftpFileClientConfig;
import cn.iocoder.yudao.module.infra.framework.file.core.client.s3.S3FileClientConfig;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * 文件客户端的配置
 * 不同实现的客户端，需要不同的配置，通过子类来定义
 *
 * @author 芋道源码
 */
// 类型名沿用完整类名，兼容已有数据库配置；仅允许显式注册的配置类型，不按输入加载任意类。
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "@class")
@JsonSubTypes({
        @JsonSubTypes.Type(value = DBFileClientConfig.class, name = "cn.iocoder.yudao.module.infra.framework.file.core.client.db.DBFileClientConfig"),
        @JsonSubTypes.Type(value = FtpFileClientConfig.class, name = "cn.iocoder.yudao.module.infra.framework.file.core.client.ftp.FtpFileClientConfig"),
        @JsonSubTypes.Type(value = LocalFileClientConfig.class, name = "cn.iocoder.yudao.module.infra.framework.file.core.client.local.LocalFileClientConfig"),
        @JsonSubTypes.Type(value = SftpFileClientConfig.class, name = "cn.iocoder.yudao.module.infra.framework.file.core.client.sftp.SftpFileClientConfig"),
        @JsonSubTypes.Type(value = S3FileClientConfig.class, name = "cn.iocoder.yudao.module.infra.framework.file.core.client.s3.S3FileClientConfig")
})
public interface FileClientConfig {
}
