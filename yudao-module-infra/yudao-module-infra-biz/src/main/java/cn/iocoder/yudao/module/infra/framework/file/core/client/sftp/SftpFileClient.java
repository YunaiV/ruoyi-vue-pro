package cn.iocoder.yudao.module.infra.framework.file.core.client.sftp;

import cn.hutool.core.io.FileUtil;
import cn.hutool.extra.ssh.Sftp;
import cn.iocoder.yudao.framework.common.util.io.FileUtils;
import cn.iocoder.yudao.module.infra.framework.file.core.client.AbstractFileClient;

import java.io.File;

/**
 * Sftp 文件客户端
 *
 * @author 芋道源码
 */
public class SftpFileClient extends AbstractFileClient<SftpFileClientConfig> {

    private Sftp sftp;

    public SftpFileClient(Long id, SftpFileClientConfig config) {
        super(id, config);
    }

    @Override
    protected void doInit() {
        // 补全风格。例如说 Linux 是 /，Windows 是 \
        if (!config.getBasePath().endsWith(File.separator)) {
            config.setBasePath(config.getBasePath() + File.separator);
        }
        // 初始化 Ftp 对象
        this.sftp = new Sftp(config.getHost(), config.getPort(), config.getUsername(), config.getPassword());
    }

    @Override
    public String upload(byte[] content, String path, String type) {
        // 执行写入
        String filePath = getFilePath(path);
        File file = FileUtils.createTempFile(content);
        sftp.upload(filePath, file);
        // 拼接返回路径
        return super.formatFileUrl(config.getDomain(), path);
    }

    @Override
    public void delete(String path) {
        String filePath = getFilePath(path);
        sftp.delFile(filePath);
    }

    @Override
    public byte[] getContent(String path) {
        String filePath = getFilePath(path);
        File destFile = FileUtils.createTempFile();
        sftp.download(filePath, destFile);
        return FileUtil.readBytes(destFile);
    }

    private String getFilePath(String path) {
        return config.getBasePath() + path;
    }

}
