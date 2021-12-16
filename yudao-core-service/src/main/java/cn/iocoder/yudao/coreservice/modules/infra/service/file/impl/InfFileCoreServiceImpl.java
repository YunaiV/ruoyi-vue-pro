package cn.iocoder.yudao.coreservice.modules.infra.service.file.impl;

import cn.hutool.core.io.FileTypeUtil;
import cn.iocoder.yudao.coreservice.modules.infra.dal.dataobject.file.InfFileDO;
import cn.iocoder.yudao.coreservice.modules.infra.dal.mysql.file.InfFileCoreMapper;
import cn.iocoder.yudao.coreservice.modules.infra.framework.file.config.FileProperties;
import cn.iocoder.yudao.coreservice.modules.infra.service.file.InfFileCoreService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.ByteArrayInputStream;

import static cn.iocoder.yudao.coreservice.modules.system.enums.SysErrorCodeConstants.*;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;

/**
 * core service 文件实现类
 *
 * @author 宋天
 */
@Service
public class InfFileCoreServiceImpl implements InfFileCoreService {

    @Resource
    private InfFileCoreMapper fileMapper;

    @Resource
    private FileProperties fileProperties;

    @Override
    public String createFile(String path, byte[] content) {
        if (fileMapper.selectCountById(path) > 0) {
            throw exception(FILE_PATH_EXISTS);
        }
        // 保存到数据库
        InfFileDO file = new InfFileDO();
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
    public InfFileDO getFile(String path) {
        return fileMapper.selectByPath(path);
    }

}
