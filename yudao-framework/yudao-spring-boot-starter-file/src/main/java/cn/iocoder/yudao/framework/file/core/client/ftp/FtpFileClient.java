package cn.iocoder.yudao.framework.file.core.client.ftp;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.ftp.Ftp;
import cn.hutool.extra.ftp.FtpException;
import cn.hutool.extra.ftp.FtpMode;
import cn.iocoder.yudao.framework.file.core.client.AbstractFileClient;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * Ftp 文件客户端
 *
 * @author 芋道源码
 */
public class FtpFileClient extends AbstractFileClient<FtpFileClientConfig> {

    private Ftp ftp;

    public FtpFileClient(Long id, FtpFileClientConfig config) {
        super(id, config);
    }

    @Override
    protected void doInit() {
        // 把配置的 \ 替换成 /, 如果路径配置 \a\test, 替换成 /a/test, 替换方法已经处理 null 情况
        config.setBasePath(StrUtil.replace(config.getBasePath(), StrUtil.BACKSLASH, StrUtil.SLASH));
        // ftp的路径是 / 结尾
        if (!config.getBasePath().endsWith(StrUtil.SLASH)) {
            config.setBasePath(config.getBasePath() + StrUtil.SLASH);
        }
        // 初始化 Ftp 对象
        this.ftp = new Ftp(config.getHost(), config.getPort(), config.getUsername(), config.getPassword(),
                CharsetUtil.CHARSET_UTF_8, null, null, FtpMode.valueOf(config.getMode()));
    }

    @Override
    public String upload(byte[] content, String path) {
        // 执行写入
        String filePath = getFilePath(path);
        String fileName = FileUtil.getName(filePath);
        String dir = StrUtil.removeSuffix(filePath, fileName);
        ftp.reconnectIfTimeout();
        boolean success = ftp.upload(dir, fileName, new ByteArrayInputStream(content));
        if (!success) {
            throw new FtpException(StrUtil.format("上传文件到目标目录 ({}) 失败", filePath));
        }
        // 拼接返回路径
        return super.formatFileUrl(config.getDomain(), path);
    }

    @Override
    public void delete(String path) {
        String filePath = getFilePath(path);
        ftp.reconnectIfTimeout();
        ftp.delFile(filePath);
    }

    @Override
    public byte[] getContent(String path) {
        String filePath = getFilePath(path);
        String fileName = FileUtil.getName(filePath);
        String dir = StrUtil.removeSuffix(filePath, fileName);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ftp.reconnectIfTimeout();
        ftp.download(dir, fileName, out);
        return out.toByteArray();
    }

    private String getFilePath(String path) {
        return config.getBasePath() + path;
    }

}