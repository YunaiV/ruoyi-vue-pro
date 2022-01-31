package cn.iocoder.yudao.module.infra.service.file;

import cn.hutool.core.io.FileTypeUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.infra.controller.admin.file.vo.FilePageReqVO;
import cn.iocoder.yudao.module.infra.dal.dataobject.file.FileDO;
import cn.iocoder.yudao.module.infra.dal.mysql.file.FileMapper;
import cn.iocoder.yudao.module.infra.framework.file.config.FileProperties;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.ByteArrayInputStream;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.infra.enums.ErrorCodeConstants.*;

/**
 * 文件 Service 实现类
 *
 * @author 芋道源码
 */
@Service
public class FileServiceImpl implements FileService {

    @Resource
    private FileMapper fileMapper;

    @Resource
    private FileProperties fileProperties;

    @Override
    public PageResult<FileDO> getFilePage(FilePageReqVO pageReqVO) {
        return fileMapper.selectPage(pageReqVO);
    }

    @Override
    public String createFile(String path, byte[] content) {
        if (fileMapper.selectCountById(path) > 0) {
            throw exception(FILE_PATH_EXISTS);
        }
        // 保存到数据库
        FileDO file = new FileDO();
        file.setId(path);
        file.setType(FileTypeUtil.getType(new ByteArrayInputStream(content)));
        file.setContent(content);
        fileMapper.insert(file);
        // 拼接路径返回
        return fileProperties.getBasePath() + path;
    }

    @Override
    public void deleteFile(String id) {
        // 校验存在
        this.validateFileExists(id);
        // 更新
        fileMapper.deleteById(id);
    }

    private void validateFileExists(String id) {
        if (fileMapper.selectById(id) == null) {
            throw exception(FILE_NOT_EXISTS);
        }
    }

    @Override
    public FileDO getFile(String path) {
        return fileMapper.selectByPath(path);
    }

}
