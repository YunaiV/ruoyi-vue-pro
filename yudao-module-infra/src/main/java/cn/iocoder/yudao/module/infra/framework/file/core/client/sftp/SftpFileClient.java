package cn.iocoder.yudao.module.infra.framework.file.core.client.sftp;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.ftp.FtpConfig;
import cn.hutool.extra.ssh.JschRuntimeException;
import cn.hutool.extra.ssh.Sftp;
import cn.iocoder.yudao.framework.common.util.io.FileUtils;
import cn.iocoder.yudao.module.infra.framework.file.core.client.AbstractFileClient;
import com.jcraft.jsch.JSch;

import java.io.File;

/**
 * Sftp 文件客户端
 *
 * @author 芋道源码
 */
public class SftpFileClient extends AbstractFileClient<SftpFileClientConfig> {

    /**
     * 连接超时时间，单位：毫秒
     */
    private static final Long CONNECTION_TIMEOUT = 3000L;
    /**
     * 读写超时时间，单位：毫秒
     */
    private static final Long SO_TIMEOUT = 10000L;

    static {
        // 某些旧的 sftp 服务器仅支持 ssh-dss 协议，该协议并不安全，默认不支持该协议，按需添加
        JSch.setConfig("server_host_key", JSch.getConfig("server_host_key") + ",ssh-dss");
    }

    private Sftp sftp;

    public SftpFileClient(Long id, SftpFileClientConfig config) {
        super(id, config);
    }

    @Override
    protected void doInit() {
        // 初始化 Sftp 对象
        FtpConfig ftpConfig = new FtpConfig(config.getHost(), config.getPort(), config.getUsername(), config.getPassword(),
                CharsetUtil.CHARSET_UTF_8, null, null);
        ftpConfig.setConnectionTimeout(CONNECTION_TIMEOUT);
        ftpConfig.setSoTimeout(SO_TIMEOUT);
        this.sftp = new Sftp(ftpConfig);
    }

    @Override
    public String upload(byte[] content, String path, String type) {
        // 执行写入
        String filePath = getFilePath(path);
        String fileName = FileUtil.getName(filePath);
        String dir = StrUtil.removeSuffix(filePath, fileName);
        File file = FileUtils.createTempFile(content);
        reconnectIfTimeout();
        sftp.mkDirs(dir); // 需要创建父目录，不然会报错
        boolean success = sftp.upload(filePath, file);
        if (!success) {
            throw new JschRuntimeException(StrUtil.format("上传文件到目标目录 ({}) 失败", filePath));
        }
        // 拼接返回路径
        return super.formatFileUrl(config.getDomain(), path);
    }

    @Override
    public void delete(String path) {
        String filePath = getFilePath(path);
        reconnectIfTimeout();
        sftp.delFile(filePath);
    }

    @Override
    public byte[] getContent(String path) {
        String filePath = getFilePath(path);
        File destFile = FileUtils.createTempFile();
        reconnectIfTimeout();
        sftp.download(filePath, destFile);
        return FileUtil.readBytes(destFile);
    }

    private String getFilePath(String path) {
        return config.getBasePath() + File.separator + path;
    }

    private synchronized void reconnectIfTimeout() {
        sftp.reconnectIfTimeout();
    }

}
