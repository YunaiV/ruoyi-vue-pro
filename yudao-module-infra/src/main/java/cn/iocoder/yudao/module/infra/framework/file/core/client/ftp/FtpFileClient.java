package cn.iocoder.yudao.module.infra.framework.file.core.client.ftp;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.ftp.Ftp;
import cn.hutool.extra.ftp.FtpConfig;
import cn.hutool.extra.ftp.FtpException;
import cn.hutool.extra.ftp.FtpMode;
import cn.iocoder.yudao.module.infra.framework.file.core.client.AbstractFileClient;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Ftp 文件客户端
 *
 * @author 芋道源码
 */
public class FtpFileClient extends AbstractFileClient<FtpFileClientConfig> {

    /**
     * 连接超时时间，单位：毫秒
     */
    private static final Long CONNECTION_TIMEOUT = 3000L;
    /**
     * 读写超时时间，单位：毫秒
     */
    private static final Long SO_TIMEOUT = 10000L;

    private FtpConfig ftpConfig;
    private Ftp ftp;

    public FtpFileClient(Long id, FtpFileClientConfig config) {
        super(id, config);
    }

    @Override
    protected void doInit() {
        // 初始化 Ftp 对象：https://gitee.com/zhijiantianya/yudao-cloud/pulls/207/
        ftpConfig = new FtpConfig(config.getHost(), config.getPort(), config.getUsername(), config.getPassword(),
                CharsetUtil.CHARSET_UTF_8, null, null);
        ftpConfig.setConnectionTimeout(CONNECTION_TIMEOUT);
        ftpConfig.setSoTimeout(SO_TIMEOUT);
        this.ftp = createFtp();
    }

    /**
     * 创建 Ftp 连接；文件流读写期间会占用数据连接，使用独立连接，不阻塞共享连接上的其它操作
     *
     * @return Ftp 连接
     */
    Ftp createFtp() {
        return new Ftp(ftpConfig, FtpMode.valueOf(config.getMode()));
    }

    @Override
    public String upload(byte[] content, String path, String type) {
        // 执行写入
        String filePath = getFilePath(path);
        String fileName = FileUtil.getName(filePath);
        String dir = StrUtil.removeSuffix(filePath, fileName);
        reconnectIfTimeout();
        boolean success = ftp.upload(dir, fileName, new ByteArrayInputStream(content)); // 不需要主动创建目录，ftp 内部已经处理（见源码）
        if (!success) {
            throw new FtpException(StrUtil.format("上传文件到目标目录 ({}) 失败", filePath));
        }
        // 拼接返回路径
        return super.formatFileUrl(config.getDomain(), path);
    }

    @Override
    public String upload(InputStream content, long size, String path, String type) throws IOException {
        // 执行写入：使用独立连接边读边写，完成后释放连接
        String filePath = getFilePath(path);
        String fileName = FileUtil.getName(filePath);
        String dir = StrUtil.removeSuffix(filePath, fileName);
        try (Ftp streamFtp = createFtp()) {
            boolean success = streamFtp.upload(dir, fileName, content); // 不需要主动创建目录，ftp 内部已经处理（见源码）
            if (!success) {
                throw new FtpException(StrUtil.format("上传文件到目标目录 ({}) 失败", filePath));
            }
        }
        // 拼接返回路径
        return super.formatFileUrl(config.getDomain(), path);
    }

    @Override
    public void delete(String path) {
        String filePath = getFilePath(path);
        reconnectIfTimeout();
        ftp.delFile(filePath);
    }

    @Override
    public byte[] getContent(String path) {
        String filePath = getFilePath(path);
        String fileName = FileUtil.getName(filePath);
        String dir = StrUtil.removeSuffix(filePath, fileName);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        reconnectIfTimeout();
        ftp.download(dir, fileName, out);
        return out.toByteArray();
    }

    @Override
    public InputStream getStream(String path) throws IOException {
        String filePath = getFilePath(path);
        String fileName = FileUtil.getName(filePath);
        String dir = StrUtil.removeSuffix(filePath, fileName);
        // 读取流期间会占用数据连接，使用独立连接；关闭流时结束传输并释放连接
        Ftp streamFtp = createFtp();
        try {
            FTPClient client = streamFtp.getClient();
            if (!streamFtp.cd(dir)) {
                // 目录不存在（550）按文件不存在返回 null，由调用方按 404 处理；其它失败仍抛出
                if (client.getReplyCode() == FTPReply.FILE_UNAVAILABLE) {
                    IoUtil.close(streamFtp);
                    return null;
                }
                throw new FtpException(StrUtil.format("切换到目录 ({}) 失败：{}", dir, client.getReplyString()));
            }
            client.setFileType(FTPClient.BINARY_FILE_TYPE);
            InputStream input = client.retrieveFileStream(fileName);
            if (input == null) {
                // 文件不存在或不可用（550）返回 null；连接、传输等其它失败仍抛出
                if (client.getReplyCode() == FTPReply.FILE_UNAVAILABLE) {
                    IoUtil.close(streamFtp);
                    return null;
                }
                throw new FtpException(StrUtil.format("读取文件 ({}) 失败：{}", filePath, client.getReplyString()));
            }
            return new FilterInputStream(input) {

                @Override
                public void close() throws IOException {
                    try {
                        super.close();
                        client.completePendingCommand();
                    } finally {
                        IoUtil.close(streamFtp);
                    }
                }

            };
        } catch (IOException | RuntimeException ex) {
            IoUtil.close(streamFtp);
            throw ex;
        }
    }

    private String getFilePath(String path) {
        return config.getBasePath() + StrUtil.SLASH + path;
    }

    private synchronized void reconnectIfTimeout() {
        ftp.reconnectIfTimeout();
    }

}
