package cn.iocoder.yudao.module.infra.framework.file.core.client.sftp;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.ftp.FtpConfig;
import cn.hutool.extra.ssh.JschRuntimeException;
import cn.hutool.extra.ssh.Sftp;
import cn.iocoder.yudao.module.infra.framework.file.core.client.AbstractFileClient;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

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
        reconnectIfTimeout();
        sftp.mkDirs(dir); // 需要创建父目录，不然会报错
        boolean success = sftp.upload(dir, fileName, new ByteArrayInputStream(content));
        if (!success) {
            throw new JschRuntimeException(StrUtil.format("上传文件到目标目录 ({}) 失败", filePath));
        }
        // 拼接返回路径
        return super.formatFileUrl(config.getDomain(), path);
    }

    @Override
    public String upload(InputStream content, long size, String path, String type) {
        // 执行写入：使用独立通道边读边写，完成后断开通道
        String filePath = getFilePath(path);
        String fileName = FileUtil.getName(filePath);
        String dir = StrUtil.removeSuffix(filePath, fileName);
        Sftp streamSftp = openStreamSftp();
        try {
            streamSftp.mkDirs(dir); // 需要创建父目录，不然会报错
            streamSftp.upload(dir, fileName, content);
        } finally {
            streamSftp.close();
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
        reconnectIfTimeout();
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        sftp.download(filePath, output);
        return output.toByteArray();
    }

    @Override
    public InputStream getStream(String path) {
        // 读取流期间会占用通道，使用同一会话下的独立通道；关闭流时断开通道
        Sftp streamSftp = openStreamSftp();
        try {
            InputStream input = streamSftp.getClient().get(getFilePath(path));
            return new FilterInputStream(input) {

                @Override
                public void close() throws IOException {
                    try {
                        super.close();
                    } finally {
                        streamSftp.close();
                    }
                }

            };
        } catch (SftpException ex) {
            streamSftp.close();
            throw new JschRuntimeException(ex);
        } catch (RuntimeException ex) {
            streamSftp.close();
            throw ex;
        }
    }

    /**
     * 打开文件流读写使用的独立通道：ChannelSftp 不是线程安全的，传输期间不占用共享通道
     *
     * @return 基于独立通道的 Sftp，使用完成后关闭
     */
    synchronized Sftp openStreamSftp() {
        reconnectIfTimeout();
        try {
            ChannelSftp channel = (ChannelSftp) sftp.getClient().getSession().openChannel("sftp");
            channel.connect(CONNECTION_TIMEOUT.intValue());
            return new Sftp(channel, CharsetUtil.CHARSET_UTF_8);
        } catch (JSchException ex) {
            throw new JschRuntimeException(ex);
        }
    }

    private String getFilePath(String path) {
        return config.getBasePath() + File.separator + path;
    }

    private synchronized void reconnectIfTimeout() {
        sftp.reconnectIfTimeout();
    }

}
