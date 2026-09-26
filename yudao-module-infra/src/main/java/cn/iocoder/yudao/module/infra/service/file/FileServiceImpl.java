package cn.iocoder.yudao.module.infra.service.file;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.http.HttpUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.infra.controller.admin.file.vo.file.FileCreateReqVO;
import cn.iocoder.yudao.module.infra.controller.admin.file.vo.file.FilePageReqVO;
import cn.iocoder.yudao.module.infra.controller.admin.file.vo.file.FilePresignedUrlRespVO;
import cn.iocoder.yudao.module.infra.dal.dataobject.file.FileDO;
import cn.iocoder.yudao.module.infra.dal.mysql.file.FileMapper;
import cn.iocoder.yudao.module.infra.framework.file.core.client.FileClient;
import cn.iocoder.yudao.module.infra.framework.file.core.utils.FilePathUtils;
import cn.iocoder.yudao.module.infra.framework.file.core.utils.FileTypeUtils;
import com.google.common.annotations.VisibleForTesting;
import jakarta.annotation.Resource;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

import static cn.hutool.core.date.DatePattern.PURE_DATE_PATTERN;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.infra.enums.ErrorCodeConstants.*;

/**
 * 文件 Service 实现类
 *
 * @author 芋道源码
 */
@Service
public class FileServiceImpl implements FileService {

    /**
     * 允许上传的文件类型，可按业务需要调整
     */
    private static final Set<String> ALLOWED_FILE_TYPES = Set.of(
            // 图片
            "image/jpeg", "image/png", "image/gif", "image/webp", "image/bmp",
            "image/vnd.microsoft.icon", "image/tiff", "image/avif", "image/heic", "image/heif", "image/svg+xml",
            // 文档
            "application/pdf", "text/plain", "text/csv", "text/markdown", "text/html", "application/xhtml+xml",
            "application/xml", "application/json", "application/epub+zip", "message/rfc822", "application/vnd.ms-outlook",
            "application/msword", "application/vnd.ms-excel", "application/vnd.ms-powerpoint",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "application/vnd.openxmlformats-officedocument.presentationml.presentation",
            // 压缩包
            "application/zip", "application/x-rar-compressed", "application/x-7z-compressed", "application/gzip",
            // 音视频
            "audio/mpeg", "audio/vnd.wave", "audio/vorbis", "audio/ogg", "audio/opus", "audio/mp4", "audio/webm",
            "audio/x-aac", "audio/x-flac", "audio/amr", "audio/x-ms-wma",
            "video/mp4", "video/quicktime", "video/x-msvideo", "video/webm", "video/x-ms-wmv", "video/x-flv",
            "application/x-matroska", "video/x-matroska"
    );

    /**
     * 上传文件的前缀，是否包含日期（yyyyMMdd）
     *
     * 目的：按照日期，进行分目录
     */
    static boolean PATH_PREFIX_DATE_ENABLE = true;
    /**
     * 上传文件的后缀，是否启用
     *
     * 算法：当前时间戳（毫秒）+ 5 位随机数；目的是保证文件的唯一性，避免覆盖
     * 定制：可按需调整成 UUID、或者其他方式
     */
    static boolean PATH_SUFFIX_TIMESTAMP_ENABLE = false;
    /**
     * 后缀是否作为上级目录
     *
     * true：{@code yyyyMMdd/<后缀>/原文件名.ext}；保留原文件名
     * false：{@code yyyyMMdd/原文件名_<后缀>.ext}；后缀拼到文件名
     */
    static boolean PATH_SUFFIX_AS_DIRECTORY = true;

    @Resource
    private FileConfigService fileConfigService;

    @Resource
    private FileMapper fileMapper;

    @Override
    public PageResult<FileDO> getFilePage(FilePageReqVO pageReqVO) {
        return fileMapper.selectPage(pageReqVO);
    }

    @Override
    @SneakyThrows
    public String createFile(byte[] content, String name, String directory, String type) {
        // 1.1 处理 name 的合法性，禁止携带目录路径
        name = FilePathUtils.validateFileName(name);

        // 1.2.1 校验文件内容和类型，不信任客户端传入的 type
        if (ArrayUtil.isEmpty(content)) {
            throw exception(FILE_IS_EMPTY);
        }
        type = FileTypeUtils.getMineType(content, name);
        String nameType = StrUtil.isNotEmpty(FileUtil.extName(name)) ? validateFileType(name) : null;
        if (!ALLOWED_FILE_TYPES.contains(type)
                || (FileTypeUtils.isImage(nameType) && ObjectUtil.notEqual(type, nameType))) {
            throw exception(FILE_TYPE_NOT_ALLOWED);
        }
        // 1.2.2 处理 name 为空的情况
        if (StrUtil.isEmpty(name)) {
            name = DigestUtil.sha256Hex(content);
        }
        if (StrUtil.isEmpty(FileUtil.extName(name))) {
            // 如果 name 没有后缀 type，则补充后缀
            String extension = FileTypeUtils.getExtension(type);
            if (StrUtil.isNotEmpty(extension)) {
                name = name + extension;
            }
        }

        // 2.1 生成上传的 path，需要保证唯一
        String path = generateUploadPath(name, directory);
        // 2.2 上传到文件存储器
        FileClient client = fileConfigService.getMasterFileClient();
        Assert.notNull(client, "客户端(master) 不能为空");
        String url = client.upload(content, path, type);

        // 3. 保存到数据库
        fileMapper.insert(new FileDO().setConfigId(client.getId())
                .setName(name).setPath(path).setUrl(url)
                .setType(type).setSize((long) content.length));
        return url;
    }

    @VisibleForTesting
    String generateUploadPath(String name, String directory) {
        // 1.1 处理 name 和 directory 的合法性
        name = FilePathUtils.validateFileName(name);
        FilePathUtils.validatePath(name);
        FilePathUtils.validateDirectory(directory);
        // 1.2 生成前缀、后缀
        String prefix = null;
        if (PATH_PREFIX_DATE_ENABLE) {
            prefix = LocalDateTimeUtil.format(LocalDateTimeUtil.now(), PURE_DATE_PATTERN);
        }
        String suffix = null;
        if (PATH_SUFFIX_TIMESTAMP_ENABLE) {
            // 5 位随机数，避免同一毫秒内的重复
            suffix = String.valueOf(System.currentTimeMillis()) + RandomUtil.randomInt(10000, 100000);
        }

        // 2.1 先拼接 suffix 后缀
        if (StrUtil.isNotEmpty(suffix)) {
            if (PATH_SUFFIX_AS_DIRECTORY) {
                name = suffix + StrUtil.SLASH + name;
            } else {
                String ext = FileUtil.extName(name);
                if (StrUtil.isNotEmpty(ext)) {
                    name = FileUtil.mainName(name) + StrUtil.C_UNDERLINE + suffix + StrUtil.DOT + ext;
                } else {
                    name = name + StrUtil.C_UNDERLINE + suffix;
                }
            }
        }
        // 2.2 再拼接 prefix 前缀
        if (StrUtil.isNotEmpty(prefix)) {
            name = prefix + StrUtil.SLASH + name;
        }
        // 2.3 最后拼接 directory 目录
        if (StrUtil.isNotEmpty(directory)) {
            name = directory + StrUtil.SLASH + name;
        }
        return name;
    }

    @Override
    @SneakyThrows
    public FilePresignedUrlRespVO presignPutUrl(String name, String directory) {
        // 1.1 生成上传的 path，需要保证唯一
        String path = generateUploadPath(name, directory);
        // 1.2 前端直传无法读取文件内容，此处仅校验文件名类型
        validateFileType(name);

        // 2. 获取文件预签名地址
        FileClient fileClient = fileConfigService.getMasterFileClient();
        String uploadUrl = fileClient.presignPutUrl(path);
        String visitUrl = fileClient.presignGetUrl(path, null);
        return new FilePresignedUrlRespVO().setConfigId(fileClient.getId())
                .setPath(path).setUploadUrl(uploadUrl).setUrl(visitUrl);
    }

    @Override
    public String presignGetUrl(String url, Integer expirationSeconds) {
        FileClient fileClient = fileConfigService.getMasterFileClient();
        return fileClient.presignGetUrl(url, expirationSeconds);
    }

    @Override
    public Long createFile(FileCreateReqVO createReqVO) {
        // 1.1 校验参数的合法性
        FilePathUtils.validatePath(createReqVO.getPath());
        createReqVO.setName(FilePathUtils.validateFileName(createReqVO.getName()));
        // 1.2 校验原文件名和存储路径的类型，前端直传不校验文件内容
        String type = validateFileType(createReqVO.getName());
        if (ObjectUtil.notEqual(type, validateFileType(FileUtil.getName(createReqVO.getPath())))) {
            throw exception(FILE_TYPE_NOT_ALLOWED);
        }
        createReqVO.setType(type);
        // 1.3 处理 URL 的合法性，移除 URL 中的查询参数（例如签名参数），保证 URL 的唯一性
        createReqVO.setUrl(HttpUtils.removeUrlQuery(createReqVO.getUrl())); // 目的：移除私有桶情况下，URL 的签名参数

        // 2. 保存到数据库
        FileDO file = BeanUtils.toBean(createReqVO, FileDO.class);
        fileMapper.insert(file);
        return file.getId();
    }

    private String validateFileType(String name) {
        String type = FileTypeUtils.getMineType(name);
        if (!ALLOWED_FILE_TYPES.contains(type)) {
            throw exception(FILE_TYPE_NOT_ALLOWED);
        }
        return type;
    }

    @Override
    public FileDO getFile(Long id) {
        return validateFileExists(id);
    }

    @Override
    public void deleteFile(Long id) throws Exception {
        // 1.1 校验存在
        FileDO file = validateFileExists(id);
        // 1.2 校验路径合法性，避免误删文件存储器中的其他文件
        FilePathUtils.validatePath(file.getPath());

        // 2.1 从文件存储器中删除
        FileClient client = fileConfigService.getFileClient(file.getConfigId());
        Assert.notNull(client, "客户端({}) 不能为空", file.getConfigId());
        client.delete(file.getPath());

        // 2.2 删除记录
        fileMapper.deleteById(id);
    }

    @Override
    @SneakyThrows
    public void deleteFileList(List<Long> ids) {
        // 删除文件
        List<FileDO> files = fileMapper.selectByIds(ids);
        for (FileDO file : files) {
            FilePathUtils.validatePath(file.getPath());
            // 获取客户端
            FileClient client = fileConfigService.getFileClient(file.getConfigId());
            Assert.notNull(client, "客户端({}) 不能为空", file.getPath());
            // 删除文件
            client.delete(file.getPath());
        }

        // 删除记录
        fileMapper.deleteByIds(ids);
    }

    private FileDO validateFileExists(Long id) {
        FileDO fileDO = fileMapper.selectById(id);
        if (fileDO == null) {
            throw exception(FILE_NOT_EXISTS);
        }
        return fileDO;
    }

    @Override
    public byte[] getFileContent(Long configId, String path) throws Exception {
        // 1. 校验路径合法性
        FilePathUtils.validatePath(path);

        // 2.1 获取客户端
        FileClient client = fileConfigService.getFileClient(configId);
        Assert.notNull(client, "客户端({}) 不能为空", configId);
        // 2.2 获取文件内容
        return client.getContent(path);
    }

    @Override
    public FileDO getFileByConfigIdAndPath(Long configId, String path) {
        return fileMapper.selectLatestByConfigIdAndPath(configId, path);
    }

}
